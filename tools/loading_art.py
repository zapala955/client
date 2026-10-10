"""Generates the loading-screen art (run from the repo root: python tools/loading_art.py).

textures/gui/loading/drop.png  the client's drop mark, white on transparent (1024 px, mipmapped in game)
textures/gui/loading/glow.png  soft radial glow, white, tinted when drawn
textures/gui/loading/dot.png   anti-aliased white disc; its halves are the progress bar's round caps
textures/gui/loading/tile.png  the app icon (gradient squircle with the drop), 1024 px
textures/gui/loading/wordmark.png  "Liquid Client" in Inter SemiBold, white

The loading overlay runs before the client's shaders and fonts exist (they load in the very reload
it shows), so everything it draws is a plain texture blit with vanilla pipelines.
"""
import math
import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "liquidclient",
                   "textures", "gui", "loading")
SS = 4  # supersampling


def bezier(p0, p1, p2, p3, steps=160):
    pts = []
    for i in range(steps + 1):
        t = i / steps
        mt = 1 - t
        x = mt ** 3 * p0[0] + 3 * mt * mt * t * p1[0] + 3 * mt * t * t * p2[0] + t ** 3 * p3[0]
        y = mt ** 3 * p0[1] + 3 * mt * mt * t * p1[1] + 3 * mt * t * t * p2[1] + t ** 3 * p3[1]
        pts.append((x, y))
    return pts


def drop_outline():
    """Unit drop: tip at y=0, bottom at y=1, round base of radius r. Sides meet the circle tangentially."""
    r = 0.335
    cy = 1 - r
    right = bezier((0, 0), (0.05, 0.19), (r, 0.36), (r, cy))
    arc = [(r * math.cos(a), cy + r * math.sin(a)) for a in [i / 200 * math.pi for i in range(201)]]
    left = [(-x, y) for x, y in reversed(right)]
    return right + arc + left


def drop(size=1024, margin=0.06):
    big = size * SS
    img = Image.new("L", (big, big), 0)
    h = big * (1 - 2 * margin)
    pts = [(big / 2 + x * h, big * margin + y * h) for x, y in drop_outline()]
    ImageDraw.Draw(img).polygon(pts, fill=255)
    alpha = img.resize((size, size), Image.LANCZOS)
    out = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    out.putalpha(alpha)
    return out


def glow(size=256):
    img = Image.new("L", (size, size), 0)
    c = size / 2
    px = img.load()
    for y in range(size):
        for x in range(size):
            d = math.hypot(x + 0.5 - c, y + 0.5 - c) / c
            px[x, y] = int(255 * max(0.0, math.exp(-d * d * 4.2) - math.exp(-4.2)) / (1 - math.exp(-4.2)))
    out = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    out.putalpha(img.filter(ImageFilter.GaussianBlur(1)))
    return out


def dot(size=64):
    big = size * SS
    img = Image.new("L", (big, big), 0)
    ImageDraw.Draw(img).ellipse((0, 0, big - 1, big - 1), fill=255)
    out = Image.new("RGBA", (size, size), (255, 255, 255, 0))
    out.putalpha(img.resize((size, size), Image.LANCZOS))
    return out


FONT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "liquidclient", "font",
                    "inter-latin-600.ttf")


def tile(size=1024):
    """App icon: diagonal blue -> violet gradient squircle, soft top sheen, white drop."""
    big = size * 2
    grad = Image.new("RGBA", (big, big))
    px = grad.load()
    top = (0x3D, 0x8B, 0xFF)
    bottom = (0x8E, 0x5C, 0xF7)
    for y in range(big):
        for x in range(big):
            k = (x * 0.45 + y * 0.55) / big
            px[x, y] = tuple(int(top[i] + (bottom[i] - top[i]) * k) for i in range(3)) + (255,)
    mask = Image.new("L", (big, big), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, big - 1, big - 1), int(big * 0.225), fill=255)
    sheen = Image.new("L", (big, big), 0)
    sd = ImageDraw.Draw(sheen)
    for y in range(big // 2):
        sd.line((0, y, big, y), fill=int(46 * (1 - y / (big / 2)) ** 1.4))
    white = Image.new("RGBA", (big, big), (255, 255, 255, 255))
    grad = Image.composite(white, grad, sheen)
    d = drop(int(big * 0.56), margin=0.0).resize((int(big * 0.56), int(big * 0.56)), Image.LANCZOS)
    dl = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    dl.alpha_composite(d, ((big - d.width) // 2, (big - d.height) // 2 + int(big * 0.01)))
    grad.alpha_composite(dl)
    grad.putalpha(mask)
    return grad.resize((size, size), Image.LANCZOS)


def wordmark(height=220):
    font = ImageFont.truetype(FONT, height)
    text = "Liquid Client"
    box = font.getbbox(text)
    w, h = box[2] - box[0], box[3] - box[1]
    pad = height // 8
    img = Image.new("RGBA", (w + pad * 2, h + pad * 2), (255, 255, 255, 0))
    layer = Image.new("L", img.size, 0)
    ImageDraw.Draw(layer).text((pad - box[0], pad - box[1]), text, font=font, fill=255)
    img.putalpha(layer)
    return img


if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    drop().save(os.path.join(OUT, "drop.png"), optimize=True)
    glow().save(os.path.join(OUT, "glow.png"), optimize=True)
    dot().save(os.path.join(OUT, "dot.png"), optimize=True)
    tile().save(os.path.join(OUT, "tile.png"), optimize=True)
    wordmark().save(os.path.join(OUT, "wordmark.png"), optimize=True)
    print("wrote", os.path.abspath(OUT))
