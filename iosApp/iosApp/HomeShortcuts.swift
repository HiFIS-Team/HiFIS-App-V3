import SwiftUI
import SharedKit

/// 홈 바로가기 — 하단바로 못 가는 여섯 화면
///
/// 목록은 `shared` 의 `HomeShortcut` 하나만 읽는다. 여기서 새로 세우지 않는다.
/// 안드로이드 `HomeShortcuts.kt` 와 같은 격자다 — 한쪽만 고치면 갈린다.
///
/// **머리말을 안 붙였다.** 아이콘과 글자가 스스로 무엇인지 말하고 있어서,
/// `바로가기` 한 줄을 더 얹으면 카드가 그만큼 길어지기만 한다.
struct HomeShortcuts: View {
    let onOpen: (HomeShortcut) -> Void

    /// 한 줄에 세우는 칸 수
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 3)

    var body: some View {
        LazyVGrid(columns: columns, spacing: 18) {
            ForEach(HomeShortcut.companion.all, id: \.icon) { shortcut in
                ShortcutItem(shortcut: shortcut, onOpen: onOpen)
            }
        }
        // 좌우는 카드 기본값보다 좁다 — 칸이 스스로 가운데를 잡아서
        // 24 를 주면 아이콘이 안쪽으로 지나치게 몰린다
        .padding(.horizontal, 12)
        .padding(.vertical, HifisSize.cardPadding)
        .frame(maxWidth: .infinity)
        .background(HifisColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous)
                .strokeBorder(HifisColor.line, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.04), radius: 12, y: 6)
    }
}

private struct ShortcutItem: View {
    let shortcut: HomeShortcut
    let onOpen: (HomeShortcut) -> Void

    var body: some View {
        let tint = HifisShortcutTint.of(shortcut)
        return Button { onOpen(shortcut) } label: {
            VStack(spacing: 8) {
                Image(shortcut.icon)
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: HifisSize.shortcutIcon, height: HifisSize.shortcutIcon)
                    .foregroundStyle(tint)
                    .frame(width: HifisSize.shortcutChip, height: HifisSize.shortcutChip)
                    // **면에도 색을 깐다.** 회색 네모에 아이콘만 색을 주면 한눈에는
                    // 여섯이 다 같은 상자로 보여서 색이 하는 일이 거의 없다
                    .background(
                        RoundedRectangle(
                            cornerRadius: HifisSize.shortcutChipRadius,
                            style: .continuous
                        )
                        .fill(tint.opacity(HifisShortcutTint.fillOpacity))
                    )
                Text(shortcut.label)
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkSecondary)
            }
            .frame(maxWidth: .infinity)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(shortcut.label)
    }
}
