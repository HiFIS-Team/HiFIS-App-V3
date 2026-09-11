import SwiftUI

/// 헤더에 서는 아이콘 버튼 — 테두리도 배경도 없다
///
/// **터치 자리와 그림 크기가 다르다** (44 / 22). 손가락이 닿는 넓이는 44 로 두고
/// 보이는 것만 22 다. 안드로이드 `HeaderIconButton` 과 같은 값을 쓴다.
///
/// 아이콘은 SF Symbols 가 아니라 안드로이드에서 변환해 온 에셋이다
/// (`tools/icons/sync_ios_icons.py`). 두 플랫폼이 같은 그림이어야 한다.
struct HeaderIconButton: View {
    @Environment(\.brand) private var brand
    let icon: String
    let label: String
    var active: Bool = false
    var badge: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(icon)
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                .foregroundStyle(active ? brand : HifisColor.ink)
                .overlay(alignment: .topTrailing) {
                    if badge { badgeDot }
                }
                // 그림은 22 지만 닿는 자리는 44 다
                .frame(
                    width: HifisSize.headerIconButton,
                    height: HifisSize.headerIconButton
                )
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel(label)
    }

    /// 안 읽음 점. **숫자는 안 쓴다** — 몇 개인지는 들어가서 본다.
    ///
    /// 바탕색 원 **안에** 빨간 원을 넣는다. 빨간 원 위에 테두리를 얹으면
    /// 테 바깥 안티에일리어싱 틈으로 아래 빨강이 비쳐 분홍 테가 생긴다
    /// (안드로이드에서 실제로 겪었다).
    private var badgeDot: some View {
        Circle()
            .fill(HifisColor.surface)
            .frame(
                width: HifisSize.badgeDot + HifisSize.badgeRing * 2,
                height: HifisSize.badgeDot + HifisSize.badgeRing * 2
            )
            .overlay {
                Circle()
                    .fill(HifisColor.danger)
                    .frame(width: HifisSize.badgeDot, height: HifisSize.badgeDot)
            }
            // 아이콘 그림 밖으로 조금 나와 앉는다 — 안에 두면 선과 겹쳐 뭉갠다
            .offset(x: HifisSize.badgeRing * 2, y: -HifisSize.badgeRing)
    }
}
