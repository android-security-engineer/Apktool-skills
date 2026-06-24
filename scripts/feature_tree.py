#!/usr/bin/env python3
"""Render the AI-Apktool feature tree as a PNG (zero deps beyond Pillow).

A left-to-right tree:  root -> 8 capability branches -> detail leaves.
We lay it out by hand and draw with Pillow, supersampling 2x for crisp text
and antialiased rounded boxes / Bezier connectors.

Usage:
    python3 scripts/feature_tree.py            # English -> docs/assets/feature-tree.png
    python3 scripts/feature_tree.py --lang zh  # Chinese -> docs/assets/feature-tree.zh.png
    python3 scripts/feature_tree.py --lang en out.png
"""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

# Accent colors are shared across languages; only the labels differ.
COLORS = ["#5B8DEF", "#22A06B", "#E8A33D", "#9B5DE5",
          "#EF5DA8", "#15AABF", "#8D99AE", "#E4572E"]

# --------------------------------------------------------------------------
# Feature tree data per language: (branch title, [leaf detail lines]).
# Leaf text may contain "\n" for an explicit second line.
# --------------------------------------------------------------------------
DATA = {
    "en": {
        "root": "AI-Apktool",
        "root_sub": "51 cmds · 11 skills",
        "subtitle": "AI-native Android reverse engineering — 51 commands, 11 skills",
        "tree": [
            ("Core · from Apktool", [
                "decode / build",
                "framework: install / clean / list",
                "publicize-resources",
            ]),
            ("Analysis · 38 cmds · JSON", [
                "Metadata — info · analyze · manifest · sdk-info · file-hash",
                "Components — activities · services · receivers\nproviders · api-surface",
                "Permissions — permissions · permission-detail",
                "Security — security 0-100 · signing · manifest-flags",
                "DEX & Code — class · method · field\ninheritance · structure · dex-strings",
                "Resources & Files — resources · locales\nnative-libs · assets · file-list / file-hash",
                "Compare — diff (two APKs)",
            ]),
            ("Search", ["strings · classes · methods  (regex)"]),
            ("Scripting", ["run · pipe — one shared parse, many commands"]),
            ("AI Interface", [
                "ai — explain / security-review\nsummarize / context (facts JSON)",
            ]),
            ("Service", ["serve — HTTP REST · /api/v1/* endpoints"]),
            ("Discovery", ["help --format=json  (machine-readable catalog)"]),
            ("Claude Code Skills · 11", [
                "quick-analysis · security-audit · compare\nreverse · reference · decode-build",
                "dex-deep-dive · network-analysis · malware-hunt\nresource-explorer · signing-verify",
            ]),
        ],
    },
    "zh": {
        "root": "AI-Apktool",
        "root_sub": "51 命令 · 11 Skills",
        "subtitle": "AI 原生 Android 逆向工程平台 —— 51 个命令，11 个 Skills",
        "tree": [
            ("核心 · 源自 Apktool", [
                "decode / build 解码与构建",
                "框架管理：install / clean / list",
                "publicize-resources",
            ]),
            ("分析 · 38 命令 · JSON", [
                "元数据 — info · analyze · manifest · sdk-info · file-hash",
                "组件 — activities · services · receivers\nproviders · api-surface",
                "权限 — permissions · permission-detail",
                "安全 — security 0-100 · signing · manifest-flags",
                "DEX 与代码 — class · method · field\ninheritance · structure · dex-strings",
                "资源与文件 — resources · locales\nnative-libs · assets · file-list / file-hash",
                "对比 — diff（两个 APK）",
            ]),
            ("搜索", ["strings 字符串 · classes 类 · methods 方法（正则）"]),
            ("脚本", ["run · pipe —— 一次解析，多条命令"]),
            ("AI 接口", [
                "ai —— explain / security-review\nsummarize / context（结构化事实 JSON）",
            ]),
            ("服务", ["serve —— HTTP REST · /api/v1/* 端点"]),
            ("能力发现", ["help --format=json（机器可读目录）"]),
            ("Claude Code Skills · 11", [
                "快速分诊 · 安全审计 · 版本对比\n逆向 · 命令参考 · 解码构建",
                "DEX 深析 · 网络分析 · 恶意软件狩猎\n资源探索 · 签名验证",
            ]),
        ],
    },
}

