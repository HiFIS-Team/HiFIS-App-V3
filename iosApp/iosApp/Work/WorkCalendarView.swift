import SwiftUI
import SharedKit

/// 업무 달력 — **헤더 바로 밑** (2026-09-13 대표, TeamFIS 일정 달력을 그대로)
///
/// 평소에는 **이번 주 한 줄**, `펼쳐보기` 를 누르면 **그 달 전체**로 늘어난다.
/// 달을 늘 펴 두면 업무 목록이 화면 밖으로 밀린다.
///
/// **브랜드색을 아예 안 쓴다.** 고른 날은 `surface` 알약과 `ink` 로 표시한다 —
/// 상시 떠 있는 것에 액센트 예산을 쓰지 않는다. 오늘은 굵기로만 선다.
///
/// 격자를 어떻게 자르는지는 `shared` 의 `Calendar` 가 정한다. 여기는 그리기만 한다 —
/// 두 플랫폼이 각자의 날짜 API 로 세면 같은 주인데 첫 칸이 다른 날이 된다.
///
/// 안드로이드 `WorkCalendar` 와 같은 값이다.
struct WorkCalendarView: View {
    /// 고른 날 — 개인 업무 목록이 이 날의 요일로 갈린다
    let picked: Kotlinx_datetimeLocalDate
    let today: Kotlinx_datetimeLocalDate
    /// 펼쳤을 때 보이는 달 (그 달의 아무 날). 화살표는 이것만 옮긴다
    let month: Kotlinx_datetimeLocalDate
    let expanded: Bool
    let onPick: (Kotlinx_datetimeLocalDate) -> Void
    let onMonth: (Kotlinx_datetimeLocalDate) -> Void

    /// 알약은 칸을 따라 **흐른다** — 칸마다 면을 껐다 켜면 고른 자리가 순간이동해 보인다
    @Namespace private var pill

    private var pickedKey: String { picked.description() }

    private var week: [CalendarCell] {
        SharedKit.Calendar.shared.weekGrid(anyDayInWeek: picked, today: today).first ?? []
    }

    private var weeks: [[CalendarCell]] {
        SharedKit.Calendar.shared.monthGrid(anyDayInMonth: month, today: today)
    }

    var body: some View {
        // **잘라 내며 드러난다.** 둘을 같은 자리에서 겹쳐 흐리면(`if`/`else` 갈아 끼우기)
        // 주 줄 위에 달 격자가 포개져 보인다 — 안드로이드는 둘이 세로로 나란히 서서
        // 각자 잘리며 높이만 오간다 (`expandVertically` / `shrinkVertically`).
        //
        // **아래에 붙어 위가 잘린다** (`Alignment.Bottom` — 안드로이드 기본값).
        // 달은 아래 줄부터 드러나고 머리글이 마지막에 붙는다.
        VStack(spacing: 0) {
            weekStrip
                // **제 키로 먼저 선다.** 안 박으면 `frame(height:)` 이 자르는 게 아니라
                // 키를 *제안*해서, 안쪽 줄들이 그 키에 맞춰 다시 앉는다 (줄이 겹쳐 보였다)
                .fixedSize(horizontal: false, vertical: true)
                .frame(height: expanded ? 0 : Self.stripRow, alignment: .bottom)
                .opacity(expanded ? 0 : 1)
                .clipped()
                // 잘려 안 보이는 것은 눌리지도 않아야 한다 — 그림만 잘릴 뿐 자리는 남는다
                .allowsHitTesting(!expanded)
            monthBlock
                .fixedSize(horizontal: false, vertical: true)
                .frame(height: expanded ? monthHeight : 0, alignment: .bottom)
                .opacity(expanded ? 1 : 0)
                .clipped()
                .allowsHitTesting(expanded)
        }
        .padding(.horizontal, HifisSize.screenEdge)
    }

    private var weekStrip: some View {
        HStack(spacing: 0) {
            ForEach(week, id: \.key) { cell in
                dayCell(cell)
            }
        }
    }

