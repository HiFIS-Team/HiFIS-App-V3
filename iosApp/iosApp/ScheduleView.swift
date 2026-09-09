import SwiftUI
import SharedKit

/// 일정 — 달력 + 다가오는 일정
///
/// 격자·묶음 계산은 `shared` 의 `Calendar` 가 한다. 두 플랫폼이 같은 달력을 그려야 한다.
/// 안드로이드 `ScheduleScreen.kt` 와 같은 화면이다 — 한쪽만 고치면 갈린다.
///
struct ScheduleView: View {
    private let today: Kotlinx_datetimeLocalDate
    private let events: [ScheduleEvent]

    @State private var monthMode = true
    @State private var anchor: Kotlinx_datetimeLocalDate
    @State private var pickedKey: String

    init() {
        let parts = Foundation.Calendar.current.dateComponents(
            [.year, .month, .day], from: Date()
        )
        // 날짜는 `shared` 가 짓는다 — 화면에서 직접 만들면 판이 올라갈 때 한쪽만 깨진다
        let now = SharedKit.Calendar.shared.dateOf(
            year: Int32(parts.year ?? 2026),
            month: Int32(parts.month ?? 1),
            day: Int32(parts.day ?? 1)
        )
        today = now
        events = ScheduleEvent.companion.demo(today: now)
        _anchor = State(initialValue: now)
        _pickedKey = State(initialValue: now.description())
    }

    private var weeks: [[CalendarCell]] {
        monthMode
            ? SharedKit.Calendar.shared.monthGrid(anyDayInMonth: anchor, today: today)
            : SharedKit.Calendar.shared.weekGrid(anyDayInWeek: anchor, today: today)
    }

    private var upcoming: [DaySchedule] {
        SharedKit.Calendar.shared.upcoming(events: events, today: today, days: 14)
    }

    var body: some View {
        TabPage(title: "일정") {
            ScrollView {
                VStack(spacing: 0) {
                    controls
                    CalendarGridView(
                        weeks: weeks,
                        events: events,
                        pickedKey: pickedKey,
                        onPick: { pickedKey = $0 }
                    )
                    UpcomingListView(groups: upcoming)
                }
                .padding(.bottom, 24)
            }
        }
    }

    /// **달 이름이 줄 한가운데에 선다.** 오른쪽으로 몰아 놓으면 이전/다음을
    /// 누를 때마다 글자 길이에 따라 자리가 흔들린다 (`2026년 9월` ↔ `2026년 12월`)
    private var controls: some View {
        ZStack {
            HStack(spacing: 0) {
                ModeToggle(monthMode: $monthMode)
                Spacer()
                Button {} label: {
                    Image("ic_plus")
                        .renderingMode(.template)
                        .resizable()
                        .frame(width: 18, height: 18)
                        .foregroundStyle(.white)
                        .frame(width: HifisSize.stepButton, height: HifisSize.stepButton)
                        .background(
                            HifisColor.brand,
                            in: RoundedRectangle(cornerRadius: 12, style: .continuous)
                        )
                }
                .buttonStyle(TapStyle())
                .accessibilityLabel("일정 추가")
            }

            HStack(spacing: 0) {
                StepButton(icon: "ic_chevron_left", label: "이전") {
                    anchor = SharedKit.Calendar.shared.step(
                        from: anchor, monthMode: monthMode, back: true
                    )
                }
                Text(SharedKit.Calendar.shared.monthLabel(anyDayInMonth: anchor))
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(HifisColor.ink)
                    .padding(.horizontal, 10)
                StepButton(icon: "ic_chevron_right", label: "다음") {
                    anchor = SharedKit.Calendar.shared.step(
                        from: anchor, monthMode: monthMode, back: false
                    )
                }
            }
        }
        .padding(.horizontal, HifisSize.screenEdge)
        .padding(.vertical, 10)
    }
}

/// 달/주 전환 — 두 칸짜리 알약
private struct ModeToggle: View {
    @Binding var monthMode: Bool

    var body: some View {
        HStack(spacing: 0) {
            chip("월", on: monthMode) { monthMode = true }
            chip("주", on: !monthMode) { monthMode = false }
        }
        .padding(3)
        .background(HifisColor.background, in: RoundedRectangle(cornerRadius: 11, style: .continuous))
    }

    private func chip(_ label: String, on: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(on ? HifisColor.ink : HifisColor.inkTertiary)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .fill(on ? HifisColor.surface : .clear)
                )
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
    }
}

private struct StepButton: View {
    let icon: String
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(icon)
                .renderingMode(.template)
                .resizable()
                .frame(width: 18, height: 18)
                .foregroundStyle(HifisColor.ink)
                .frame(width: HifisSize.stepButton, height: HifisSize.stepButton)
                .overlay(
                    RoundedRectangle(cornerRadius: 12, style: .continuous)
                        .strokeBorder(HifisColor.line, lineWidth: 1)
                )
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel(label)
    }
}

/// 일요일은 빨강, 토요일은 파랑 — 달력에서 늘 그렇게 읽는다
func weekdayColor(_ weekday: Int32) -> Color {
    switch weekday {
    case 0: return HifisColor.danger
    case 6: return HifisColor.brand
    default: return HifisColor.ink
    }
}
