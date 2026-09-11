import SwiftUI
import SharedKit

/// 달력 격자 — 요일 머리말 + 날짜 칸
///
/// 격자를 어떻게 자르는지는 `shared` 의 `Calendar` 가 정한다. 여기는 그리기만 한다.
///
/// **줄은 안쪽에만 긋는다.** 바깥까지 두르면 화면 가장자리에 선이 붙어 답답해 보인다.
struct CalendarGridView: View {
    let weeks: [[CalendarCell]]
    let events: [ScheduleEvent]
    let pickedKey: String
    let onPick: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            weekdayHeader
            ForEach(Array(weeks.enumerated()), id: \.offset) { row, week in
                HStack(spacing: 0) {
                    ForEach(Array(week.enumerated()), id: \.element.key) { column, cell in
                        DayCellView(
                            cell: cell,
                            events: SharedKit.Calendar.shared.eventsOn(events: events, date: cell.date),
                            picked: cell.key == pickedKey,
                            // 마지막 줄·마지막 칸은 바깥이라 선을 안 긋는다
                            drawBottom: row < weeks.count - 1,
                            drawEnd: column < 6,
                            onPick: onPick
                        )
                    }
                }
            }
        }
        .background(HifisColor.surface)
    }

    private var weekdayHeader: some View {
        HStack(spacing: 0) {
            ForEach(Array(SharedKit.Calendar.shared.weekdayLabels.enumerated()), id: \.offset) { index, label in
                Text(label)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(weekdayColor(Int32(index)))
                    .frame(maxWidth: .infinity)
            }
        }
        .frame(height: HifisSize.calendarWeekday)
        .overlay(alignment: .bottom) {
            Rectangle().fill(HifisColor.line).frame(height: 1)
        }
    }
}

private struct DayCellView: View {
    @Environment(\.brand) private var brand
    let cell: CalendarCell
    let events: [ScheduleEvent]
    let picked: Bool
    let drawBottom: Bool
    let drawEnd: Bool
    let onPick: (String) -> Void

    var body: some View {
        ZStack(alignment: .topLeading) {
            if cell.inMonth {
                // 오늘은 숫자를 브랜드색 원으로 감싼다 — 고른 날(옅은 면)과 겹쳐도 서로 안 가린다
                Text("\(cell.day)")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(cell.isToday ? .white : weekdayColor(cell.weekday))
                    .frame(width: 22, height: 22)
                    .background(
                        Circle().fill(cell.isToday ? brand : .clear)
                    )
                    .padding(.leading, 8)
                    .padding(.top, 6)

                if !events.isEmpty {
                    // 넷 이상이면 칸을 넘친다 — 셋까지만 찍고 나머지는 눌러서 본다
                    HStack(spacing: 3) {
                        ForEach(Array(events.prefix(3).enumerated()), id: \.element.id) { _, event in
                            Circle()
                                .fill(HifisEventColor.at(event.colorIndex))
                                .frame(width: HifisSize.calendarDot, height: HifisSize.calendarDot)
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                    .padding(.bottom, 9)
                }
            }
        }
        // **`alignment` 를 여기서 준다.** 안 주면 `frame(height:)` 이 내용물을
        // 칸 가운데로 맞춰서, 점이 없는 칸만 숫자가 아래로 내려앉는다
        .frame(
            maxWidth: .infinity,
            minHeight: HifisSize.calendarCell,
            maxHeight: HifisSize.calendarCell,
            alignment: .topLeading
        )
        .background(picked ? brand.opacity(0.08) : .clear)
        .overlay(alignment: .trailing) {
            if drawEnd { Rectangle().fill(HifisColor.line).frame(width: 1) }
        }
        .overlay(alignment: .bottom) {
            if drawBottom { Rectangle().fill(HifisColor.line).frame(height: 1) }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            // 이 달이 아닌 날은 눌러도 갈 곳이 없다 — 자리만 채운다
            if cell.inMonth { onPick(cell.key) }
        }
    }
}