    private var monthBlock: some View {
        VStack(spacing: 0) {
            monthHeader
            weekdayHeader
            ForEach(Array(weeks.enumerated()), id: \.offset) { _, week in
                monthRow(week)
            }
        }
    }

    /// 다 펼쳤을 때의 키 — **재지 않고 센다**
    ///
    /// 안의 값이 다 고정이라 더할 수 있다. 재려면(`GeometryReader`) 첫 그림에서 한 번은
    /// 0 으로 서고, 그 사이에 펼치면 0 에서 0 으로 움직여 아무 일도 안 일어난다.
    private var monthHeight: CGFloat {
        Self.headerRow + Self.monthHeaderGap
            + Self.weekdayRow + Self.weekdayGap
            + CGFloat(weeks.count) * Self.monthRow
    }

    // MARK: - 접힌 줄

    /// 요일과 날짜가 한 칸에 있고, 고른 칸만 알약이 채워진다
    private func dayCell(_ cell: CalendarCell) -> some View {
        let picked = cell.key == pickedKey

        return VStack(spacing: Self.markGap) {
            Text(SharedKit.Calendar.shared.weekdayLabels[Int(cell.weekday)])
                .font(HifisFont.caption)
                .foregroundStyle(picked ? HifisColor.background : dayColor(cell, picked: false, dim: true))
                .frame(width: Self.mark, height: Self.mark)
                .background { if picked { Circle().fill(HifisColor.ink) } }
            Text("\(cell.day)")
                // 날짜는 자릿수가 바뀌어도 칸 안에서 흔들리면 안 된다
                .font(HifisFont.body.weight(Self.todayWeight(cell, .semibold)).monospacedDigit())
                .foregroundStyle(dayColor(cell, picked: picked))
                .frame(width: Self.stamp, height: Self.stamp)
        }
        .frame(maxWidth: .infinity)
        .frame(height: Self.stripRow)
        .background {
            if picked {
                Capsule()
                    .fill(HifisColor.surface)
                    .frame(width: Self.pillWidth)
                    .matchedGeometryEffect(id: "week-pill", in: pill)
            }
        }
        .contentShape(Rectangle())
        .onTapGesture {
            guard !picked else { return }
            withAnimation(.easeOut(duration: Self.slide)) { onPick(cell.date) }
        }
        .accessibilityLabel("\(cell.day)일")
    }

    // MARK: - 펼친 달

    /// 펼친 상태의 달 머리글 — `2026년 9월` 과 앞뒤 달로 가는 화살표
    private var monthHeader: some View {
        HStack(spacing: 0) {
            Text(SharedKit.Calendar.shared.monthLabel(anyDayInMonth: month))
                .font(HifisFont.header.monospacedDigit())
                .foregroundStyle(HifisColor.ink)
            Spacer(minLength: 0)
            monthArrow("ic_chevron_left", label: "이전 달", back: true)
            monthArrow("ic_chevron_right", label: "다음 달", back: false)
        }
        // 화살표는 누르는 자리가 그림보다 넓다 — 그만큼 오른쪽으로 내보낸다
        .padding(.trailing, -(Self.arrowTap - Self.arrowIcon) / 2)
        .padding(.bottom, Self.monthHeaderGap)
    }

