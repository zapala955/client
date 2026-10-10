"""Synthesises Photo Mode's camera sounds (mono, 44.1 kHz) as WAV, then: ffmpeg -i X.wav -ac 1 -c:a libvorbis -q:a 6
assets/liquidclient/sounds/camera/X.ogg. Usage: python tools/camera_sounds.py <out dir>

shutter.wav  – a DSLR shutter: mirror slap + first curtain ("ka"), then second curtain + mirror return
               ("chunk") ~85 ms later; damped body resonances, a faint metallic ring, a tiny room.
focus.wav    – the classic focus-confirm "bip-bip": two short, soft sine beeps.
"""
import sys
import numpy as np
import soundfile as sf

SR = 44100
rng = np.random.default_rng(26)


def t_axis(seconds):
    return np.arange(int(SR * seconds)) / SR


def damped(freq, tau, seconds, phase=0.0):
    t = t_axis(seconds)
    return np.sin(2 * np.pi * freq * t + phase) * np.exp(-t / tau)


def noise_burst(tau, seconds, color=0.0):
    n = rng.standard_normal(int(SR * seconds))
    if color > 0:  # simple one-pole low-pass for a darker burst
        out = np.empty_like(n)
        acc = 0.0
        for i, v in enumerate(n):
            acc += (v - acc) * (1 - color)
            out[i] = acc
        n = out / (np.std(out) + 1e-9)
    t = t_axis(seconds)
    return n * np.exp(-t / tau)


def biquad(x, kind, f0, q=0.707):
    w0 = 2 * np.pi * f0 / SR
    alpha = np.sin(w0) / (2 * q)
    c = np.cos(w0)
    if kind == 'hp':
        b = [(1 + c) / 2, -(1 + c), (1 + c) / 2]
    else:
        b = [(1 - c) / 2, 1 - c, (1 - c) / 2]
    a = [1 + alpha, -2 * c, 1 - alpha]
    b = [v / a[0] for v in b]
    a = [1.0, a[1] / a[0], a[2] / a[0]]
    y = np.zeros_like(x)
    x1 = x2 = y1 = y2 = 0.0
    for i, v in enumerate(x):
        out = b[0] * v + b[1] * x1 + b[2] * x2 - a[1] * y1 - a[2] * y2
        x2, x1 = x1, v
        y2, y1 = y1, out
        y[i] = out
    return y


def place(buffer, sound, at_seconds, gain=1.0):
    start = int(at_seconds * SR)
    end = min(len(buffer), start + len(sound))
    buffer[start:end] += sound[:end - start] * gain


def click(pitch=1.0, weight=1.0, seconds=0.09):
    """One mechanical event: a sharp transient, a body 'thock' and a faint ring."""
    s = np.zeros(int(SR * seconds))
    place(s, noise_burst(0.0016, 0.012), 0.0, 0.9 * weight)                     # the snap
    place(s, noise_burst(0.006, 0.03, color=0.55), 0.0005, 0.5 * weight)         # dull rustle
    place(s, damped(165 * pitch, 0.016, seconds), 0.0, 0.55 * weight)            # body
    place(s, damped(410 * pitch, 0.011, seconds, 0.6), 0.0, 0.4 * weight)
    place(s, damped(980 * pitch, 0.006, seconds, 1.3), 0.0, 0.22 * weight)
    place(s, damped(2650 * pitch, 0.0055, seconds, 0.2), 0.0, 0.07 * weight)     # metallic ring
    place(s, damped(5150 * pitch, 0.0035, seconds, 2.1), 0.0, 0.035 * weight)
    return s


def room(x, mix=0.12):
    """A very small room: a few early reflections and a short decaying tail."""
    out = x.copy()
    for delay, g in [(0.0047, 0.32), (0.0083, 0.24), (0.0131, 0.18), (0.0197, 0.12)]:
        d = int(delay * SR)
        out[d:] += x[:-d] * g * mix * 4
    return out


def finish(x, peak_db=-3.0):
    x = biquad(x, 'hp', 110, 0.6)
    x = biquad(x, 'lp', 9500, 0.6)
    fade = int(0.004 * SR)
    x[-fade:] *= np.linspace(1, 0, fade)
    peak = np.max(np.abs(x)) + 1e-9
    return x / peak * (10 ** (peak_db / 20))


def shutter():
    total = np.zeros(int(SR * 0.26))
    place(total, click(1.0, 1.0), 0.0)                    # mirror up + first curtain
    place(total, click(1.08, 0.38, 0.03), 0.011)          # curtain brake bounce
    place(total, click(1.18, 0.78), 0.086)                # second curtain + mirror down
    for k, g in enumerate([0.22, 0.12, 0.06]):            # settling rattle
        place(total, click(1.3 + 0.1 * k, g, 0.025), 0.104 + 0.0065 * k)
    return finish(room(total))


def focus():
    seconds = 0.2
    total = np.zeros(int(SR * seconds))
    for start in (0.0, 0.085):
        t = t_axis(0.05)
        env = np.minimum(1, t / 0.004) * np.minimum(1, (0.05 - t) / 0.012)
        beep = (np.sin(2 * np.pi * 2730 * t) + 0.12 * np.sin(2 * np.pi * 5460 * t)) * env
        place(total, beep, start)
    return finish(total, -9.0)


out = sys.argv[1]
sf.write(f'{out}/shutter.wav', shutter().astype(np.float32), SR, subtype='PCM_16')
sf.write(f'{out}/focus.wav', focus().astype(np.float32), SR, subtype='PCM_16')
print('ok')
