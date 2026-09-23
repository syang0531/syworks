"""Block textures for the machines and the incinerator.

Built by recolouring the vanilla furnace textures, so they read as believable furnace-family
blocks, then adding a distinct accent and glow:

    Ore Roaster (광석 화덕)   furnace body, steel blue, CYAN heat glow
    Crusher (분쇄기)          blast-furnace metal, slate grey, AMBER grinding glow
    Charcoal Kiln (숯가마)    furnace body, fired clay with dark bands, EMBER glow
    Incinerator (소각로)      blast-furnace metal, soot black, RED fire

Each block gets four faces: _side, _top, _front and _front_on (lit).

The blast furnace's lit front is animated: two 16x16 frames stacked into a 16x32 strip, whose only
difference is the top of the fire bars. Our glow paints over exactly those pixels, so instead the
second frame gets a dimmer glow, and a .mcmeta like vanilla's makes it pulse. Up to 1.1.0 the
strip went out with no .mcmeta and the game squashed both frames into one face.

    python tools/gen_block_textures.py
    python tools/gen_block_textures.py --out some/dir   # write elsewhere, e.g. to compare

Needs Pillow. Ported from gen_block_textures.ps1 (System.Drawing). A file whose pixels would not
change is left alone, so re-running does not churn the committed PNGs.
"""

import argparse
import json
import math
import zipfile
from pathlib import Path

from PIL import Image

TOOLS = Path(__file__).resolve().parent
ROOT = TOOLS.parent
VBASE = TOOLS / "vanilla_base"
DEFAULT_OUT = ROOT / "src/main/resources/assets/syworks/textures/block"

FRAME = 16
# How far toward the edge colour the second frame's glow core sinks. The animation interpolates,
# so the glow breathes between full and this.
DIM = 0.4
# Vanilla's blast_furnace_front_on.png.mcmeta.
ANIMATION = {"animation": {"interpolate": True, "frametime": 10}}

BASES = ["furnace_side", "furnace_top", "furnace_front", "furnace_front_on",
         "blast_furnace_side", "blast_furnace_top", "blast_furnace_front", "blast_furnace_front_on"]


def ensure_furnace_bases():
    """Copies the vanilla furnace faces out of the client jar the first time they are needed.

    Takes the jar from build/moddev/artifacts because its name carries the exact Minecraft version;
    the Gradle caches hold jars of several versions side by side.
    """
    missing = [n for n in BASES if not (VBASE / f"{n}.png").exists()]
    if not missing:
        return
    jars = [j for j in (ROOT / "build/moddev/artifacts").glob("minecraft-patched-*.jar")
            if "sources" not in j.name and "merged" not in j.name]
    if not jars:
        raise SystemExit("Vanilla client jar not found. Run a gradle task once.")
    VBASE.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(jars[0]) as jar:
        for n in missing:
            (VBASE / f"{n}.png").write_bytes(jar.read(f"assets/minecraft/textures/block/{n}.png"))


def rgb(hex_):
    return tuple(int(hex_[i:i + 2], 16) for i in (0, 2, 4))


def load(name):
    return Image.open(VBASE / f"{name}.png").convert("RGBA")


# Every [int] cast in the PowerShell original rounds half to even, as Python's round() does.
# int() would truncate and shift some pixels by one.

def recolor(src, tint_hex, base, keep_colored):
    """Recolours the grey (stone/metal) pixels toward the tint by luminance. With keep_colored,
    strongly coloured pixels (the furnace's orange fire) are left as they are."""
    t = rgb(tint_hex)
    dst = Image.new("RGBA", src.size)
    sp, dp = src.load(), dst.load()
    for y in range(src.height):
        for x in range(src.width):
            r, g, b, a = sp[x, y]
            if a == 0:
                dp[x, y] = (0, 0, 0, 0)
                continue
            if keep_colored and max(r, g, b) - min(r, g, b) > 40:
                dp[x, y] = (r, g, b, a)
                continue
            f = (r * 0.3 + g * 0.59 + b * 0.11) / base
            dp[x, y] = (min(255, round(t[0] * f)), min(255, round(t[1] * f)), min(255, round(t[2] * f)), a)
    return dst


