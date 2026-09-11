import SwiftUI

/// 전환 스위치 — 회색 트랙 위에 **알약 하나가 미끄러진다**
///
/// 알약은 **늘 있는 하나**다. 칸마다 `if selected` 로 끼우고 빼면 옮기는 동안 글자와
/// 알약이 다시 만들어져 흐려졌다 사라졌다 한다 (대표가 봤다). 칸은 제 자리만
/// 알려 주고(`isSource`), 알약이 고른 칸의 자리로 옮겨 간다 (240ms — V2 와 같은 빠르기).
///
/// **움직이는 것은 알약뿐이다.** 전환을 `withAnimation` 으로 감싸면 그 안에서 바뀐
/// 목록(오늘·이전 카드)까지 같이 페이드돼 안드로이드보다 느리게 보였다.
/// 애니메이션은 알약에만 건다 — 목록은 즉시 바뀐다.
///
/// **고른 칸이 굵어져도 폭이 안 변한다.** 굵은 글자와 보통 글자를 둘 다 두고
/// 투명도만 바꾼다 — 폭은 늘 굵은 것이 정하고, 글자를 다시 만들 일도 없다.
///
/// **칸 수는 정해져 있지 않다.** 둘(전체/안읽음 · 공통/개인)도 셋(제품 고르개)도 같은 부품이다.
struct ModeSwitch: View {
    let segments: [String]
    @Binding var selected: Int

    @Namespace private var pill

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

    init(segments: [String], selected: Binding<Int>) {
        self.segments = segments
        self._selected = selected
    }

    private var at: Int { min(max(selected, 0), segments.count - 1) }

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(segments.enumerated()), id: \.offset) { index, label in
                segment(label, index: index, selected: index == at) { selected = index }
            }
        }
        .padding(Self.pad)
        // 알약 — 고른 칸(`isSource`)의 자리를 받아 그리로 옮겨 간다
        .background {
            Capsule()
                .fill(HifisColor.fieldFill)
                .matchedGeometryEffect(id: at, in: pill, isSource: false)
                .animation(.easeOut(duration: Self.slide), value: at)
        }
        .background(HifisColor.surface, in: Capsule())
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
            // 이 칸의 자리를 알약에게 알려 준다 — 그리는 것은 없다
            .background(Color.clear.matchedGeometryEffect(id: index, in: pill, isSource: true))
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
