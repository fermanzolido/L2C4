# Generates the site's raster art: the social-card image and the two icon sizes a
# browser or a phone home screen asks for. Everything is drawn here rather than
# sourced, so nothing on the site is anyone else's artwork.
import io, math, os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "site", "assets")
FONTS = r"C:\Windows\Fonts"

BG_TOP = (11, 12, 17)
BG_BOT = (7, 8, 11)
GOLD = (205, 182, 127)
GOLD_HI = (242, 226, 180)
GOLD_LO = (138, 116, 66)
TEXT = (228, 231, 238)
MUTED = (139, 147, 167)


def font(name, size):
    return ImageFont.truetype(os.path.join(FONTS, name), size)


def vertical_gradient(size, top, bottom):
    w, h = size
    base = Image.new("RGB", (1, h))
    px = base.load()
    for y in range(h):
        t = y / max(h - 1, 1)
        px[0, y] = tuple(round(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
    return base.resize((w, h), Image.BILINEAR)


def radial_glow(size, center, radius, color, strength):
    """A soft light. Drawn small and scaled up, which is both faster and smoother
    than evaluating the falloff per pixel at full resolution."""
    w, h = size
    small = (max(w // 6, 1), max(h // 6, 1))
    layer = Image.new("L", small, 0)
    px = layer.load()
    cx, cy = center[0] / 6, center[1] / 6
    r = radius / 6
    for y in range(small[1]):
        for x in range(small[0]):
            d = math.hypot(x - cx, y - cy) / r
            if d < 1:
                px[x, y] = round(255 * strength * (1 - d) ** 2)
    layer = layer.resize(size, Image.BICUBIC).filter(ImageFilter.GaussianBlur(12))
    return Image.new("RGB", size, color), layer


def grain(size, amount=7):
    """Film grain. Without it a large flat gradient bands visibly on an OLED phone."""
    import random

    random.seed(4)
    w, h = size
    small = Image.new("L", (w // 2, h // 2))
    small.putdata([random.randint(128 - amount, 128 + amount) for _ in range(small.width * small.height)])
    return small.resize(size, Image.BILINEAR)


def tracked(draw, xy, text, fnt, fill, tracking, anchor_center_x=None):
    """PIL has no letter-spacing, so each glyph is placed by hand. Heraldic capitals
    need the tracking -- set solid they read as a wall."""
    widths = [draw.textlength(ch, font=fnt) for ch in text]
    total = sum(widths) + tracking * (len(text) - 1)
    x = (anchor_center_x - total / 2) if anchor_center_x is not None else xy[0]
    for ch, wch in zip(text, widths):
        draw.text((x, xy[1]), ch, font=fnt, fill=fill)
        x += wch + tracking
    return total


def crest(size, stroke_scale=1.0):
    """The mark: a heater shield holding a gate with four bars -- four for the
    chronicle. Drawn at 4x and downsampled, since PIL has no antialiased strokes."""
    s = size * 4
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    u = s / 72.0
    lw = max(round(3 * u * stroke_scale), 1)

    def P(pts):
        return [(x * u, y * u) for x, y in pts]

    # Shield: straight shoulders, curve gathered into a point at the chin.
    shield = [(36, 4), (66, 14), (66, 38)]
    for i in range(21):
        t = i / 20
        ang = math.radians(90 * t)
        shield.append((66 - 30 * (1 - math.cos(ang)) ** 1.35, 38 + 30 * math.sin(ang) ** 0.9))
    for i in range(21):
        t = i / 20
        ang = math.radians(90 * (1 - t))
        shield.append((6 + 30 * (1 - math.cos(ang)) ** 1.35, 38 + 30 * math.sin(ang) ** 0.9))
    shield += [(6, 14)]
    d.polygon(P(shield), fill=(205, 182, 127, 26))
    d.line(P(shield + [shield[0]]), fill=GOLD + (255,), width=lw, joint="curve")

    # The gate: an arch on two jambs.
    arch = []
    for i in range(33):
        a = math.pi * (1 - i / 32)
        arch.append((36 + 12 * math.cos(a), 36 - 12 * math.sin(a)))
    d.line(P([(24, 52), (24, 36)] + arch + [(48, 36), (48, 52)]), fill=GOLD + (255,), width=lw, joint="curve")

    # Four bars.
    for x in (30, 34.5, 39, 43.5):
        d.line(P([(x - 1.5, 52), (x - 1.5, 30)]), fill=GOLD + (200,), width=max(round(1.6 * u), 1))

    return img.resize((size, size), Image.LANCZOS)


def social_card():
    W, H = 1200, 630
    img = vertical_gradient((W, H), BG_TOP, BG_BOT)

    warm, mask = radial_glow((W, H), (600, 250), 620, (168, 132, 60), 0.42)
    img = Image.composite(Image.blend(img, warm, 0.55), img, mask)
    cool, mask = radial_glow((W, H), (170, 600), 520, (40, 70, 120), 0.30)
    img = Image.composite(Image.blend(img, cool, 0.5), img, mask)

    img = Image.blend(img, Image.merge("RGB", [grain((W, H))] * 3), 0.05)
    d = ImageDraw.Draw(img)

    # A drawn frame reads as intent; the platforms crop the outer few pixels.
    d.rectangle([28, 28, W - 29, H - 29], outline=(58, 52, 38), width=1)
    for corner in [(28, 28, 1, 1), (W - 29, 28, -1, 1), (28, H - 29, 1, -1), (W - 29, H - 29, -1, -1)]:
        cx, cy, sx, sy = corner
        d.line([(cx, cy), (cx + 26 * sx, cy)], fill=GOLD, width=2)
        d.line([(cx, cy), (cx, cy + 26 * sy)], fill=GOLD, width=2)

    mark = crest(132)
    img.paste(mark, (W // 2 - 66, 84), mark)

    d = ImageDraw.Draw(img)
    tracked(d, (0, 246), "LINEAGE 2  ·  CHRONICLE 4", font("georgiab.ttf", 22), GOLD, 6.5, anchor_center_x=W // 2)
    tracked(d, (0, 296), "SCIONS OF DESTINY", font("cambriab.ttf", 82), GOLD_HI, 7.0, anchor_center_x=W // 2)

    d.line([(W // 2 - 180, 412), (W // 2 + 180, 412)], fill=(72, 64, 46), width=1)
    d.polygon([(W // 2, 405), (W // 2 + 9, 412), (W // 2, 419), (W // 2 - 9, 412)], outline=GOLD)

    tracked(d, (0, 442), "EXPERIENCIA x1  ·  SIN ITEMS CUSTOM  ·  SIN REBALANCEO",
            font("segoeui.ttf", 25), TEXT, 1.6, anchor_center_x=W // 2)
    tracked(d, (0, 500), "l2jsaked.com.ar", font("georgia.ttf", 27), MUTED, 3.2, anchor_center_x=W // 2)

    # JPEG, not PNG: at 1200x630 the gradient and grain cost 180 KB as PNG and 60 KB
    # here, and every platform that reads og:image accepts it.
    path = os.path.join(OUT, "og.jpg")
    img.save(path, quality=90, optimize=True, progressive=True)
    return os.path.getsize(path)


def icons():
    # The home-screen icon needs its own background: iOS drops the alpha channel and
    # would otherwise composite the mark onto black.
    for size, name, pad in ((180, "apple-touch-icon.png", 0.16), (32, "favicon-32.png", 0.06)):
        img = vertical_gradient((size, size), (19, 21, 29), (10, 11, 15)).convert("RGBA")
        inner = round(size * (1 - pad * 2))
        mark = crest(inner, stroke_scale=1.25 if size < 64 else 1.0)
        img.paste(mark, (round(size * pad), round(size * pad)), mark)
        img.convert("RGB").save(os.path.join(OUT, name), optimize=True)


print("og.jpg bytes:", social_card())
icons()
for f in ("og.jpg", "apple-touch-icon.png", "favicon-32.png"):
    print(f, os.path.getsize(os.path.join(OUT, f)), "bytes")
