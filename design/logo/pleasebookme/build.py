"""Build the PleaseBookMe exploratory board and production SVG/PNG assets.

Run with a Python environment containing fonttools, uharfbuzz, cairosvg and Pillow.
The outlined wordmark is generated from the included OFL-licensed Manrope source.
"""

from __future__ import annotations

from pathlib import Path
import html
from io import BytesIO

import cairosvg
import uharfbuzz as hb
from PIL import Image, ImageDraw
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont


ROOT = Path(__file__).parent
OUT = ROOT / "assets"
OUT.mkdir(exist_ok=True)
BLACK = "#111214"
RED = "#B7192B"
WHITE = "#FFFFFF"
WARM = "#F6F5F2"


def svg(width: int, height: int, body: str, title: str, viewbox: str | None = None) -> str:
    viewbox = viewbox or f"0 0 {width} {height}"
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" '
            f'viewBox="{viewbox}" role="img" aria-label="{html.escape(title)}">'
            f'<title>{html.escape(title)}</title>{body}</svg>')


def save(name: str, content: str, png_scale: float | None = None) -> None:
    target = OUT / name
    target.write_text(content)
    if png_scale:
        cairosvg.svg2png(bytestring=content.encode(), write_to=str(target.with_suffix(".png")),
                          scale=png_scale)


def mark(kind: int = 1, main: str = BLACK, accent: str = RED) -> str:
    # Each of the ten exploratory marks has a different geometric construction.
    if kind == 1:  # Aperture: bounded time with a single selected interval.
        return (f'<path fill="{main}" d="M16 15H81V30H32V70H81V85H16Z"/>'
                f'<path fill="{accent}" d="M66 43H81V70H66Z"/>')
    if kind == 2:  # Meridian: an interrupted radial form.
        return (f'<path fill="none" stroke="{main}" stroke-width="14" '
                f'd="M76 25A36 36 0 1 0 77 73"/>'
                f'<path fill="{accent}" d="M71 43H85V57H71Z"/>')
    if kind == 3:  # Exchange: adjacent availability columns.
        return (f'<path fill="{main}" d="M15 20H34V80H15ZM42 20H61V80H42ZM69 20H88V54H69Z"/>'
                f'<path fill="{accent}" d="M69 62H88V80H69Z"/>')
    if kind == 4:  # Bridge: a captured cross-axis moment.
        return (f'<path fill="{main}" d="M15 17H30V70H85V85H15ZM46 17H85V32H46Z"/>'
                f'<path fill="{accent}" d="M46 43H61V58H46Z"/>')
    if kind == 5:  # P monogram: stem and open bowl.
        return (f'<path fill="{main}" d="M19 15H71V29H35V43H72V15H86V57H35V85H19Z"/>'
                f'<path fill="{accent}" d="M72 43H86V57H72Z"/>')
    if kind == 6:  # Sequential B: two intervals on one spine.
        return (f'<path fill="{main}" d="M18 15H75V28H34V43H74V15H87V49H76V56H87V85H18ZM34 57V72H74V57Z" '
                f'fill-rule="evenodd"/>'
                f'<path fill="{accent}" d="M74 43H87V56H74Z"/>')
    if kind == 7:  # Keyhole: a negative-space interval in a solid block.
        return (f'<path fill="{main}" fill-rule="evenodd" '
                f'd="M17 17H83V83H17ZM34 34V66H66V34Z"/>'
                f'<path fill="{accent}" d="M66 17H83V34H66Z"/>')
    if kind == 8:  # Indexed timeline: staggered precision bars.
        return (f'<path fill="{main}" d="M16 20H84V34H16ZM16 44H63V58H16ZM16 68H84V82H16Z"/>'
                f'<path fill="{accent}" d="M69 44H84V58H69Z"/>')
    if kind == 9:  # Inclined threshold: a time slice in an angled frame.
        return (f'<path fill="{main}" d="M20 16H78L55 84H20L43 30H20Z"/>'
                f'<path fill="{accent}" d="M63 65H82V84H57Z"/>')
    if kind == 10:  # Quarter-turn: two offset angular intervals.
        return (f'<path fill="{main}" d="M17 17H72V32H32V72H17ZM39 68H68V39H83V83H39Z"/>'
                f'<path fill="{accent}" d="M68 17H83V32H68Z"/>')
    raise ValueError(kind)