# oy below is the top of the animation frame being painted: 0, 16, ...

def add_rivets(img, hex_, oy=0):
    """Four corner rivet pixels."""
    c = rgb(hex_) + (255,)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        img.putpixel((x, oy + y), c)


def add_band(img, hex_, y, oy=0):
    """A thin accent status band across the upper part of a face."""
    c = rgb(hex_) + (255,)
    for x in range(2, 14):
        img.putpixel((x, oy + y), c)


def add_glow(img, core_hex, edge_hex, oy=0, dim=0.0):
    """Fills the lower opening with a radial glow, bright core fading to the edge colour.
    dim sinks the core that far toward the edge colour."""
    edge = rgb(edge_hex)
    core = tuple(c + (e - c) * dim for c, e in zip(rgb(core_hex), edge))
    cx, cy = 7.5, 11.0
    for y in range(8, 14):
        for x in range(3, 13):
            d = math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy))
            if d > 4.2:
                continue
            t = max(0.0, 1.0 - d / 4.2)
            img.putpixel((x, oy + y), tuple(round(e + (c - e) * t) for c, e in zip(core, edge)) + (255,))


def save(img, path):
    """Writes the PNG unless the file already holds exactly these pixels."""
    if path.exists():
        with Image.open(path) as old:
            if old.size == img.size and old.convert("RGBA").tobytes() == img.tobytes():
                return
    img.save(path)


def machine(out, name, body, tint, accent, base, core, edge):
    """The four faces of one block. body is 'furnace' or 'blast_furnace'.

    The lit front keeps the vanilla fire pixels only on the blast furnace: the plain furnace's lit
    front is recoloured whole and the glow painted over it."""
    top = recolor(load(f"{body}_top"), tint, base, False)
    add_rivets(top, accent)
    save(top, out / f"{name}_top.png")

    for face, src, glow in (("side", f"{body}_side", False),
                            ("front", f"{body}_front", False),
                            ("front_on", f"{body}_front_on" if body == "blast_furnace" else f"{body}_front", True)):
        img = recolor(load(src), tint, base, glow and body == "blast_furnace")
        frames = img.height // FRAME
        for i in range(frames):
            add_rivets(img, accent, i * FRAME)
            add_band(img, accent, 3, i * FRAME)
            if glow:
                add_glow(img, core, edge, i * FRAME, DIM if i else 0.0)
        path = out / f"{name}_{face}.png"
        save(img, path)
        meta = path.with_name(path.name + ".mcmeta")
        if frames > 1:
            meta.write_text(json.dumps(ANIMATION, indent=2) + "\n", encoding="utf-8")
        elif meta.exists():
            meta.unlink()


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    out = parser.parse_args().out
    out.mkdir(parents=True, exist_ok=True)
    ensure_furnace_bases()

    #       name             body             tint      accent    base   glow core  glow edge
    machine(out, "ore_roaster",   "furnace",       "5E7488", "34C7E0", 150.0, "EAFDFF", "1E9FC0")
    # The blast furnace so it reads as a heavier, banded machine than the roaster.
    machine(out, "crusher",       "blast_furnace", "6A6E78", "D8A33A", 140.0, "FFE9B0", "C4761A")
    # Earthy terracotta rather than metal: a real charcoal kiln is a clay-sealed mound.
    machine(out, "charcoal_kiln", "furnace",       "A8613E", "2E241E", 150.0, "FFD9A0", "B33A12")
    # Not a machine, but it stands next to them. Blackened as if it had been burning rubbish for
    # years, and a glow redder than any machine's — it only destroys.
    machine(out, "incinerator",   "blast_furnace", "3C3836", "B8382A", 140.0, "FFE08A", "D2361A")

    print(f"Generated 16 block textures in {out}")


if __name__ == "__main__":
    main()
