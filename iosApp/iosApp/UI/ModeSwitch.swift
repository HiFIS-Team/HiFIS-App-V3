import SwiftUI

/// 전환 스위치 — 회색 트랙 위에 **알약 하나가 미끄러진다**
///
/// 알약은 **늘 있는 하나**다. 칸마다 `if selected` 로 끼우고 빼면 옮기는 동안 글자와
/// 알약이 다시 만들어져 흐려졌다 사라졌다 한다 (대표가 봤다). 칸은 제 폭만
/// 알려 주고, 알약이 그 폭과 자리로 옮겨 간다 (240ms — V2 와 같은 빠르기).
///
/// **움직이는 것은 알약뿐이다.** 전환을 `withAnimation` 으로 감싸면 그 안에서 바뀐
/// 목록(오늘·이전 카드)까지 같이 페이드돼 안드로이드보다 느리게 보였다.
/// 애니메이션은 알약에만 건다 — 목록은 즉시 바뀐다.
///
/// **고른 칸이 굵어져도 폭이 안 변한다.** 굵은 글자와 보통 글자를 둘 다 두고
/// 투명도만 바꾼다 — 폭은 늘 굵은 것이 정하고, 글자를 다시 만들 일도 없다.
///
/// **칸 수는 정해져 있지 않다.** 둘(전체/안읽음 · 공통/개인)도 셋(제품 고르개)도 같은 부품이다.
///
/// ## 폭을 다 재기 전에는 알약을 안 그린다 (2026-09-11)
///
/// 예전에는 `matchedGeometryEffect` 로 고른 칸의 자리를 받아 썼다. 그런데 화면이
/// **새로 설 때** 첫 프레임에는 그 자리가 아직 없어서, 알약이 제 크기를 몰라
/// **트랙 전체만 하게 그려졌다 칸 크기로 줄어들었다** (대표가 봤다 — "동그라미 칸이
/// 커졌다가 줄어든다"). 제품 고르개가 셸을 갈아 끼우며 매번 새로 서는 자리라 자주 걸렸다.
///
/// 그래서 칸 폭을 직접 재서 들고 있는다. **다 재기 전에는 아예 안 그린다** —
/// 안드로이드도 같은 방식이다.
struct ModeSwitch: View {
    let segments: [String]
    @Binding var selected: Int

    /// 알약이 미끄러지는가 — **화면이 그대로 있는 자리에서만 true**
    ///
    /// 제품 고르개는 false 다. 자세한 이유는 `ProductSwitch` 에 적어 두었다.
    var slides: Bool = true

    /// 잰 칸 폭 — 차례대로. 비어 있으면 아직 못 쟀다는 뜻이다
    @State private var widths: [Int: CGFloat] = [:]

    /// 두 칸짜리 — 켜고 끄는 자리(전체/안읽음 · 공통/개인)가 쓰는 짧은 길
    ///
    /// 차례를 `Bool` 로 들고 있는 화면이 굳이 0·1 로 바꿔 부르지 않게 한다.
    init(left: String, right: String, rightSelected: Binding<Bool>) {
        self.segments = [left, right]
        self._selected = Binding(
            get: { rightSelected.wrappedValue ? 1 : 0 },
            set: { rightSelected.wrappedValue = $0 == 1 }
        )
    }

    init(segments: [String], selected: Binding<Int>, slides: Bool = true) {
        self.segments = segments
        self._selected = selected
        self.slides = slides
    }

    private var at: Int { min(max(selected, 0), segments.count - 1) }

    /// 칸 폭을 다 쟀는가 — 하나라도 모르면 알약을 안 그린다
    private var measured: Bool {
        widths.count == segments.count && widths.values.allSatisfy { $0 > 0 }
    }

    /// 알약이 설 자리 — 앞 칸들의 폭을 더한 값 (안쪽 여백만큼 밀어 준다)
    private var pillX: CGFloat {
        Self.pad + (0..<at).reduce(0) { $0 + (widths[$1] ?? 0) }
    }

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(segments.enumerated()), id: \.offset) { index, label in
                segment(label, index: index, selected: index == at) { selected = index }
            }
        }
        .padding(Self.pad)
        .background(alignment: .leading) {
            if measured {
                Capsule()
                    .fill(HifisColor.fieldFill)
                    .frame(width: widths[at] ?? 0, height: Self.height - Self.pad * 2)
                    .offset(x: pillX)
                    .animation(slides ? .easeOut(duration: Self.slide) : nil, value: at)
            }
        }
        .background(HifisColor.surface, in: Capsule())
        .onPreferenceChange(SegmentWidths.self) { widths = $0 }
    }

    private func segment(
        _ label: String,
        index: Int,
        selected: Bool,
        pick: @escaping () -> Void
    ) -> some View {
        Button(action: pick) {
            ZStack {
                Text(label)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(HifisColor.ink)
                    .opacity(selected ? 1 : 0)
                Text(label)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(HifisColor.inkSecondary)
                    .opacity(selected ? 0 : 1)
            }
            .padding(.horizontal, Self.segmentPad)
            .frame(height: Self.height - Self.pad * 2)
            // 이 칸의 폭을 알약에게 알려 준다 — 그리는 것은 없다
            .background(
                GeometryReader { proxy in
                    Color.clear.preference(key: SegmentWidths.self, value: [index: proxy.size.width])
                }
            )
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
    }

    private static let height: CGFloat = 36
    private static let pad: CGFloat = 4
    private static let segmentPad: CGFloat = 18
    /// 알약이 옮겨 가는 데 걸리는 시간 — 목록바가 도는 자리는 다 이 값이다 (V2)
    private static let slide: TimeInterval = 0.24
}

/// 칸이 제 폭을 위로 올려 보내는 통로 — 차례를 열쇠로 쓴다
private struct SegmentWidths: PreferenceKey {
    static let defaultValue: [Int: CGFloat] = [:]

    static func reduce(value: inout [Int: CGFloat], nextValue: () -> [Int: CGFloat]) {
        value.merge(nextValue()) { _, new in new }
    }
}