def symbol(kind: int = 1, main: str = BLACK, accent: str = RED,
           canvas: int = 100, background: str | None = None) -> str:
    bg = f'<path fill="{background}" d="M0 0H100V100H0Z"/>' if background else ""
    return svg(canvas, canvas, bg + mark(kind, main, accent),
               "PleaseBookMe symbol", "0 0 100 100")


def outlined_wordmark() -> tuple[str, float]:
    font = TTFont(ROOT / "source" / "Manrope-variable.ttf")
    font = instantiateVariableFont(font, {"wght": 600}, inplace=True)
    font_bytes = BytesIO()
    font.save(font_bytes)
    data = font_bytes.getvalue()
    face = hb.Face(data)
    hb_font = hb.Font(face)
    hb_font.scale = (font["head"].unitsPerEm, font["head"].unitsPerEm)
    buf = hb.Buffer()
    buf.add_str("PleaseBookMe")
    buf.guess_segment_properties()
    hb.shape(hb_font, buf, {"kern": True})
    glyph_set = font.getGlyphSet()
    order = font.getGlyphOrder()
    upm = font["head"].unitsPerEm
    scale = 46 / upm
    x = 0
    paths = []
    for info, pos in zip(buf.glyph_infos, buf.glyph_positions):
        glyph = glyph_set[order[info.codepoint]]
        pen = SVGPathPen(glyph_set)
        glyph.draw(pen)
        dx = (x + pos.x_offset) * scale
        dy = pos.y_offset * scale
        paths.append(f'<path d="{pen.getCommands()}" transform="translate({dx:.4f} {46-dy:.4f}) '
                     f'scale({scale:.6f} {-scale:.6f})"/>')
        x += pos.x_advance + 12  # restrained optical tracking
    # Keep only the outlined artwork; the font is included as reproducible source.
    return "".join(paths), x * scale


WORD_PATHS, WORD_WIDTH = outlined_wordmark()


def wordmark(color: str = BLACK) -> str:
    return svg(round(WORD_WIDTH + 4), 54,
               f'<g fill="{color}" transform="translate(2 1)">{WORD_PATHS}</g>',
               "PleaseBookMe wordmark")


def lockup(main: str = BLACK, accent: str = RED, compact: bool = False) -> str:
    symbol_size = 58 if compact else 76
    word_scale = 0.72 if compact else 1.0
    gap = 13 if compact else 21
    width = round(symbol_size + gap + WORD_WIDTH * word_scale + 5)
    height = 62 if compact else 80
    symbol_x = 0
    symbol_y = (height - symbol_size) / 2
    word_x = symbol_size + gap
    word_y = (height - 54 * word_scale) / 2 + 1
    body = (f'<g transform="translate({symbol_x} {symbol_y}) scale({symbol_size/100})">'
            f'{mark(1, main, accent)}</g>'
            f'<g fill="{main}" transform="translate({word_x} {word_y}) scale({word_scale})">'
            f'{WORD_PATHS}</g>')
    return svg(width, height, body, "PleaseBookMe horizontal logo")


