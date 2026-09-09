#!/usr/bin/env python3
"""안드로이드 아이콘(벡터 XML) -> iOS 에셋(SVG). **안드로이드가 원본이다.**

두 플랫폼이 같은 그림을 써야 해서 iOS 쪽을 손으로 다시 그리지 않는다.
`androidApp/src/main/res/drawable/ic_*.xml` 을 고쳤으면 이걸 한 번 돌린다.

    python3 tools/icons/sync_ios_icons.py

SF Symbols 를 안 쓰는 이유 — 건물·말풍선이 안드로이드 쪽과 모양이 달라서
같은 헤더가 플랫폼마다 다른 아이콘 세트로 보인다. 색은 넣지 않고
`template-rendering-intent` 로 두어 밖에서 tint 를 준다 (안드로이드와 같은 규칙).
"""

import json
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID = "{http://schemas.android.com/apk/res/android}"
ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "androidApp/src/main/res/drawable"
DST = ROOT / "iosApp/iosApp/Assets.xcassets"


def to_svg(xml_path: Path) -> str:
    root = ET.parse(xml_path).getroot()
    parts = []
    for path in root.iter("path"):
        data = path.get(ANDROID + "pathData")
        if path.get(ANDROID + "fillColor"):
            # evenOdd = 겹치는 자리를 뚫는다 (채운 아이콘의 문·바늘·구분선)
            rule = ""
            if path.get(ANDROID + "fillType") == "evenOdd":
                rule = ' fill-rule="evenodd"'
            parts.append(f'<path d="{data}" fill="#000000"{rule}/>')
            continue
        attrs = [
            f'd="{data}"',
            'fill="none"',
            'stroke="#000000"',
            f'stroke-width="{path.get(ANDROID + "strokeWidth")}"',
        ]
        if path.get(ANDROID + "strokeLineCap"):
            attrs.append(f'stroke-linecap="{path.get(ANDROID + "strokeLineCap")}"')
        if path.get(ANDROID + "strokeLineJoin"):
            attrs.append(f'stroke-linejoin="{path.get(ANDROID + "strokeLineJoin")}"')
        parts.append("<path " + " ".join(attrs) + "/>")
    body = "\n  ".join(parts)
    return (
        '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" '
        f'viewBox="0 0 24 24">\n  {body}\n</svg>\n'
    )


def main() -> None:
    for xml_path in sorted(SRC.glob("ic_*.xml")):
        name = xml_path.stem
        out = DST / f"{name}.imageset"
        out.mkdir(parents=True, exist_ok=True)
        (out / f"{name}.svg").write_text(to_svg(xml_path))
        (out / "Contents.json").write_text(
            json.dumps(
                {
                    "images": [{"filename": f"{name}.svg", "idiom": "universal"}],
                    "info": {"author": "xcode", "version": 1},
                    "properties": {
                        "preserves-vector-representation": True,
                        "template-rendering-intent": "template",
                    },
                },
                indent=2,
            )
            + "\n"
        )
        print(f"{name} -> {out.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
