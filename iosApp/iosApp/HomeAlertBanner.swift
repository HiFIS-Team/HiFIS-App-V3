import SwiftUI
import SharedKit

/// 홈 맨 위 알림 배너 — **한 장만 서고 몇 초마다 다음 것으로 바뀐다**
///
/// 바뀔 때 **줄었다가 커지면서** 갈린다. 그냥 글자만 갈아 끼우면 바뀐 줄 모르고 지나간다.
///
/// **닫기(X)를 두지 않는다.** 닫아 놓은 것을 언제 다시 띄울지가 또 정해야 할 일이 되고,
/// 어차피 몇 초 뒤면 다음 것으로 넘어간다.
///
/// 안드로이드 `HomeAlertBanner.kt` 와 같은 배너다 — 도는 간격도 `shared` 가 들고 있다.
struct HomeAlertBanner: View {
    let alerts: [HomeAlert]
    let onOpen: (HomeAlert) -> Void

    @State private var index = 0

    /// 도는 간격 — `shared` 값이라 두 플랫폼이 같은 박자다
    private static let interval = TimeInterval(HomeAlert.companion.ROTATE_MILLIS) / 1000
    private let timer = Timer.publish(every: interval, on: .main, in: .common).autoconnect()

    var body: some View {
        // 알림이 없으면 **아무것도 안 그린다** — 빈 껍데기를 남기면 그만큼 홈이 밀린다
        if alerts.isEmpty {
            EmptyView()
        } else {
            ZStack {
                AlertCard(alert: alerts[index % alerts.count], onOpen: onOpen)
                    .id(index)
                    .transition(
                        .scale(scale: 0.94).combined(with: .opacity)
                    )
            }
            .onReceive(timer) { _ in
                // 한 장뿐이면 돌릴 것이 없다
                guard alerts.count > 1 else { return }
                withAnimation(.easeInOut(duration: 0.26)) {
                    index = (index + 1) % alerts.count
                }
            }
        }
    }
}

private struct AlertCard: View {
    let alert: HomeAlert
    let onOpen: (HomeAlert) -> Void

    var body: some View {
        let tint = HifisColor.tone(alert.kind.tone)
        let fill = tint.opacity(HifisColor.toneFillOpacity)

        return Button { onOpen(alert) } label: {
            HStack(spacing: 0) {
                Image(alert.kind.icon)
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: HifisSize.alertIcon, height: HifisSize.alertIcon)
                    .foregroundStyle(tint)
                    .frame(width: HifisSize.alertChip, height: HifisSize.alertChip)
                    .background(
                        RoundedRectangle(cornerRadius: HifisSize.alertChipRadius, style: .continuous)
                            .fill(fill)
                    )

                Spacer().frame(width: 14)

                VStack(alignment: .leading, spacing: 2) {
                    Text(alert.title)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(HifisColor.ink)
                        .lineLimit(1)
                    Text(alert.detail)
                        .font(HifisFont.caption)
                        .foregroundStyle(HifisColor.inkTertiary)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                Spacer().frame(width: 12)

                // 버튼은 카드 전체와 같은 곳으로 간다 — 따로 누를 자리를 만들지 않는다.
                // 갈 곳이 하나인데 누르는 자리를 둘로 나누면 어느 쪽이 무엇인지 설명해야 한다
                Text(alert.action)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(tint)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(fill, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
            }
            .padding(HifisSize.alertPadding)
            .frame(maxWidth: .infinity)
            .background(HifisColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous)
                    .strokeBorder(HifisColor.line, lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.04), radius: 12, y: 6)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