def board() -> str:
    names = ["APERTURE", "MERIDIAN", "EXCHANGE", "BRIDGE", "P MONOGRAM",
             "SEQUENTIAL B", "KEYHOLE", "INDEX", "THRESHOLD", "QUARTER-TURN"]
    families = ["Time block", "Abstract clock", "Time block", "Calendar geometry",
                "Letterform", "PB monogram", "Negative space", "Scheduling grid",
                "Luxury geometry", "Hybrid time / letter"]
    parts = [f'<path fill="{WARM}" d="M0 0H1200V1010H0Z"/>',
             '<text x="60" y="81" class="eyebrow">PLEASEBOOKME / IDENTITY EXPLORATION</text>',
             '<text x="60" y="136" class="title">Ten distinct constructions.</text>',
             '<text x="60" y="172" class="intro">One geometric mark. One reserved moment.</text>']
    for i in range(10):
        col, row = i % 5, i // 5
        x, y = 60 + col * 220, 230 + row * 366
        parts.append(f'<rect x="{x}" y="{y}" width="202" height="320" fill="#fff" stroke="#E5E2DB"/>')
        parts.append(f'<g transform="translate({x+42} {y+42}) scale(1.18)">{mark(i+1)}</g>')
        parts.append(f'<text x="{x+20}" y="{y+221}" class="number">{i+1:02d}</text>')
        parts.append(f'<text x="{x+20}" y="{y+251}" class="name">{names[i]}</text>')
        parts.append(f'<text x="{x+20}" y="{y+277}" class="family">{families[i]}</text>')
    style = ('<style>.eyebrow{font:600 15px Arial,sans-serif;letter-spacing:3px;fill:#B7192B}'
             '.title{font:600 43px Arial,sans-serif;fill:#111214}'
             '.intro{font:20px Arial,sans-serif;fill:#666}'
             '.number{font:600 13px Arial,sans-serif;letter-spacing:2px;fill:#B7192B}'
             '.name{font:600 20px Arial,sans-serif;letter-spacing:.5px;fill:#111214}'
             '.family{font:16px Arial,sans-serif;fill:#666}</style>')
    return svg(1200, 1010, style + "".join(parts), "PleaseBookMe logo exploration")


def finalists() -> str:
    choices = [(1, "APERTURE", "Selected interval", "in a bounded window"),
               (5, "P MONOGRAM", "Direct initial with", "a segmented bowl"),
               (7, "KEYHOLE", "Reserved corner around", "an open center"),
               (10, "QUARTER-TURN", "Two offset moments", "in one frame")]
    parts = [f'<path fill="{WARM}" d="M0 0H1200V820H0Z"/>',
             '<text x="60" y="78" class="eyebrow">SHORTLIST / FOUR DIRECTIONS</text>',
             '<text x="60" y="135" class="title">Aperture has the clearest memory.</text>']
    for i, (kind, name, line1, line2) in enumerate(choices):
        x = 60 + i * 282
        parts.append(f'<rect x="{x}" y="205" width="264" height="500" fill="white" stroke="#E5E2DB"/>')
        parts.append(f'<g transform="translate({x+34} 264) scale(1.96)">{mark(kind)}</g>')
        parts.append(f'<text x="{x+24}" y="526" class="name">{name}</text>')
        parts.append(f'<text x="{x+24}" y="560" class="desc">{line1}</text>')
        parts.append(f'<text x="{x+24}" y="582" class="desc">{line2}</text>')
        parts.append(f'<g transform="translate({x+24} 600) scale(.5)">{mark(kind)}</g>')
        parts.append(f'<g transform="translate({x+85} 607) scale(.36)">{mark(kind, BLACK, BLACK)}</g>')
    style = ('<style>.eyebrow{font:600 15px Arial,sans-serif;letter-spacing:3px;fill:#B7192B}'
             '.title{font:600 43px Arial,sans-serif;fill:#111214}'
             '.name{font:600 22px Arial,sans-serif;fill:#111214}'
             '.desc{font:14px Arial,sans-serif;fill:#666}</style>')
    return svg(1200, 820, style + "".join(parts), "PleaseBookMe four finalist logo concepts")


