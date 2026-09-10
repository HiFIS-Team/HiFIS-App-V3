import SwiftUI
import SharedKit

/// 전체 — **앱의 전수 명단**
///
/// 하단바 다섯 칸과 홈 바로가기 여섯 개로는 화면을 다 못 담는다. 여기가 마지막
/// 그물이라 **어느 화면도 이 목록에서 새어 나가면 안 된다.** 명단은 `shared` 의
/// `MoreRow` 하나뿐이고 `MoreRowTest` 가 빠진 것을 잡는다.
///
/// **판으로 싸지 않는다.** 흐린 머리말 하나와 그 아래 줄들이 전부다 —
/// 목록을 훑는 화면이라 칸을 그리면 줄마다 테두리를 읽게 된다.
/// 그래서 묶음을 가르는 것은 `moreGroupGap` 여백 하나뿐이고,
/// 줄 사이에는 구분선도 꺾쇠도 없다.
///
/// **줄에 색을 안 쓴다.** 홈 바로가기 여섯 색은 그 격자 전용이다 —
/// 여기까지 번지면 브랜드 파랑 하나로 강조하던 규칙이 무너진다.
struct MoreView: View {
    /// 하단바에 자리가 있는 화면을 눌렀을 때 — **탭을 옮긴다.** 화면을 새로 쌓지 않는다
    var onTab: (MainTab) -> Void = { _ in }

    var body: some View {
        TabPage {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    // 제목도 같이 굴러간다 — 붙어 있는 것은 헤더(아이콘 줄)뿐이다
                    ScreenTitle("전체")

                    ForEach(Array(MoreGroup.companion.all.enumerated()), id: \.offset) { _, group in
                        MenuGroup(group: group, onTab: onTab)
                    }
                }
                .padding(.bottom, 24)
            }
        }
    }
}

private struct MenuGroup: View {
    let group: MoreGroup
    let onTab: (MainTab) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Spacer().frame(height: HifisSize.moreGroupGap)

            Text(group.title)
                .font(.system(size: 13, weight: .medium))
                .foregroundStyle(HifisColor.inkTertiary)
                .padding(.horizontal, HifisSize.screenEdge)

            Spacer().frame(height: HifisSize.moreGroupTitleGap)

            ForEach(Array(MoreRow.companion.of(group: group).enumerated()), id: \.offset) { _, row in
                MenuRow(row: row, onTab: onTab)
            }
        }
    }
}

private struct MenuRow: View {
    let row: MoreRow
    let onTab: (MainTab) -> Void

    var body: some View {
        Button {
            // 하단바에 자리가 있는 화면만 갈 곳이 있다.
            // 나머지는 아직 화면이 없어서 눌러도 할 일이 없다
            if let tab = row.tab { onTab(tab) }
        } label: {
            HStack(spacing: 14) {
                Image(row.icon)
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: HifisSize.moreIcon, height: HifisSize.moreIcon)
                    .foregroundStyle(HifisColor.ink)

                // 안드로이드 `HifisType.body` 와 같은 값이다
                Text(row.label)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(HifisColor.ink)

                Spacer(minLength: 0)
            }
            .padding(.horizontal, HifisSize.screenEdge)
            .frame(height: HifisSize.moreRow)
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
    }
}
