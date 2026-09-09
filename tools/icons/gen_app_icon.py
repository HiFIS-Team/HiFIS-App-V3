"""앱 아이콘 한 벌을 **로고 하나에서** 굽는다.

    python3 tools/icons/gen_app_icon.py

원본은 `assets/brand/logo.png` — 흰 배경에 청록→보라 그라데이션 FS 마크다.
**원본의 여백 비율을 그대로 쓴다.** 마크만 떼어 다시 앉히면 원본과 다른 아이콘이 된다.

MyFIS 의 같은 이름 스크립트와 다른 점은 **배경이 검정이 아니라 흰색**이라는 것뿐이다.
검정 배경이면 픽셀값이 곧 `색 × 알파` 라 밝기를 알파로 쓸 수 있지만, 흰 배경에서는
그 방법이 안 통한다 — 여기서는 **배경에서 얼마나 멀어졌나**를 알파로 삼고
주변의 제 색으로 정규화한다.
"""
import os
import sys
from PIL import Image, ImageChops, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
SRC = sys.argv[1] if len(sys.argv) > 1 else f"{ROOT}/assets/brand/logo.png"

ANDROID = f"{ROOT}/androidApp/src/main/res"
IOS_ICON = f"{ROOT}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon.png"

# 런처 아이콘 밀도별 배수 — 레거시(48dp) / 어댑티브 레이어(108dp)
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}

# 어댑티브 아이콘은 108dp 레이어 중 **가운데 72dp 만** 보인다. 바깥 18dp 는 마스크가 먹는다.
# 로고 캔버스를 레이어에 꽉 채우면 마크가 보이는 영역의 90% 가 되어 **원형 마스크에서 잘린다.**
# 캔버스를 72dp 자리에 앉히고 나머지를 투명 여백으로 두면, 잘리지 않으면서
# iOS 아이콘과 여백 비율도 같아진다.
ADAPTIVE_INSET = 72 / 108


def extract(src):
    """흰 배경 위 그림에서 마크를 **알파와 함께** 떼어낸다.

    배경색과의 거리를 알파로 잡되, **주변의 가장 진한 값으로 나눠 정규화**한다.
    그냥 거리를 쓰면 밝은 청록 구간이 보라 구간보다 옅게 잡혀 마크가 얼룩진다.
    """
    im = src.convert("RGB")
    bg = im.getpixel((0, 0))
    dist = ImageChops.difference(im, Image.new("RGB", im.size, bg)).convert("L")
    full = dist.filter(ImageFilter.MaxFilter(9))    # 주변에서 가장 진한 값 = 알파 1 인 자리

    out = Image.new("RGBA", im.size)
    px, dp, fp, op = im.load(), dist.load(), full.load(), out.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            f = fp[x, y]
            if f < 40:                              # 마크에서 먼 순수 배경
                continue
            a = dp[x, y] / f
            if a <= 0.002:
                continue
            a = min(1.0, a)
            # 흰 배경과 섞이기 전의 색을 되돌린다: p = c·a + bg·(1-a)
            cr, cg, cb = px[x, y]
            c = tuple(
                max(0, min(255, round((v - b * (1 - a)) / a)))
                for v, b in ((cr, bg[0]), (cg, bg[1]), (cb, bg[2]))
            )
            op[x, y] = (*c, round(a * 255))
    return out, bg


def circle_mask(size):
    # 4배로 그리고 줄여서 계단을 없앤다
    m = Image.new("L", (size * 4, size * 4), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size * 4 - 1, size * 4 - 1), fill=255)
    return m.resize((size, size), Image.LANCZOS)


def main():
    src = Image.open(SRC)
    mark, bg = extract(src)

    # ── iOS — 1024 **불투명**. iOS 는 알파를 못 쓴다
    flat = Image.new("RGB", src.size, bg)
    flat.paste(mark, (0, 0), mark)
    os.makedirs(os.path.dirname(IOS_ICON), exist_ok=True)
    flat.resize((1024, 1024), Image.LANCZOS).save(IOS_ICON)

    # ── Android
    # 모노크롬(테마 아이콘)은 시스템이 색을 입히므로 실루엣만 있으면 된다
    mono = Image.new("RGBA", src.size, (255, 255, 255, 255))
    mono.putalpha(mark.getchannel("A"))

    for name, scale in DENSITIES.items():
        d = f"{ANDROID}/mipmap-{name}"
        os.makedirs(d, exist_ok=True)
        legacy, layer = int(48 * scale), int(108 * scale)

        # 어댑티브 레이어 — 배경은 `@color/ic_launcher_background` 가 따로 깐다
        inner = max(1, round(layer * ADAPTIVE_INSET))
        pad = (layer - inner) // 2
        for img, fname in ((mark, "ic_launcher_foreground"), (mono, "ic_launcher_monochrome")):
            canvas = Image.new("RGBA", (layer, layer), (0, 0, 0, 0))
            canvas.paste(img.resize((inner, inner), Image.LANCZOS), (pad, pad))
            canvas.save(f"{d}/{fname}.png")

        # 레거시 — 배경까지 그려 넣는다
        sq = flat.resize((legacy, legacy), Image.LANCZOS).convert("RGBA")
        sq.save(f"{d}/ic_launcher.png")
        rnd = sq.copy()
        rnd.putalpha(circle_mask(legacy))
        rnd.save(f"{d}/ic_launcher_round.png")

    # ── 마크만 필요한 곳을 위해 배경 뺀 벌도 같이 둔다
    mark.crop(mark.getbbox()).save(f"{ROOT}/assets/brand/logo-mark.png")

    box = mark.getchannel("A").point(lambda v: 255 if v > 127 else 0).getbbox()
    ratio = (box[2] - box[0]) / src.size[0]
    print("구웠다 — 마크가 캔버스의 %.1f%% (원본 비율 그대로)" % (ratio * 100))
    print("        어댑티브 레이어에서는 %.1f%%, 마스크에 보이는 영역 기준 %.1f%%"
          % (ratio * ADAPTIVE_INSET * 100, ratio * 100))


main()
