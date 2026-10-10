"""
Builds Liquid Client's interface sounds from Google's Material Design sound resources
(CC BY 4.0, https://m2.material.io/design/sound/sound-resources.html) into
src/main/resources/assets/liquidclient/sounds/ui/*.ogg.

The Material kit was designed as one family – soft, short, warm – so every file gets the same gain
(the kit's own balance between sounds is kept); silence is trimmed with short fades so nothing
clicks, and loudness per event is set in sounds.json. The WAV masters are downloaded from the
Internet Archive mirror of the kit on first run and cached next to this script.

Requires: numpy, soundfile, ffmpeg (with libvorbis) on PATH.   Run:  python tools/ui_sounds.py
"""
import os
import subprocess
import tempfile
import urllib.parse
import urllib.request

import numpy as np
import soundfile as sf

ROOT = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(ROOT, "..", "src", "main", "resources", "assets", "liquidclient", "sounds", "ui")
CACHE = os.path.join(ROOT, ".material_sounds")
MIRROR = "https://archive.org/download/material-design-sound-resources/material_product_sounds/wav/"

# event file -> Material master (folder/name)
MAP = {
    "click1": "03 Primary System Sounds/ui_tap-variant-01",
    "click2": "03 Primary System Sounds/ui_tap-variant-02",
    "click3": "03 Primary System Sounds/ui_tap-variant-03",
    "click4": "03 Primary System Sounds/ui_tap-variant-04",
    "hover": "03 Primary System Sounds/navigation_hover-tap",
    "toggle_on": "03 Primary System Sounds/state-change_confirm-up",
    "toggle_off": "03 Primary System Sounds/state-change_confirm-down",
    "open": "03 Primary System Sounds/navigation_forward-selection-minimal",
    "close": "03 Primary System Sounds/navigation_backward-selection-minimal",
    "tick": "03 Primary System Sounds/ui_tap-variant-02",
    "wake": "02 Alerts and Notifications/notification_ambient",
}
GAIN_DB = 13.0          # brings the kit's quiet masters (taps peak ~-16 dBFS) to a usable level
FLOOR_DB = -58.0        # trim threshold relative to each sound's peak
FADE_OUT_S = 0.012
# Switches are flipped often: their two-note confirm chimes are cut short with a gentle fade.
MAX_LENGTH = {"toggle_on": (0.42, 0.11), "toggle_off": (0.42, 0.11)}


def master(name):
    os.makedirs(CACHE, exist_ok=True)
    local = os.path.join(CACHE, os.path.basename(name) + ".wav")
    if not os.path.exists(local):
        url = MIRROR + urllib.parse.quote(name + ".wav")
        print("downloading", url)
        urllib.request.urlretrieve(url, local)
    return local


def process(src, event):
    x, sr = sf.read(src, always_2d=True)
    level = np.abs(x).max(axis=1)
    peak = level.max()
    active = np.where(level > peak * 10 ** (FLOOR_DB / 20))[0]
    start = max(0, active[0] - int(sr * 0.001))
    end = min(len(x), active[-1] + int(sr * FADE_OUT_S))
    y = x[start:end].copy()
    fade = int(sr * FADE_OUT_S)
    if event in MAX_LENGTH:
        length, fade_s = MAX_LENGTH[event]
        y = y[: int(sr * length)]
        fade = int(sr * fade_s)
    y[-fade:] *= np.cos(np.linspace(0, np.pi / 2, fade))[:, None] ** 2
    y[: int(sr * 0.0015)] *= np.linspace(0, 1, int(sr * 0.0015))[:, None]
    y *= 10 ** (GAIN_DB / 20)
    if np.abs(y).max() > 0.98:
        y *= 0.98 / np.abs(y).max()
    return y, sr


def encode(y, sr, out):
    with tempfile.TemporaryDirectory() as tmp:
        wav = os.path.join(tmp, "x.wav")
        sf.write(wav, y, sr, subtype="PCM_24")
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-c:a", "libvorbis", "-q:a", "7", out], check=True)


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for old in os.listdir(OUT):
        if old.endswith(".ogg") and old[:-4] not in MAP:
            os.remove(os.path.join(OUT, old))
    for event, name in MAP.items():
        y, sr = process(master(name), event)
        out = os.path.join(OUT, event + ".ogg")
        encode(y, sr, out)
        print(f"{event:11s} <- {os.path.basename(name):40s} {len(y) / sr:.3f}s  peak {20 * np.log10(np.abs(y).max()):.1f} dBFS")