# --------------------------------------------------------------------------
# Style (logical px; multiplied by SCALE for the supersampled canvas)
# --------------------------------------------------------------------------
SCALE = 2
BG = "#0D1117"          # GitHub dark canvas
INK = "#E6EDF3"         # primary text
INK_DIM = "#9DA7B3"     # subtitle / leaf text
ROOT_FILL = "#1F6FEB"
LEAF_FILL = "#161B22"
LEAF_BORDER = "#30363D"

MARGIN = 36
COL_GAP = 64            # horizontal gap between columns
LEAF_GAP = 12           # vertical gap between leaves
BRANCH_GAP = 16         # extra vertical gap between branches
PAD_X, PAD_Y = 16, 11   # text padding inside a box
RADIUS = 12

ROOT_W = 190
CAT_W = 230
LEAF_W = 470
LINE_SP = 5

# Per-language fonts: Latin uses DejaVu; Chinese needs a CJK face (Noto Sans CJK).
# .ttc faces are loaded by index — Noto's SC face is what we want.
_DEJAVU = "/usr/share/fonts/truetype/dejavu"
_NOTO = "/usr/share/fonts/opentype/noto"
FONTS = {
    "en": {
        "bold": (f"{_DEJAVU}/DejaVuSans-Bold.ttf", 0),
        "regular": (f"{_DEJAVU}/DejaVuSans.ttf", 0),
    },
    "zh": {
        "bold": (f"{_NOTO}/NotoSansCJK-Bold.ttc", 0),
        "regular": (f"{_NOTO}/NotoSansCJK-Regular.ttc", 0),
    },
}


def font(spec, size):
    path, index = spec
    return ImageFont.truetype(path, size * SCALE, index=index)


def s(v):
    return v * SCALE


# Measurement uses a throwaway 1x1 draw context.
_md = ImageDraw.Draw(Image.new("RGB", (1, 1)))


def text_size(txt, fnt):
    w = h = 0
    lines = txt.split("\n")
    for ln in lines:
        bb = _md.textbbox((0, 0), ln, font=fnt)
        w = max(w, bb[2] - bb[0])
        h = max(h, bb[3] - bb[1])
    total_h = h * len(lines) + s(LINE_SP) * (len(lines) - 1)
    return w, total_h, h, lines


def draw_box(d, x, y, w, h, fill, border, radius):
    d.rounded_rectangle([x, y, x + w, y + h], radius=radius,
                        fill=fill, outline=border, width=max(1, SCALE))


def draw_text_block(d, cx, cy, txt, fnt, color):
    """Draw possibly multi-line text centered on (cx, cy)."""
    _, total_h, line_h, lines = text_size(txt, fnt)
    y = cy - total_h / 2
    for ln in lines:
        bb = d.textbbox((0, 0), ln, font=fnt)
        lw = bb[2] - bb[0]
        d.text((cx - lw / 2, y - bb[1]), ln, font=fnt, fill=color)
        y += line_h + s(LINE_SP)


def bezier(d, p0, p3, color, width):
    """Cubic Bezier with horizontal control handles, sampled as a polyline."""
    x0, y0 = p0
    x3, y3 = p3
    dx = (x3 - x0) * 0.5
    x1, y1 = x0 + dx, y0
    x2, y2 = x3 - dx, y3
    pts = []
    N = 24
    for i in range(N + 1):
        t = i / N
        mt = 1 - t
        x = mt**3 * x0 + 3 * mt**2 * t * x1 + 3 * mt * t**2 * x2 + t**3 * x3
        y = mt**3 * y0 + 3 * mt**2 * t * y1 + 3 * mt * t**2 * y2 + t**3 * y3
        pts.append((x, y))
    d.line(pts, fill=color, width=width, joint="curve")


def parse_args(argv):
    lang, out = "en", None
    i = 0
    while i < len(argv):
        a = argv[i]
        if a == "--lang":
            lang = argv[i + 1]
            i += 2
        else:
            out = Path(a)
            i += 1
    if out is None:
        base = Path(__file__).resolve().parent.parent / "docs/assets"
        out = base / ("feature-tree.png" if lang == "en" else f"feature-tree.{lang}.png")
    return lang, out


