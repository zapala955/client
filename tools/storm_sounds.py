"""Synthesises Storm Chaser's sounds (44.1 kHz) as WAV, then: ffmpeg -i X.wav -c:a libvorbis -q:a 5
assets/liquidclient/sounds/storm/X.ogg. Usage: python tools/storm_sounds.py <out dir>
Needs numpy, scipy and soundfile.

Thunder is modelled, not faked: a lightning channel is a tortuous path kilometres long, and every
stretch of it sends out a shock (an N-wave) that reaches you when sound has covered the distance
(343 m/s). The nearest stretch arrives first – a sharp crack if it's close – and the rest of the
channel follows over seconds, from further and further away: the rumble. Stretches lying across your
line of sight add up coherently (loud), ones pointing at you smear out. The air swallows high
frequencies with distance (far thunder only rolls), the ground adds a reflection, hills and the cloud
base an echo tail.

thunder_crack1..3.wav   (mono)   a strike 120–300 m away: crack, ripping tearing, heavy rumble
thunder_near1..3.wav    (mono)   0.7–1.6 km: a deep clap and a long rumble
thunder_far1..4.wav     (mono)   4–11 km: a low, rolling, muffled grumble
rain_heavy.wav          (stereo) loop: a downpour on every surface – dense patter, hiss and roar
wind.wav                (stereo) loop: a gusting wind bed with drifting whistles
"""
import sys
import numpy as np
import soundfile as sf
from scipy import signal

SR = 44100
rng = np.random.default_rng(1926)


# ---- the channel --------------------------------------------------------------------------------

