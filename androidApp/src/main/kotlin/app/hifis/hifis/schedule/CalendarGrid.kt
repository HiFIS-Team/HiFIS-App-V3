package app.hifis.hifis.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.eventColor
import app.hifis.shared.schedule.Calendar
import app.hifis.shared.schedule.CalendarCell
import app.hifis.shared.schedule.ScheduleEvent
import kotlinx.datetime.LocalDate

/**
 * 달력 격자 — 요일 머리말 + 날짜 칸
 *
 * 격자를 어떻게 자르는지는 `shared` 의 [Calendar] 가 정한다. 여기는 그리기만 한다.
 *
 * **줄은 안쪽에만 긋는다.** 바깥까지 두르면 화면 가장자리에 선이 붙어 답답해 보인다.
 */
@Composable
fun CalendarGrid(
    weeks: List<List<CalendarCell>>,
    events: List<ScheduleEvent>,
    picked: LocalDate,
    onPick: (LocalDate) -> Unit,
) {
    val colors = HifisTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        WeekdayHeader()
        weeks.forEachIndexed { row, week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEachIndexed { column, cell ->
                    DayCell(
                        cell = cell,
                        events = Calendar.eventsOn(events, cell.date),
                        picked = cell.date == picked,
                        // 마지막 줄·마지막 칸은 바깥이라 선을 안 긋는다
                        drawBottom = row < weeks.lastIndex,
                        drawEnd = column < 6,
                        onPick = onPick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(Dimens.calendarWeekday)
            .drawBehind {
                drawLine(
                    color = colors.line,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f,
                )
            },
        // **`Row` 는 기본이 위쪽 정렬이다.** 줄 높이만 키우면 글자가 위에 붙은 채로
        // 아래만 비어서, 요일이 조작줄에 눌린 것처럼 보인다
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Calendar.weekdayLabels.forEachIndexed { index, label ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    style = HifisType.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = weekdayColor(index, colors, dim = false),
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: CalendarCell,
    events: List<ScheduleEvent>,
    picked: Boolean,
    drawBottom: Boolean,
    drawEnd: Boolean,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    Box(
        modifier
            .height(Dimens.calendarCell)
            .drawBehind {
                if (drawEnd) {
                    drawLine(
                        colors.line,
                        Offset(size.width, 0f),
                        Offset(size.width, size.height),
                        strokeWidth = 1f,
                    )
                }
                if (drawBottom) {
                    drawLine(
                        colors.line,
                        Offset(0f, size.height),
                        Offset(size.width, size.height),
                        strokeWidth = 1f,
                    )
                }
            }
            .background(if (picked) colors.brand.copy(alpha = 0.08f) else Color.Transparent)
            // 이 달이 아닌 날은 눌러도 갈 곳이 없다 — 자리만 채운다
            .then(if (cell.inMonth) Modifier.tap { onPick(cell.date) } else Modifier),
    ) {
        if (!cell.inMonth) return@Box

        // 오늘은 숫자를 브랜드색 원으로 감싼다 — 고른 날(옅은 면)과 겹쳐도 서로 안 가린다
        Box(
            Modifier
                .padding(start = 8.dp, top = 6.dp)
                .size(22.dp)
                .background(if (cell.isToday) colors.brand else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                cell.day.toString(),
                style = HifisType.caption.copy(fontWeight = FontWeight.SemiBold),
                color = when {
                    cell.isToday -> Color.White
                    else -> weekdayColor(cell.weekday, colors, dim = false)
                },
            )
        }

        if (events.isNotEmpty()) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // 넷 이상이면 칸을 넘친다 — 셋까지만 찍고 나머지는 눌러서 본다
                events.take(3).forEach { event ->
                    Box(
                        Modifier
                            .size(Dimens.calendarDot)
                            .background(eventColor(event.colorIndex), CircleShape),
                    )
                }
            }
        }
    }
}

/** 일요일은 빨강, 토요일은 파랑 — 달력에서 늘 그렇게 읽는다 */
@Composable
internal fun weekdayColor(weekday: Int, colors: HifisColors, dim: Boolean): Color {
    val base = when (weekday) {
        0 -> colors.danger
        6 -> colors.calendarSaturday
        else -> colors.ink
    }
    return if (dim) base.copy(alpha = 0.35f) else base
}
