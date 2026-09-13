import SwiftUI
import SharedKit

/// 하단 탭 바 **위**에 붙는 다음 수업 줄 — 왼쪽 회원, 오른쪽 시작 시각
///
/// **유리를 직접 그리지 않는다.** iOS 26 의 `tabViewBottomAccessory` 자리라
/// 재질·모서리·스크롤 반응이 전부 시스템 것이다 (애플 뮤직 미니 플레이어와 같은 자리).
/// 아래로 굴리면 탭바가 접히면서 이 줄이 가운데로 내려앉는 것도 시스템이 한다.
///
/// **TeamFIS 에만 선다** (2026-09-14 대표). 트레이너가 하루 종일 보는 것이 다음 수업이라
/// 어느 탭에 있든 눈에 있어야 한다 — HiFIS 는 그 자리에 떠 있을 것이 없다.
///
/// TeamFIS 레포의 `NextClassBar` 와 같은 짜임이다 (대표가 그 레포를 지목).
struct NextClassBarView: View {
    let item: TeamClass

    var body: some View {
        HStack(spacing: 0) {
            // 시계 — 우리 아이콘 벌의 `ic_attendance` 가 그 그림이다 (원 + 바늘)
            Image("ic_attendance")
                .renderingMode(.template)
                .resizable()
                .frame(width: Self.icon, height: Self.icon)
                .foregroundStyle(HifisColor.ink)
                .padding(.trailing, Self.gap)

            Text(item.memberLabel)
                // 오른쪽 시각과 **같은 크기**로 맞춘다 — 한 줄 안에서 둘이 짝이다
                .font(HifisFont.body)
                .foregroundStyle(HifisColor.ink)
                .lineLimit(1)

            Spacer(minLength: Self.gap)

            Text(item.startLabel)
                // 시각은 자릿수가 바뀌어도 자리가 안 흔들려야 한다
                .font(HifisFont.body.monospacedDigit())
                .foregroundStyle(HifisColor.ink)
        }
        .padding(.horizontal, HifisSize.screenEdge)
    }

    private static let icon: CGFloat = 18
    private static let gap: CGFloat = 8

    /// 지금 세울 수업 — 없으면 줄을 안 붙인다
    ///
    /// 값은 아직 `TeamSchedule.demo` 다 — **서버를 안 붙였다.**
    static func next() -> TeamClass? {
        let parts = Foundation.Calendar.current.dateComponents([.year, .month, .day], from: Date())
        let today = SharedKit.Calendar.shared.dateOf(
            year: Int32(parts.year ?? 2026),
            month: Int32(parts.month ?? 1),
            day: Int32(parts.day ?? 1)
        )
        return TeamSchedule.shared.next(classes: TeamSchedule.shared.demo(today: today), today: today)
    }
}

extension View {
    /// 탭 바 위 유리 자리. **iOS 26 부터만 있다** — 그 아래에서는 그냥 안 붙는다
    @ViewBuilder
    func bottomAccessory(_ content: some View) -> some View {
        if #available(iOS 26.0, *) {
            self.tabViewBottomAccessory { content }
        } else {
            self
        }
    }

    /// 아래로 굴리면 탭 바가 **접히고**, 맨 위로 돌아오면 다시 펴진다 (애플 뮤직과 같은 동작)
    ///
    /// 접히는 모양·모션은 전부 시스템 것이다. **iOS 26 부터만 있다.**
    @ViewBuilder
    func minimizeTabBarOnScroll() -> some View {
        if #available(iOS 26.0, *) {
            self.tabBarMinimizeBehavior(.onScrollDown)
        } else {
            self
        }
    }
}