def channel(top, bottom, levels, rng, tortuosity=0.18):
    """Midpoint displacement of top → bottom (like render.storm.BoltGenerator), plus in-cloud branches."""
    pts = [np.array(top, float), np.array(bottom, float)]
    for lvl in range(levels):
        out = [pts[0]]
        for a, b in zip(pts[:-1], pts[1:]):
            d = b - a
            ln = np.linalg.norm(d)
            off = rng.standard_normal(3) * tortuosity * ln
            off -= d * (off @ d) / (ln * ln)
            out += [a + d * (0.5 + rng.uniform(-0.12, 0.12)) + off, b]
        pts = out
    segs = [(a, b) for a, b in zip(pts[:-1], pts[1:])]
    # Horizontal channels inside the cloud: most of a flash's length is up there.
    for _ in range(rng.integers(3, 7)):
        root = pts[rng.integers(0, len(pts) // 4 + 1)]
        ang = rng.uniform(0, 2 * np.pi)
        length = rng.uniform(1500, 5000)
        end = root + np.array([np.cos(ang) * length, rng.uniform(-150, 300), np.sin(ang) * length])
        sub = [root, end]
        for lvl in range(levels - 1):
            out = [sub[0]]
            for a, b in zip(sub[:-1], sub[1:]):
                d = b - a
                ln = np.linalg.norm(d)
                off = rng.standard_normal(3) * tortuosity * ln
                off -= d * (off @ d) / (ln * ln)
                out += [a + d * 0.5 + off, b]
            sub = out
        segs += [(a, b) for a, b in zip(sub[:-1], sub[1:])]
    return segs


def n_wave(duration):
    """An N-wave: a sudden rise, a linear fall through zero, a sudden return – the shape of a shock."""
    n = max(8, int(duration * SR))
    w = np.linspace(1.0, -1.0, n)
    w[0] = 0.0
    return w


def absorb(x, distance):
    """Air absorption: the further, the more of the highs are gone (roughly f² · distance)."""
    fc = float(np.clip(7000.0 * (100.0 / max(distance, 50.0)) ** 0.9, 55.0, 11000.0))
    sos = signal.butter(4, fc, 'low', fs=SR, output='sos')
    return signal.sosfilt(sos, x)


def shocks(segs, listener, total, rng):
    """Every stretch of the channel as an N-wave arriving at the listener, filtered by its distance."""
    bins = 16
    rs = np.array([np.linalg.norm((a + b) * 0.5 - listener) for a, b in segs])
    r_min = rs.min()
    edges = np.geomspace(r_min, rs.max() + 1.0, bins + 1)
    out = np.zeros(total)
    for k in range(bins):
        idx = np.nonzero((rs >= edges[k]) & (rs < edges[k + 1]))[0]
        if len(idx) == 0:
            continue
        group = np.zeros(total)
        for i in idx:
            a, b = segs[i]
            mid = (a + b) * 0.5
            seg = b - a
            length = np.linalg.norm(seg)
            look = (mid - listener) / rs[i]
            # Stretches across the line of sight arrive as one front: loud. Along it: smeared out.
            across = np.sqrt(max(0.0, 1.0 - (seg @ look / max(length, 1e-6)) ** 2))
            start = int(((rs[i] - r_min) / 343.0 + 0.06) * SR)
            if start >= total:
                continue
            # N-waves lengthen as they travel; the long ones carry the low boom.
            dur = 0.004 * (rs[i] / 80.0) ** 0.3 * rng.uniform(0.7, 1.6)
            amp = np.sqrt(length) * (0.15 + across ** 2) / rs[i]
            wave = n_wave(dur) * amp
            smear = int(length * (1.0 - across) / 343.0 * SR)
            if smear > 8:
                kernel = np.hanning(smear * 2 + 1)
                wave = np.convolve(wave, kernel / kernel.sum())
            end = min(total, start + len(wave))
            group[start:end] += wave[:end - start]
        out += absorb(group, float(np.median(rs[idx])))
    return out


def thunder(distance, seconds, seed, kind):
    """kind: 'crack' (close: a tearing crack, then the boom and rumble), 'near' or 'far'."""
    rng = np.random.default_rng(seed)
    top = np.array([rng.uniform(-400, 400), rng.uniform(1200, 2000), rng.uniform(-400, 400)])
    segs = channel(top, np.zeros(3), 8, rng)
    angle = rng.uniform(0, 2 * np.pi)
    listener = np.array([distance * np.cos(angle), 1.7, distance * np.sin(angle)])
    total = int(seconds * SR)
    t = np.arange(total) / SR
    out = shocks(segs, listener, total, rng)
    # The ground reflection: the same shock a few milliseconds later.
    delay = int(0.004 * SR)
    out[delay:] += out[:-delay] * 0.5
    out /= np.max(np.abs(out)) + 1e-12

    if kind == 'crack':
        # The channel's lowest stretch, right next to you: a ripping, tearing crack – a rapid burst of
        # shocks – led by one big bang.
        tear = np.zeros(total)
        n = int(0.32 * SR)
        count = 420
        for k in range(count):
            at = int(0.06 * SR + n * (k / count) ** 1.6 + rng.integers(0, 200))
            w = n_wave(rng.uniform(0.0006, 0.0025)) * rng.uniform(0.2, 1.0) * (1.0 - 0.75 * k / count)
            tear[at:at + len(w)] += w * (1 if rng.random() < 0.5 else -1)
        bang = n_wave(0.007) * 3.0
        tear[int(0.06 * SR):int(0.06 * SR) + len(bang)] += bang
        tear = signal.sosfilt(signal.butter(2, 9500, 'low', fs=SR, output='sos'), tear)
        out = out * 0.6 + tear / (np.max(np.abs(tear)) + 1e-12) * 1.0

    # The rumble: thunder's body below ~150 Hz, rolling with the arrivals (never quite silent).
    env = np.abs(signal.hilbert(out))
    env = signal.sosfilt(signal.butter(1, 2.5, 'low', fs=SR, output='sos'), env)
    env = env / (np.max(env) + 1e-12)
    env = 0.25 + 0.75 * env
    rumble = signal.sosfilt(signal.butter(4, [20, 150], 'band', fs=SR, output='sos'), rng.standard_normal(total))
    rumble /= np.max(np.abs(rumble)) + 1e-12
    if kind == 'crack':
        mix_shock, mix_rumble, attack, decay = 1.0, 0.9, 0.0, 2.6
    elif kind == 'near':
        mix_shock, mix_rumble, attack, decay = 0.75, 1.0, 0.12, 3.6
    else:
        mix_shock, mix_rumble, attack, decay = 0.5, 1.0, 0.9, 4.8
        out = absorb(out, distance * 0.5)
    shaped = out * mix_shock + rumble * env * mix_rumble
    # The overall arc: a crack hits at once, distant thunder swells in and rolls on.
    arc = (1.0 - np.exp(-t / max(attack, 1e-3))) * np.exp(-np.maximum(t - attack, 0.0) / decay)
    arc = 0.15 + 0.85 * arc if kind != 'crack' else 0.08 + 0.92 * arc
    shaped *= arc
    # Echoes from hills and the cloud base: a long, darkening tail.
    tail_len = int(2.6 * SR)
    tt = np.arange(tail_len) / SR
    ir = rng.standard_normal(tail_len) * np.exp(-tt / 0.8)
    ir = signal.sosfilt(signal.butter(2, 900 if kind == 'crack' else 450, 'low', fs=SR, output='sos'), ir)
    wet = signal.fftconvolve(shaped, ir)[:total]
    shaped = shaped + wet / (np.max(np.abs(wet)) + 1e-12) * np.max(np.abs(shaped)) * 0.45
    return finish(shaped, peak_db=-1.0 if kind == 'crack' else -2.0)


# ---- rain ---------------------------------------------------------------------------------------

def rain(seconds):
    total = int(seconds * SR)
    channels = []
    hiss_common = rng.standard_normal(total)
    for side in range(2):
        out = np.zeros(total)
        # Thousands of drops a second, each a tiny damped ping on leaves, stone, water, wood.
        drops = int(seconds * 2600)
        starts = rng.integers(0, total - 400, drops)
        freqs = np.exp(rng.uniform(np.log(1400), np.log(9000), drops))
        amps = np.minimum(rng.pareto(3.0, drops) * 0.02 + 0.008, 0.06)
        decays = rng.uniform(0.0007, 0.003, drops)
        n = 380
        tt = np.arange(n) / SR
        for s, f, a, d in zip(starts, freqs, amps, decays):
            out[s:s + n] += np.sin(2 * np.pi * f * tt + rng.uniform(0, 6.28)) * np.exp(-tt / d) * a
        # A few big drops close by: duller plops.
        for _ in range(int(seconds * 10)):
            s = rng.integers(0, total - 2000)
            f = rng.uniform(380, 900)
            t2 = np.arange(2000) / SR
            out[s:s + 2000] += np.sin(2 * np.pi * f * t2 * (1 + 0.6 * np.exp(-t2 / 0.004))) * np.exp(-t2 / 0.012) * rng.uniform(0.02, 0.05)
        # The hiss of rain everywhere (mostly shared between the ears) and the low roar of the downpour.
        hiss = 0.6 * hiss_common + 0.4 * rng.standard_normal(total)
        hiss = signal.sosfilt(signal.butter(2, [900, 11000], 'band', fs=SR, output='sos'), hiss)
        roar = signal.sosfilt(signal.butter(2, [120, 700], 'band', fs=SR, output='sos'), rng.standard_normal(total))
        swell = 1.0 + 0.12 * np.sin(2 * np.pi * np.arange(total) / SR / seconds * 3 + side)
        out = out + hiss * 0.11 * swell + roar * 0.09 * swell
        channels.append(out)
    stereo = np.stack(channels, 1)
    return finish_loop(stereo, fade=1.5, peak_db=-4.0)


# ---- wind ---------------------------------------------------------------------------------------

def wind(seconds):
    total = int(seconds * SR)
    t = np.arange(total) / SR
    channels = []
    # Gusts: a slow swell, the same in both ears; whistles drift in pitch.
    gust = 0.55 + 0.25 * np.sin(2 * np.pi * t / seconds * 2) + 0.2 * np.sin(2 * np.pi * t / seconds * 5 + 1.3)
    gust = np.clip(gust, 0.2, 1.0)
    for side in range(2):
        noise = rng.standard_normal(total)
        body = signal.sosfilt(signal.butter(2, [60, 900], 'band', fs=SR, output='sos'), noise)
        air = signal.sosfilt(signal.butter(2, [900, 5000], 'band', fs=SR, output='sos'), rng.standard_normal(total))
        out = body * (0.5 + 0.8 * gust) + air * 0.25 * gust ** 2
        # Whistles: narrow resonances in the gusts, sliding with them (built block by block).
        for k, base in enumerate((420.0, 680.0, 1150.0)):
            whistle = np.zeros(total)
            block = 2048
            phase = 0.0
            zi = None
            for s in range(0, total, block):
                g = gust[s]
                f = base * (0.8 + 0.5 * g) * (1.0 + 0.03 * side)
                sos = signal.butter(2, [f * 0.97, f * 1.03], 'band', fs=SR, output='sos')
                seg = noise[s:s + block]
                if zi is None:
                    zi = signal.sosfilt_zi(sos) * 0.0
                y, zi = signal.sosfilt(sos, seg, zi=zi)
                whistle[s:s + block] = y
            out += whistle * (0.9 - 0.25 * k) * gust ** 3
        channels.append(out)
    stereo = np.stack(channels, 1)
    return finish_loop(stereo, fade=2.0, peak_db=-5.0)


# ---- finishing ----------------------------------------------------------------------------------

def finish(x, peak_db=-2.0):
    x = signal.sosfilt(signal.butter(2, 18, 'high', fs=SR, output='sos'), x)
    fade = int(0.6 * SR)
    x[-fade:] *= np.linspace(1, 0, fade) ** 2
    x[:16] *= np.linspace(0, 1, 16)
    return x / (np.max(np.abs(x)) + 1e-12) * (10 ** (peak_db / 20))


def finish_loop(x, fade, peak_db):
    """A seamless loop: the last `fade` seconds cross-fade (equal power) into the start."""
    x = signal.sosfilt(signal.butter(2, 25, 'high', fs=SR, output='sos'), x, axis=0)
    n = int(fade * SR)
    head = x[:n].copy()
    tail = x[-n:]
    ramp = np.linspace(0, np.pi / 2, n)[:, None]
    looped = x[:-n].copy()
    looped[:n] = head * np.sin(ramp) + tail * np.cos(ramp)
    return looped / (np.max(np.abs(looped)) + 1e-12) * (10 ** (peak_db / 20))


if __name__ == '__main__':
    out = sys.argv[1]
    for i, (d, s) in enumerate([(140, 9.0), (220, 8.5), (300, 9.5)]):
        sf.write(f'{out}/thunder_crack{i + 1}.wav', thunder(d, s, 100 + i, 'crack').astype(np.float32), SR, subtype='PCM_16')
    for i, (d, s) in enumerate([(700, 10.0), (1100, 11.0), (1600, 11.5)]):
        sf.write(f'{out}/thunder_near{i + 1}.wav', thunder(d, s, 200 + i, 'near').astype(np.float32), SR, subtype='PCM_16')
    for i, (d, s) in enumerate([(4000, 12.0), (6000, 13.0), (8500, 13.0), (11000, 14.0)]):
        sf.write(f'{out}/thunder_far{i + 1}.wav', thunder(d, s, 300 + i, 'far').astype(np.float32), SR, subtype='PCM_16')
    sf.write(f'{out}/rain_heavy.wav', rain(13.5).astype(np.float32), SR, subtype='PCM_16')
    sf.write(f'{out}/wind.wav', wind(18.0).astype(np.float32), SR, subtype='PCM_16')
    print('ok')
