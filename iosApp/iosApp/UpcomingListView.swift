import SwiftUI
import SharedKit

/// 다가오는 일정 — 날짜별로 묶어 세운다
///
/// 왼쪽에 날짜, 오른쪽에 그 날 일정들. **일정이 없는 날은 아예 빠진다** (`Calendar.upcoming`).
struct UpcomingListView: View {
    let groups: [DaySchedule]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("다가오는 일정")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(HifisColor.ink)
                .padding(.horizontal, HifisSize.screenEdge)
                .padding(.top, 24)

            Spacer().frame(height: 14)

            if groups.isEmpty {
                Text("앞으로 2주간 잡힌 일정이 없어요")
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
                    .padding(.horizontal, HifisSize.screenEdge)
                    .padding(.vertical, 12)
            } else {
                ForEach(Array(groups.enumerated()), id: \.element.key) { index, group in
                    if index > 0 {
                        Rectangle()
                            .fill(HifisColor.line)
                            .frame(height: 1)
                            .padding(.horizontal, HifisSize.screenEdge)
                    }
                    DayGroupView(group: group)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private struct DayGroupView: View {
    let group: DaySchedule

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // 날짜 기둥 — 줄이 몇 개든 폭이 같아야 오른쪽 일정들이 안 흔들린다
            VStack(spacing: 0) {
                Text("\(group.day)")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(weekdayColor(group.weekday))
                Text(group.weekdayLabel)
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
            }
            .frame(width: 44)

            VStack(spacing: 8) {
                ForEach(group.events, id: \.id) { event in
                    EventRowView(event: event)
                }
            }
        }
        .padding(.horizontal, HifisSize.screenEdge)
        .padding(.vertical, 12)
    }
}

private struct EventRowView: View {
    let event: ScheduleEvent

    var body: some View {
        Button {} label: {
            HStack(spacing: 10) {
                Circle()
                    .fill(HifisEventTint.of(event.kind))
                    .frame(width: HifisSize.calendarDot + 1, height: HifisSize.calendarDot + 1)
                Text(event.title)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(HifisColor.ink)
                    .lineLimit(1)
                Spacer(minLength: 10)
                Text(event.timeText)
                    .font(HifisFont.caption)
                    .monospacedDigit()
                    .foregroundStyle(HifisColor.inkTertiary)
            }
            .padding(.horizontal, HifisSize.rowPaddingH)
            .padding(.vertical, HifisSize.rowPaddingV)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                HifisColor.surface,
                in: RoundedRectangle(cornerRadius: HifisSize.rowRadius, style: .continuous)
            )
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
    }
}
