"""The CurseForge project logo, built from the machine block textures.

Output: docs/curseforge/logo.png (512x512). Re-run after changing the hero textures.

    python tools/gen_logo.py
    python tools/gen_logo.py --out some/logo.png

Needs Pillow and numpy. Ported from gen_logo.ps1 (System.Drawing). Unlike the block textures this
is not pixel-identical to the old output: GDI+ antialiases the gradient edges and rotated images
its own way. It is the same picture — same layout, colours and glows.
"""

import argparse
import math
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
TEXTURES = ROOT / "src/main/resources/assets/syworks/textures/block"
DEFAULT_OUT = ROOT / "docs/curseforge/logo.png"

SIZE = 512

# Pixel centres, shared by every pass.
_ys, _xs = np.mgrid[0:SIZE, 0:SIZE].astype(np.float64) + 0.5


def blend(canvas, rgb, alpha):
    """Source-over of a flat colour with a per-pixel alpha (0..1) onto an RGB float canvas."""
    a = alpha[..., None]
    canvas[:] = canvas * (1.0 - a) + np.asarray(rgb, dtype=np.float64) * a


def radial(canvas, cx, cy, radius, rgb, centre_alpha, edge_alpha=0):
    """A circular gradient: alpha runs linearly from the centre to the rim, nothing outside it.
    What GDI+'s PathGradientBrush on an ellipse does."""
    t = np.hypot(_xs - cx, _ys - cy) / radius
    alpha = np.where(t <= 1.0, (centre_alpha + (edge_alpha - centre_alpha) * t) / 255.0, 0.0)
    blend(canvas, rgb, alpha)


def draw_texture(canvas, name, x, y, size, angle_deg):
    """Stretches a texture over a size x size square at (x, y), turned clockwise by angle_deg about
    its centre, sampled nearest-neighbour so the pixels stay crisp."""
    tex = np.asarray(Image.open(TEXTURES / name).convert("RGBA"), dtype=np.float64)
    th, tw = tex.shape[:2]
    cx, cy = x + size / 2, y + size / 2
    a = math.radians(angle_deg)
    # Undo the rotation to find where each canvas pixel falls on the unrotated square.
    dx, dy = _xs - cx, _ys - cy
    u = dx * math.cos(a) + dy * math.sin(a) + size / 2
    v = -dx * math.sin(a) + dy * math.cos(a) + size / 2
    inside = (u >= 0) & (u < size) & (v >= 0) & (v < size)
    tx = np.clip((u * tw / size).astype(int), 0, tw - 1)
    ty = np.clip((v * th / size).astype(int), 0, th - 1)
    src = tex[ty, tx]
    alpha = np.where(inside, src[..., 3] / 255.0, 0.0)[..., None]
    canvas[:] = canvas * (1.0 - alpha) + src[..., :3] * alpha


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    out = parser.parse_args().out

    canvas = np.empty((SIZE, SIZE, 3), dtype=np.float64)
    canvas[:] = (12, 14, 20)

    # Slate ambient, then a cyan intake glow behind the hero.
    radial(canvas, 256, 250, 320, (46, 60, 92), 255)
    radial(canvas, 256, 210, 190, (52, 116, 140), 110)

    # The three machines, all lit.
    draw_texture(canvas, "charcoal_kiln_front_on.png", 48, 48, 170, -12)   # behind, left
    draw_texture(canvas, "ore_roaster_front_on.png", 296, 56, 170, 12)     # behind, right
    draw_texture(canvas, "crusher_front_on.png", 136, 168, 240, 0)         # hero, front and centre

    # A soft vignette to pull the eye to the middle.
    radial(canvas, SIZE / 2, SIZE / 2, SIZE / 2 + 90, (0, 0, 0), 0, 150)

    out.parent.mkdir(parents=True, exist_ok=True)
    Image.fromarray(np.clip(np.rint(canvas), 0, 255).astype(np.uint8), "RGB").save(out)
    print(f"wrote {out}")


if __name__ == "__main__":
    main()
