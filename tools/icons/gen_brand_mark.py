#!/usr/bin/env python3
"""FS 마크를 iOS 에셋으로 굽는다 — **손으로 안 만든다**

앱 아이콘(`gen_app_icon.py`)과 같은 방식이다. 원본은 `assets/brand/logo-mark.png`
하나뿐이고, 로고가 바뀌면 이 스크립트를 한 번 돌리면 된다.

`sync_ios_icons.py` 와는 하는 일이 다르다 —
저쪽은 **한 가지 색으로 칠할 선 아이콘**(template)을 벡터로 옮기고,
이쪽은 **제 색(그라데이션)을 그대로 지니는 그림**을 담는다.
그래서 `template-rendering-intent` 를 안 붙인다. 붙이면 그라데이션이 뭉개진다.

    python3 tools/icons/gen_brand_mark.py
"""

import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "assets" / "brand" / "logo-mark.png"
IMAGESET = ROOT / "iosApp" / "iosApp" / "Assets.xcassets" / "brand_mark.imageset"

# 1x 로 그릴 폭(pt). `HifisSize.aiChatMark` 와 같은 값이어야 흐리게 안 나온다.
# 높이는 원본 비율을 따라간다 — 마크가 가로로 길어서 폭을 기준으로 잡는다.
BASE_WIDTH = 36


def main() -> None:
    source = Image.open(SOURCE).convert("RGBA")
    ratio = source.height / source.width

    IMAGESET.mkdir(parents=True, exist_ok=True)
    images = []
    for scale in (1, 2, 3):
        width = BASE_WIDTH * scale
        height = round(width * ratio)
        name = f"brand_mark@{scale}x.png"
        source.resize((width, height), Image.LANCZOS).save(IMAGESET / name)
        images.append({"filename": name, "idiom": "universal", "scale": f"{scale}x"})
        print(f"  {name}  {width}x{height}")

    contents = {
        "images": images,
        "info": {"author": "xcode", "version": 1},
        # template 이 아니다 — 마크가 제 그라데이션을 그대로 지녀야 한다
        "properties": {"template-rendering-intent": "original"},
    }
    (IMAGESET / "Contents.json").write_text(
        json.dumps(contents, indent=2, ensure_ascii=False) + "\n"
    )
    print(f"→ {IMAGESET.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
