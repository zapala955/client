"""
Builds the Lyrics element's typefaces into src/main/resources/assets/liquidclient/font/lyrics/*.ttf:
static instances of the variable fonts, subset to Latin + Latin Extended-A + common punctuation
(GPOS kerning kept), renamed "Liquid <Family>" (see rename()), and prints each face's cap height
ratio – the font definitions (font/lyric_*.json, shift = cap * 30 - 7) and LiquidText.TextStyle use it.

Sources: the families' files from github.com/google/fonts (ofl/playfairdisplay, ofl/dancingscript,
ofl/bebasneue, ofl/nunito, ofl/jetbrainsmono, ofl/orbitron, ofl/caveat, apache/permanentmarker)
in tools/lyric_fonts_src/<directory>/, licences included (LYRIC-FONTS-LICENSES.txt).

Requires: fonttools.   Run:  python tools/lyric_fonts.py
"""
import json
import os
import sys

from fontTools.ttLib import TTFont
from fontTools.varLib import instancer
from fontTools import subset
from fontTools.pens.boundsPen import BoundsPen

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "lyric_fonts_src")
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "src", "main", "resources", "assets", "liquidclient",
                                                          "font", "lyrics")
os.makedirs(OUT, exist_ok=True)

# id, source file, wght (None = static)
FACES = [
    ("elegant", "playfairdisplay/PlayfairDisplay[wght].ttf", 400),
    ("elegant_bold", "playfairdisplay/PlayfairDisplay[wght].ttf", 700),
    ("elegant_italic", "playfairdisplay/PlayfairDisplay-Italic[wght].ttf", 400),
    ("elegant_bold_italic", "playfairdisplay/PlayfairDisplay-Italic[wght].ttf", 700),
    ("script", "dancingscript/DancingScript[wght].ttf", 400),
    ("script_bold", "dancingscript/DancingScript[wght].ttf", 700),
    ("poster", "bebasneue/BebasNeue-Regular.ttf", None),
    ("rounded", "nunito/Nunito[wght].ttf", 400),
    ("rounded_bold", "nunito/Nunito[wght].ttf", 800),
    ("rounded_italic", "nunito/Nunito-Italic[wght].ttf", 400),
    ("mono", "jetbrainsmono/JetBrainsMono[wght].ttf", 400),
    ("mono_bold", "jetbrainsmono/JetBrainsMono[wght].ttf", 700),
    ("mono_italic", "jetbrainsmono/JetBrainsMono-Italic[wght].ttf", 400),
    ("marker", "permanentmarker/PermanentMarker-Regular.ttf", None),
    ("futuristic", "orbitron/Orbitron[wght].ttf", 500),
    ("futuristic_bold", "orbitron/Orbitron[wght].ttf", 800),
    ("handwriting", "caveat/Caveat[wght].ttf", 500),
    ("handwriting_bold", "caveat/Caveat[wght].ttf", 700),
]

UNICODES = (list(range(0x20, 0x7F)) + list(range(0xA0, 0x180)) + list(range(0x2010, 0x2028))
            + list(range(0x2030, 0x203B)) + [0x20AC, 0x2122])

def rename(font, face_id):
    """Subset + instanced = a Modified Version under the OFL, which may not carry a Reserved Font Name
    (Playfair Display, Dancing Script, Orbitron), so every face is renamed "Liquid <Family>"."""
    parts = face_id.split("_")
    family = "Liquid " + parts[0].title()
    sub = " ".join(p.title() for p in parts[1:]) or "Regular"
    name = font["name"]
    for name_id in (1, 2, 3, 4, 6, 16, 17, 18, 21, 22, 25):
        name.removeNames(nameID=name_id)
    full = family + ("" if sub == "Regular" else " " + sub)
    for name_id, value in ((1, family), (2, sub), (3, full + ";LiquidClient"), (4, full),
                           (6, (family + "-" + sub).replace(" ", ""))):
        name.setName(value, name_id, 3, 1, 0x409)


result = {}
for face_id, source, wght in FACES:
    font = TTFont(os.path.join(SRC, source))
    if wght is not None and "fvar" in font:
        axes = {a.axisTag: a.defaultValue for a in font["fvar"].axes}
        axes["wght"] = wght
        font = instancer.instantiateVariableFont(font, axes)
    options = subset.Options()
    options.layout_features = ["kern"]
    options.name_IDs = ["*"]
    options.name_languages = ["*"]
    options.notdef_outline = True
    options.hinting = False
    options.desubroutinize = True
    subsetter = subset.Subsetter(options)
    subsetter.populate(unicodes=UNICODES)
    subsetter.subset(font)
    rename(font, face_id)
    path = os.path.join(OUT, face_id + ".ttf")
    font.save(path)

    font = TTFont(path)
    glyph_set = font.getGlyphSet()
    cmap = font.getBestCmap()
    upm = font["head"].unitsPerEm
    pen = BoundsPen(glyph_set)
    glyph_set[cmap[ord("H")]].draw(pen)
    cap = pen.bounds[3] / upm
    result[face_id] = {"cap": round(cap, 4), "bytes": os.path.getsize(path), "glyphs": len(font.getGlyphOrder()),
                       "gpos": "GPOS" in font}
    print(f"{face_id:22s} cap {cap:.4f}  {os.path.getsize(path) // 1024:4d} KB  {len(font.getGlyphOrder())} glyphs  GPOS={'GPOS' in font}")

print(json.dumps(result, indent=1))