def main():
    lang, out = parse_args(sys.argv[1:])
    data = DATA[lang]
    fset = FONTS[lang]
    out.parent.mkdir(parents=True, exist_ok=True)

    f_root = font(fset["bold"], 26)
    f_sub = font(fset["regular"], 13)
    f_cat = font(fset["bold"], 17)
    f_leaf = font(fset["regular"], 14)

    tree = [(title, COLORS[i % len(COLORS)], leaves)
            for i, (title, leaves) in enumerate(data["tree"])]

    # ---- First pass: measure leaf heights and assign vertical centers -----
    y = s(MARGIN)
    branches = []  # list of dicts with cat + leaf geometry
    for title, color, leaves in tree:
        leaf_geo = []
        for leaf in leaves:
            _, th, _, _ = text_size(leaf, f_leaf)
            h = th + s(PAD_Y) * 2
            cy = y + h / 2
            leaf_geo.append({"text": leaf, "y": y, "h": h, "cy": cy})
            y += h + s(LEAF_GAP)
        cat_cy = (leaf_geo[0]["cy"] + leaf_geo[-1]["cy"]) / 2
        branches.append({"title": title, "color": color,
                         "leaves": leaf_geo, "cat_cy": cat_cy})
        y += s(BRANCH_GAP)

    canvas_h = int(y - s(LEAF_GAP) - s(BRANCH_GAP) + s(MARGIN))

    # ---- Column x positions ----------------------------------------------
    x_root = s(MARGIN)
    x_cat = x_root + s(ROOT_W) + s(COL_GAP)
    x_leaf = x_cat + s(CAT_W) + s(COL_GAP)
    canvas_w = int(x_leaf + s(LEAF_W) + s(MARGIN))

    root_cy = (branches[0]["cat_cy"] + branches[-1]["cat_cy"]) / 2

    # ---- Render -----------------------------------------------------------
    img = Image.new("RGB", (canvas_w, canvas_h), BG)
    d = ImageDraw.Draw(img)

    # connectors first (so boxes sit on top)
    for b in branches:
        col = b["color"]
        # root -> category
        bezier(d, (x_root + s(ROOT_W), root_cy),
               (x_cat, b["cat_cy"]), col, max(2, SCALE))
        # category -> each leaf
        for lf in b["leaves"]:
            bezier(d, (x_cat + s(CAT_W), b["cat_cy"]),
                   (x_leaf, lf["cy"]), col, max(2, SCALE))

    # root node
    root_h = s(64)
    draw_box(d, x_root, root_cy - root_h / 2, s(ROOT_W), root_h,
             ROOT_FILL, ROOT_FILL, s(RADIUS))
    draw_text_block(d, x_root + s(ROOT_W) / 2, root_cy - s(9), data["root"], f_root, "#FFFFFF")
    draw_text_block(d, x_root + s(ROOT_W) / 2, root_cy + s(15), data["root_sub"],
                    f_sub, "#CFE0FF")

    # category + leaf nodes
    for b in branches:
        col = b["color"]
        cat_h = s(46)
        cat_y = b["cat_cy"] - cat_h / 2
        draw_box(d, x_cat, cat_y, s(CAT_W), cat_h, col, col, s(RADIUS))
        draw_text_block(d, x_cat + s(CAT_W) / 2, b["cat_cy"], b["title"],
                        f_cat, "#FFFFFF")
        for lf in b["leaves"]:
            draw_box(d, x_leaf, lf["y"], s(LEAF_W), lf["h"],
                     LEAF_FILL, col, s(RADIUS))
            draw_text_block(d, x_leaf + s(LEAF_W) / 2, lf["cy"], lf["text"],
                            f_leaf, INK)

    # subtitle banner at very top-left of root column
    d.text((x_root, s(MARGIN) - s(20)), data["subtitle"], font=f_sub, fill=INK_DIM)

    # ---- Downsample for antialiasing -------------------------------------
    final = img.resize((canvas_w // SCALE, canvas_h // SCALE), Image.LANCZOS)
    final.save(out)
    print(f"wrote {out}  ({final.width}x{final.height})")


if __name__ == "__main__":
    main()