def presentation() -> str:
    body = [f'<path fill="{WARM}" d="M0 0H1600V1000H0Z"/>',
            '<text x="80" y="80" class="eyebrow">PLEASEBOOKME / BRAND IDENTITY</text>',
            '<text x="80" y="150" class="title">A precision instrument for time.</text>',
            f'<rect x="80" y="215" width="1440" height="300" fill="{WHITE}"/>',
            f'<g transform="translate(148 282) scale(1.6)">{mark()}</g>',
            f'<g fill="{BLACK}" transform="translate(355 315) scale(1.65)">{WORD_PATHS}</g>',
            f'<rect x="80" y="540" width="700" height="360" fill="{BLACK}"/>',
            f'<g transform="translate(157 627) scale(1.75)">{mark(1, WHITE, RED)}</g>',
            f'<g fill="{WHITE}" transform="translate(380 690) scale(.86)">{WORD_PATHS}</g>',
            f'<rect x="805" y="540" width="715" height="360" fill="{WHITE}"/>',
            f'<g transform="translate(879 620) scale(1.85)">{mark()}</g>',
            '<text x="1090" y="659" class="label">OPEN WINDOW</text>',
            '<text x="1090" y="698" class="label">SELECTED TIME</text>',
            f'<path stroke="{RED}" stroke-width="3" d="M1050 672H1080"/>',
            '<text x="80" y="947" class="foot">Black structure. Controlled red signal. A mark that holds in one color.</text>']
    style = ('<style>.eyebrow{font:600 16px Arial,sans-serif;letter-spacing:4px;fill:#B7192B}'
             '.title{font:600 50px Arial,sans-serif;fill:#111214}'
             '.label{font:600 17px Arial,sans-serif;letter-spacing:2px;fill:#111214}'
             '.foot{font:20px Arial,sans-serif;fill:#666}</style>')
    return svg(1600, 1000, style + "".join(body), "PleaseBookMe identity presentation")


save("exploration.svg", board(), 1.5)
save("finalists.svg", finalists(), 1.5)
save("identity-preview.svg", presentation(), 1.5)
save("symbol-color.svg", symbol(), 5)
save("symbol-black.svg", symbol(accent=BLACK), 5)
save("symbol-white.svg", symbol(main=WHITE, accent=WHITE), 5)
save("symbol-dark.svg", symbol(main=WHITE, accent=RED, background=BLACK), 5)
save("wordmark-black.svg", wordmark(), 2)
save("wordmark-white.svg", wordmark(WHITE), 2)
save("horizontal-color.svg", lockup(), 3)
save("horizontal-black.svg", lockup(accent=BLACK), 3)
save("horizontal-dark.svg", lockup(main=WHITE, accent=RED), 3)
save("compact-color.svg", lockup(compact=True), 3)
for size in (16, 24, 32, 64, 128):
    target = OUT / f"favicon-{size}.png"
    if size <= 32:
        # Snap the three orthogonal black bars and red interval to device pixels.
        image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        draw = ImageDraw.Draw(image)

        def rect(x1: int, y1: int, x2: int, y2: int, fill: str) -> None:
            a, b, c, d = (round(v * size / 100) for v in (x1, y1, x2, y2))
            draw.rectangle((a, b, c - 1, d - 1), fill=fill)

        rect(16, 15, 81, 30, BLACK)
        rect(16, 15, 32, 85, BLACK)
        rect(16, 70, 81, 85, BLACK)
        rect(66, 43, 81, 70, RED)
        image.save(target)
    else:
        cairosvg.svg2png(bytestring=symbol().encode(), write_to=str(target),
                          output_width=size, output_height=size)

size_sheet = Image.new("RGB", (900, 260), WARM)
size_draw = ImageDraw.Draw(size_sheet)
for i, size in enumerate((16, 24, 32, 64, 128)):
    icon = Image.open(OUT / f"favicon-{size}.png").convert("RGBA")
    tile = Image.new("RGBA", icon.size, WHITE)
    tile.alpha_composite(icon)
    x = 35 + i * 175
    size_sheet.paste(tile.convert("RGB").resize((128, 128), Image.Resampling.NEAREST), (x, 43))
    size_draw.text((x + 40, 190), f"{size} px", fill=BLACK)
size_sheet.save(OUT / "size-test.png")
print(f"Built brand assets in {OUT}")
