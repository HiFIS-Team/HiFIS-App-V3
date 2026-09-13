import SwiftUI
import SharedKit

/// TeamFIS 일정 — **헤더 밑 달력이 먼저다** (2026-09-13 대표)
///
/// 업무 화면에 세운 `FoldCalendarView` 를 그대로 쓴다. 같은 앱 안에서 날을 고르는 자리가
/// 둘이면 안 된다 — 달력은 `UI/` 에 두고 두 화면이 나눠 쓴다.
///
/// **HiFIS 일정과 다른 화면이다** (2026-09-11 대표). 저쪽은 달 격자에 일정 점을 찍고
/// `다가오는 일정` 을 이어 붙이는데, 여기는 PT 수업을 날짜로 훑는 자리라 짜임이 다르다.
///
/// > **달력 아래는 아직 비어 있다.** 고른 날에 뭘 세울지 안 정했다 —
/// > 정해지면 `SPEC.md` 에 먼저 적고 만든다.
///
/// 안드로이드 `TeamScheduleScreen` 과 같은 화면이다.
struct TeamScheduleView: View {
    var onSearch: () -> Void = {}
    var onScan: () -> Void = {}
    var onChat: () -> Void = {}
    var onNotification: () -> Void = {}

    /// **날짜는 공용 모듈이 짓는다** — 플랫폼마다 만들면 한쪽만 어긋난다
    private let today: Kotlinx_datetimeLocalDate
    @State private var expanded = false
    /// 고른 날과 펼쳤을 때 보이는 달 — **화살표는 달만 옮긴다**
    @State private var picked: Kotlinx_datetimeLocalDate
    @State private var month: Kotlinx_datetimeLocalDate
    /// 달력이 얼마나 펼쳐졌나 — **이것만 전환 안에 든다** (0 접힘 · 1 펼침)
    @State private var fold: Double = 0

    init(
        onSearch: @escaping () -> Void = {},
        onScan: @escaping () -> Void = {},
        onChat: @escaping () -> Void = {},
        onNotification: @escaping () -> Void = {}
    ) {
        self.onSearch = onSearch
        self.onScan = onScan
        self.onChat = onChat
        self.onNotification = onNotification
        let parts = Foundation.Calendar.current.dateComponents(
            [.year, .month, .day], from: Date()
        )
        let now = SharedKit.Calendar.shared.dateOf(
            year: Int32(parts.year ?? 2026),
            month: Int32(parts.month ?? 1),
            day: Int32(parts.day ?? 1)
        )
        today = now
        _picked = State(initialValue: now)
        _month = State(initialValue: now)
    }

    var body: some View {
        TabPage(onSearch: onSearch, onScan: onScan, onChat: onChat, onNotification: onNotification) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    FoldCalendarView(
                        picked: picked,
                        today: today,
                        month: month,
                        fold: fold,
                        onPick: {
                            picked = $0
                            // 고른 날이 든 달을 보여 준다 — 옆 달을 눌러 넘어갔을 때 뒤에 남지 않게
                            month = $0
                        },
                        onMonth: { month = $0 }
                    )
                    .padding(.top, Self.calendarTop)

                    // **글자는 전환 밖, 키는 전환 안** — 같이 넣으면 글자와 아이콘이 따로 논다
                    FoldCalendarBar(expanded: expanded, fold: fold) {
                        expanded.toggle()
                        withAnimation(FoldCalendarView.foldMotion) { fold = expanded ? 1 : 0 }
                    }
                    .padding(.top, Self.barCalendarGap)
                }
                .padding(.bottom, 24)
            }
        }
    }

    /// 헤더와 달력 사이 — 업무 화면과 같은 값이다
    private static let calendarTop: CGFloat = 8
    /// 달력과 `펼쳐보기` 줄 사이 — 그 줄은 달력에 딸린 것이라 바짝 붙인다
    private static let barCalendarGap: CGFloat = 4
}
