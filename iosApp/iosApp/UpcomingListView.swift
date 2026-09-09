import SwiftUI
import SharedKit

/// 다가오는 일정 — 날짜별로 묶어 세운다
///
/// 왼쪽에 날짜, 오른쪽에 그 날 일정들. **일정이 없는 날은 아예 빠진다** (`Calendar.upcoming`).
///
/// **카드가 아니라 달력과 같은 네모 판이다** — 모서리를 안 둥글리고 화면 끝까지 간다.
/// 둥근 것은 그 안의 일정 줄이다. 달력 아래에 판이 하나 더 이어지는 모양이라
/// 위에 `sectionGap` 만큼 바닥이 비쳐 두 판이 갈린다.
struct UpcomingListView: View {
    let groups: [DaySchedule]

    /// 달력 판과 이 판을 가르는 틈 — 바닥색이 비친다
    private let sectionGap: CGFloat = 10

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Spacer().frame(height: sectionGap)

            VStack(alignment: .leading, spacing: 0) {
                Text("다가오는 일정")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(HifisColor.ink)
                    .padding(.horizontal, HifisSize.screenEdge)

                Spacer().frame(height: 16)
                divider

                if groups.isEmpty {
                    Text("앞으로 2주간 잡힌 일정이 없어요")
                        .font(HifisFont.caption)
                        .foregroundStyle(HifisColor.inkTertiary)
                        .padding(.horizontal, HifisSize.screenEdge)
                        .padding(.vertical, 14)
                } else {
                    ForEach(Array(groups.enumerated()), id: \.element.key) { index, group in
                        if index > 0 { divider }
                        DayGroupView(group: group)
                    }
                }
            }
            .padding(.vertical, 20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(HifisColor.surface)
        }
    }

    /// 판 안을 가르는 줄 — 화면 끝까지 안 간다
    private var divider: some View {
        Rectangle()
            .fill(HifisColor.line)
            .frame(height: 1)
            .padding(.horizontal, HifisSize.screenEdge)
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
            // **판이 `surface` 라 줄은 `background` 로 깐다.** 같은 색이면 줄이 안 보인다
            .background(
                HifisColor.background,
                in: RoundedRectangle(cornerRadius: HifisSize.rowRadius, style: .continuous)
            )
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
    }
}
