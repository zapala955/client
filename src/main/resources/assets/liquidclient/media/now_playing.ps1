# Liquid Client - "now playing" bridge.
#
# Reads the Windows System Media Transport Controls (the media session Spotify and other players
# publish for the volume flyout / lock screen) and prints one JSON object per line on stdout:
#   {"type":"state","active":true,"app":...,"title":...,"artist":...,"album":...,"status":...,
#    "position":ms,"duration":ms,"updated":unixMs}
# Commands on stdin, one per line: "source spotify" | "source any" | "toggle" | "next" | "prev" |
# "seek <ms>" | "quit".
# Exits as soon as stdin closes, so it can never outlive Minecraft. Read-only apart from the
# play/pause/skip commands; no network access.

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding $false
Add-Type -AssemblyName System.Runtime.WindowsRuntime

$asTaskGeneric = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
    $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'
})[0]

function Await($operation, [Type]$type) {
    $task = $asTaskGeneric.MakeGenericMethod($type).Invoke($null, @($operation))
    if (-not $task.Wait(3000)) { return $null }
    return $task.Result
}

function Emit($object) {
    [Console]::Out.WriteLine(($object | ConvertTo-Json -Compress -Depth 3))
    [Console]::Out.Flush()
}

$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
$TManager = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]
$TProps = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties]

$manager = Await ($TManager::RequestAsync()) $TManager
if ($null -eq $manager) {
    Emit @{ type = 'error'; message = 'Windows media session manager unavailable' }
    exit 1
}

$source = 'spotify'
$stdin = [Console]::OpenStandardInput()
$buffer = New-Object byte[] 4096
$pendingRead = $stdin.ReadAsync($buffer, 0, $buffer.Length)
$pendingText = ''
$lastJson = ''
$lastEmit = [DateTime]::MinValue

function Select-Session {
    foreach ($candidate in $manager.GetSessions()) {
        if ($candidate.SourceAppUserModelId -match 'spotify') { return $candidate }
    }
    if ($source -eq 'any') { return $manager.GetCurrentSession() }
    return $null
}

while ($true) {
    if ($pendingRead.IsCompleted) {
        $count = $pendingRead.Result
        if ($count -le 0) { exit 0 }
        $pendingText += [System.Text.Encoding]::UTF8.GetString($buffer, 0, $count)
        while ($pendingText.Contains("`n")) {
            $cut = $pendingText.IndexOf("`n")
            $command = $pendingText.Substring(0, $cut).Trim()
            $pendingText = $pendingText.Substring($cut + 1)
            $target = Select-Session
            switch -Regex ($command) {
                '^source (spotify|any)$' { $source = $Matches[1]; $lastJson = '' }
                '^toggle$' { if ($null -ne $target) { $null = $target.TryTogglePlayPauseAsync() } }
                '^next$' { if ($null -ne $target) { $null = $target.TrySkipNextAsync() } }
                '^prev$' { if ($null -ne $target) { $null = $target.TrySkipPreviousAsync() } }
                '^seek (\d+)$' { if ($null -ne $target) { $null = $target.TryChangePlaybackPositionAsync([long]$Matches[1] * 10000) } }
                '^quit$' { exit 0 }
            }
        }
        $pendingRead = $stdin.ReadAsync($buffer, 0, $buffer.Length)
    }

    try {
        $session = Select-Session
        if ($null -eq $session) {
            $state = [ordered]@{ type = 'state'; active = $false }
        } else {
            $props = Await ($session.TryGetMediaPropertiesAsync()) $TProps
            $timeline = $session.GetTimelineProperties()
            $playback = $session.GetPlaybackInfo()
            $state = [ordered]@{
                type     = 'state'
                active   = $true
                app      = [string]$session.SourceAppUserModelId
                title    = [string]$props.Title
                artist   = [string]$props.Artist
                album    = [string]$props.AlbumTitle
                status   = [string]$playback.PlaybackStatus
                position = [long]$timeline.Position.TotalMilliseconds
                duration = [long]($timeline.EndTime - $timeline.StartTime).TotalMilliseconds
                updated  = [long]$timeline.LastUpdatedTime.ToUnixTimeMilliseconds()
            }
        }
        $json = $state | ConvertTo-Json -Compress
        $now = [DateTime]::UtcNow
        if ($json -ne $lastJson -or ($now - $lastEmit).TotalSeconds -ge 2) {
            [Console]::Out.WriteLine($json)
            [Console]::Out.Flush()
            $lastJson = $json
            $lastEmit = $now
        }
    } catch {
        Emit @{ type = 'error'; message = [string]$_.Exception.Message }
        Start-Sleep -Milliseconds 1500
    }
    Start-Sleep -Milliseconds 300
}