    private func monthArrow(_ icon: String, label: String, back: Bool) -> some View {
        Button {
            onMonth(SharedKit.Calendar.shared.step(from: month, monthMode: true, back: back))
        } label: {
            Image(icon)
                .renderingMode(.template)
                .resizable()
                .frame(width: Self.arrowIcon, height: Self.arrowIcon)
                .foregroundStyle(HifisColor.inkSecondary)
                .frame(width: Self.arrowTap, height: Self.arrowTap)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }

    /// 칸마다 요일을 반복하면 달력이 시끄럽다 — 머리글 한 줄로 뺀다
    private var weekdayHeader: some View {
        HStack(spacing: 0) {
            ForEach(Array(SharedKit.Calendar.shared.weekdayLabels.enumerated()), id: \.offset) { weekday, label in
                Text(label)
                    .font(HifisFont.caption)
                    .foregroundStyle(Self.weekendColor(Int32(weekday)) ?? HifisColor.inkTertiary)
                    .frame(maxWidth: .infinity)
            }
        }
        // **키를 못 박는다** — 펼친 키를 세어서 쓰는 자리라 글자가 정하게 두면 어긋난다
        .frame(height: Self.weekdayRow)
        .padding(.bottom, Self.weekdayGap)
    }

    private func monthRow(_ week: [CalendarCell]) -> some View {
        HStack(spacing: 0) {
            ForEach(week, id: \.key) { cell in
                Group {
                    // 옆 달 날짜를 흐리게 채우지 않는다 — 이 달 안에서만 고르게 한다
                    if cell.inMonth { monthDay(cell) } else { Color.clear }
                }
                .frame(maxWidth: .infinity)
                .frame(height: Self.monthRow)
            }
        }
    }

    private func monthDay(_ cell: CalendarCell) -> some View {
        let picked = cell.key == pickedKey

        return VStack(spacing: 0) {
            Text("\(cell.day)")
                .font(HifisFont.label.weight(Self.todayWeight(cell, .medium)).monospacedDigit())
                .foregroundStyle(dayColor(cell, picked: picked))
                .frame(width: Self.stamp, height: Self.stamp)
            // 고른 날은 **동그라미 대신 밑에 점**이다 — 달을 다 펴면 칸이 서른 개라
            // 면을 깔면 그 칸만 카드처럼 떠오른다
            Circle()
                .fill(picked ? HifisColor.ink : .clear)
                .frame(width: HifisSize.calendarDot, height: HifisSize.calendarDot)
        }
        .contentShape(Rectangle())
        .onTapGesture {
            guard !picked else { return }
            withAnimation(.easeOut(duration: Self.slide)) { onPick(cell.date) }
        }
        .accessibilityLabel("\(cell.day)일")
    }

    // MARK: - 색

    /// 날짜 글자색 — **고른 날 > 주말 > 보통** 차례로 이긴다
    ///
    /// **오늘은 색으로 말하지 않는다** (2026-09-13 대표 — "일요일은 빨간색이어야지").
    /// 한때 오늘을 브랜드색으로 뒀는데, 그러면 오늘이 일요일인 날 **빨강이 파랑에 덮인다.**
    /// 일요일 빨강·토요일 파랑은 달력을 읽는 관습이라 그게 이겨야 한다 —
    /// 오늘은 `todayWeight` 로 굵기만 준다.
    ///
    /// - Parameter dim: 요일 한 글자 — 날짜보다 한 단 옅다
    private func dayColor(_ cell: CalendarCell, picked: Bool, dim: Bool = false) -> Color {
        if picked { return HifisColor.ink }
        return Self.weekendColor(cell.weekday) ?? (dim ? HifisColor.inkTertiary : HifisColor.inkSecondary)
    }

    /// 오늘의 굵기 — **색을 안 건드린다**
    ///
    /// 달을 넘겨 보다가도 돌아올 자리를 잃지 않게 하는 표시다. 색으로 주면 주말색과 싸우고,
    /// 점·동그라미로 주면 고른 날 표시(알약·점)와 겹친다. 남은 축이 굵기다.
    private static func todayWeight(_ cell: CalendarCell, _ base: Font.Weight) -> Font.Weight {
        cell.isToday ? .bold : base
    }

    /// 일요일 빨강 · 토요일 파랑 — 달력에서 늘 그렇게 읽는다 (일정 달력과 같다)
    private static func weekendColor(_ weekday: Int32) -> Color? {
        switch weekday {
        case 0: HifisColor.danger
        case 6: HifisColor.calendarSaturday
        default: nil
        }
    }

    /// 접힌 줄의 한 칸 높이 — **알약 높이가 곧 줄 높이다**
    ///
    /// 요일과 날짜가 위아래로 서서 터치 타겟(44)보다 커야 한다.
    private static let stripRow: CGFloat = 68
    /// 고른 날 알약 폭
    private static let pillWidth: CGFloat = 44
    /// 요일 한 글자를 감싸는 동그라미와 그 아래 사이
    private static let mark: CGFloat = 26
    private static let markGap: CGFloat = 4
    /// 날짜 글자가 앉는 칸 — 접힌 줄과 펼친 달이 같은 값을 쓴다
    private static let stamp: CGFloat = 40
    /// 펼친 달의 한 줄 높이 — 날짜(40) + 점 자리
    private static let monthRow: CGFloat = 50
    /// 달 머리글 한 줄 — **화살표 누르는 자리가 정한다** (글자보다 크다)
    private static let headerRow: CGFloat = arrowTap
    /// 요일 머리글 한 줄 — 안드로이드 `caption` 줄 높이(18)와 같다
    private static let weekdayRow: CGFloat = 18
    /// 달 머리글 아래·요일 머리글 아래
    private static let monthHeaderGap: CGFloat = 12
    private static let weekdayGap: CGFloat = 4
    /// 앞뒤 달 화살표 — 누르는 자리와 그림
    private static let arrowTap: CGFloat = 40
    private static let arrowIcon: CGFloat = 20
    /// 알약이 옮겨 가는 빠르기 — `ModeSwitch` 와 같은 값이다
    private static let slide: Double = 0.24
    /// 달이 펴지고 접히는 결 — 줄 수가 통째로 바뀌는 자리라 알약보다 길다
    ///
    /// **부르는 쪽이 `withAnimation` 으로 건다.** 여기서 `.animation(value:)` 로 걸면
    /// 달력 안쪽만 움직이고 아래 줄들은 즉시 튄다 — 화살표가 혼자 늦게 따라오는 것처럼 보인다.
    /// 안드로이드 `tween(320)` 과 **같은 값·같은 곡선**이다 — Compose `tween` 의 기본
    /// 가감속은 `FastOutSlowIn`(0.4, 0, 0.2, 1) 이라 `.easeInOut`(0.42, 0, 0.58, 1) 과 다르다
    static let foldMotion: Animation = .timingCurve(0.4, 0, 0.2, 1, duration: 0.32)
}

/// 달력 아래 한 줄 — `펼쳐보기` / `접기`
///
/// **화살표가 뒤집히며** 달력이 그 달로 늘어난다. 줄 전체를 누르는 자리로 두지 않고
/// 글자 폭만큼만 잡는다 — 옆의 빈 자리를 눌러도 펴지면 실수로 여닫힌다.
struct WorkCalendarBar: View {
    let expanded: Bool
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onToggle) {
                HStack(spacing: 2) {
                    Text(WorkBoard.shared.foldLabel(expanded: expanded))
                        .font(HifisFont.label)
                    Image("ic_chevron_down")
                        .renderingMode(.template)
                        .resizable()
                        .frame(width: Self.icon, height: Self.icon)
                        // **제 애니메이션을 안 건다.** 달력과 같은 전환을 타야 같이 움직인다
                        .rotationEffect(.degrees(expanded ? 180 : 0))
                }
                .foregroundStyle(HifisColor.inkSecondary)
                .padding(.horizontal, Self.inset)
                .frame(height: Self.height)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            Spacer(minLength: 0)
        }
        // 글자 왼쪽 끝을 달력 좌우 여백에 맞춘다 — 누르는 자리가 그만큼 밖으로 나가 있다
        .padding(.horizontal, HifisSize.screenEdge - Self.inset)
    }

    private static let height: CGFloat = 36
    private static let inset: CGFloat = 8
    private static let icon: CGFloat = 18
}
