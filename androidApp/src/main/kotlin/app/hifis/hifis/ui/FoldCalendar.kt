package app.hifis.hifis.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.schedule.Calendar
import app.hifis.shared.schedule.CalendarCell
import kotlinx.datetime.LocalDate

/**
 * 접히는 달력 — **헤더 바로 밑** (2026-09-13 대표, TeamFIS 일정 달력을 그대로)
 *
 * 평소에는 **이번 주 한 줄**, `펼쳐보기` 를 누르면 **그 달 전체**로 늘어난다.
 * 달을 늘 펴 두면 업무 목록이 화면 밖으로 밀린다.
 *
 * **브랜드색을 아예 안 쓴다.** 고른 날은 [HifisColors.surface] 알약과 [HifisColors.ink] 로
 * 표시한다 — 상시 떠 있는 것에 액센트 예산을 쓰지 않는다. 오늘은 굵기로만 선다
 * (아래 [dayColor] · [todayWeight] 참고).
 *
 * 격자를 어떻게 자르는지는 `shared` 의 [Calendar] 가 정한다. 여기는 그리기만 한다 —
 * 두 플랫폼이 각자의 날짜 API 로 세면 같은 주인데 첫 칸이 다른 날이 된다.
 *
 * @param picked 고른 날 — 아래 목록이 이 날 것으로 갈린다
 * @param month 펼쳤을 때 보이는 달 (그 달의 아무 날). 화살표는 이것만 옮긴다
 */
@Composable
fun FoldCalendar(
    picked: LocalDate,
    today: LocalDate,
    month: LocalDate,
    expanded: Boolean,
    onPick: (LocalDate) -> Unit,
    onMonth: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 높이만 줄이면 **내용이 먼저 사라지고 빈칸이 뒤늦게 닫힌다** — 아래 줄이 늦게
    // 따라오는 것처럼 보인다. 접힐 때도 내용과 높이가 같이 움직이게 감싼다
    Column(modifier.fillMaxWidth()) {
        androidx.compose.animation.AnimatedVisibility(
            visible = !expanded,
            enter = expandVertically(tween(FOLD_MS)) + fadeIn(tween(FOLD_MS)),
            exit = shrinkVertically(tween(FOLD_MS)) + fadeOut(tween(FOLD_MS)),
        ) {
            WeekStrip(
                week = Calendar.weekGrid(picked, today).first(),
                pickedKey = picked.toString(),
                onPick = onPick,
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(FOLD_MS)) + fadeIn(tween(FOLD_MS)),
            exit = shrinkVertically(tween(FOLD_MS)) + fadeOut(tween(FOLD_MS)),
        ) {
            Column {
                MonthHeader(
                    month = month,
                    onPrev = { onMonth(Calendar.step(month, monthMode = true, back = true)) },
                    onNext = { onMonth(Calendar.step(month, monthMode = true, back = false)) },
                )
                WeekdayHeader()
                Calendar.monthGrid(month, today).forEach { week ->
                    MonthRow(week = week, pickedKey = picked.toString(), onPick = onPick)
                }
            }
        }
    }
}

/**
 * 달력 아래 한 줄 — `펼쳐보기` / `접기`
 *
 * **화살표가 뒤집히며** 달력이 그 달로 늘어난다. 줄 전체를 누르는 자리로 두지 않고
 * 글자 폭만큼만 잡는다 — 옆의 빈 자리를 눌러도 펴지면 실수로 여닫힌다.
 */
@Composable
fun FoldCalendarBar(expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = HifisTheme.colors
    val arrow by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(FOLD_MS),
        label = "fold-arrow",
    )

    Row(
        modifier
            .fillMaxWidth()
            // 글자 왼쪽 끝을 달력 좌우 여백에 맞춘다 — 누르는 자리가 그만큼 밖으로 나가 있다
            .padding(horizontal = Dimens.screenEdge - BAR_INSET),
    ) {
        Row(
            Modifier
                .height(BAR_HEIGHT)
                .clip(CircleShape)
                .tap(label = Calendar.foldLabel(expanded), onClick = onToggle)
                .padding(horizontal = BAR_INSET),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                Calendar.foldLabel(expanded),
                style = HifisType.label,
                color = colors.inkSecondary,
            )
            Icon(
                painterResource(R.drawable.ic_chevron_down),
                // 옆 글자가 이름 역할을 한다
                contentDescription = null,
                tint = colors.inkSecondary,
                modifier = Modifier
                    .size(BAR_ICON)
                    .graphicsLayer { rotationZ = arrow },
            )
        }
    }
}

/**
 * 접힌 줄 — 요일과 날짜가 한 칸에 있고, 고른 칸만 알약이 채워진다
 *
 * **알약은 칸을 따라 흐른다.** 칸마다 면을 껐다 켜면 고른 자리가 순간이동해 보인다
 * (`ModeSwitch` 와 같은 생각, 같은 240ms).
 */
@Composable
private fun WeekStrip(week: List<CalendarCell>, pickedKey: String, onPick: (LocalDate) -> Unit) {
    val colors = HifisTheme.colors
    val index = week.indexOfFirst { it.key == pickedKey }.coerceAtLeast(0)

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenEdge),
    ) {
        val column = maxWidth / DAYS
        val pillX by animateDpAsState(
            targetValue = column * index + (column - PILL_WIDTH) / 2,
            animationSpec = tween(SLIDE_MS),
            label = "pill-x",
        )

        Box(
            Modifier
                .offset(x = pillX)
                .size(width = PILL_WIDTH, height = STRIP_ROW)
                .background(colors.surface, CircleShape),
        )

        Row(Modifier.fillMaxWidth()) {
            week.forEach { cell ->
                DayCell(
                    cell = cell,
                    picked = cell.key == pickedKey,
                    onPick = onPick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: CalendarCell,
    picked: Boolean,
    onPick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    val spec = tween<Color>(SLIDE_MS)
    val markFill by animateColorAsState(
        if (picked) colors.ink else Color.Transparent, spec, label = "mark-fill",
    )
    val markTint by animateColorAsState(
        if (picked) colors.background else dayColor(cell, colors, picked = false, dim = true),
        spec,
        label = "mark-tint",
    )
    val dateTint by animateColorAsState(dayColor(cell, colors, picked), spec, label = "date-tint")

    Column(
        modifier
            .height(STRIP_ROW)
            .tap(label = "${cell.day}일", onClick = { onPick(cell.date) }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(MARK).background(markFill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(Calendar.weekdayLabels[cell.weekday], style = HifisType.caption, color = markTint)
        }
        Spacer(Modifier.height(MARK_GAP))
        Box(Modifier.size(STAMP), contentAlignment = Alignment.Center) {
            Text(
                cell.day.toString(),
                // 날짜는 자릿수가 바뀌어도 칸 안에서 흔들리면 안 된다
                style = HifisType.body.copy(
                    fontWeight = todayWeight(cell, FontWeight.SemiBold),
                    fontFeatureSettings = HifisType.TABULAR,
                ),
                color = dateTint,
            )
        }
    }
}

/** 펼친 상태의 달 머리글 — `2026년 9월` 과 앞뒤 달로 가는 화살표 */
@Composable
private fun MonthHeader(month: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.screenEdge,
                // 화살표는 누르는 자리가 그림보다 넓다 — 그만큼 오른쪽으로 내보낸다
                end = Dimens.screenEdge - (ARROW_TAP - ARROW_ICON) / 2,
                bottom = MONTH_HEADER_GAP,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            Calendar.monthLabel(month),
            style = HifisType.header.copy(fontFeatureSettings = HifisType.TABULAR),
            color = colors.ink,
        )
        Spacer(Modifier.weight(1f))
        MonthArrow(R.drawable.ic_chevron_left, "이전 달", onPrev)
        MonthArrow(R.drawable.ic_chevron_right, "다음 달", onNext)
    }
}

@Composable
private fun MonthArrow(icon: Int, label: String, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    Box(
        Modifier
            .size(ARROW_TAP)
            .clip(CircleShape)
            .tap(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = label,
            tint = colors.inkSecondary,
            modifier = Modifier.size(ARROW_ICON),
        )
    }
}

/** 펼친 상태의 요일 머리글 — 칸마다 요일을 반복하면 달력이 시끄럽다 */
@Composable
private fun WeekdayHeader() {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = Dimens.screenEdge, end = Dimens.screenEdge, bottom = WEEKDAY_GAP),
    ) {
        Calendar.weekdayLabels.forEachIndexed { weekday, label ->
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    style = HifisType.caption,
                    color = weekendColor(weekday, colors) ?: colors.inkTertiary,
                )
            }
        }
    }
}

/** 펼친 상태의 한 주 — 날짜만 있고, 고른 날은 밑에 점이 붙는다 */
@Composable
private fun MonthRow(week: List<CalendarCell>, pickedKey: String, onPick: (LocalDate) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenEdge),
    ) {
        week.forEach { cell ->
            Box(
                Modifier.weight(1f).height(MONTH_ROW),
                contentAlignment = Alignment.Center,
            ) {
                // 옆 달 날짜를 흐리게 채우지 않는다 — 이 달 안에서만 고르게 한다
                if (cell.inMonth) MonthDay(cell, cell.key == pickedKey, onPick)
            }
        }
    }
}

@Composable
private fun MonthDay(cell: CalendarCell, picked: Boolean, onPick: (LocalDate) -> Unit) {
    val colors = HifisTheme.colors
    val tint by animateColorAsState(
        dayColor(cell, colors, picked), tween(SLIDE_MS), label = "month-day",
    )

    Column(
        Modifier.tap(label = "${cell.day}일", onClick = { onPick(cell.date) }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(STAMP), contentAlignment = Alignment.Center) {
            Text(
                cell.day.toString(),
                style = HifisType.label.copy(
                    fontWeight = todayWeight(cell, FontWeight.Medium),
                    fontFeatureSettings = HifisType.TABULAR,
                ),
                color = tint,
            )
        }
        // 고른 날은 **동그라미 대신 밑에 점**이다 — 달을 다 펴면 칸이 서른 개라
        // 면을 깔면 그 칸만 카드처럼 떠오른다
        Box(
            Modifier
                .size(Dimens.calendarDot)
                .background(if (picked) colors.ink else Color.Transparent, CircleShape),
        )
    }
}

/**
 * 날짜 글자색 — **고른 날 > 주말 > 보통** 차례로 이긴다
 *
 * **오늘은 색으로 말하지 않는다** (2026-09-13 대표 — "일요일은 빨간색이어야지").
 * 한때 오늘을 브랜드색으로 뒀는데, 그러면 오늘이 일요일인 날 **빨강이 파랑에 덮인다.**
 * 일요일 빨강·토요일 파랑은 달력을 읽는 관습이라 그게 이겨야 한다 —
 * 오늘은 [todayWeight] 로 굵기만 준다.
 *
 * @param dim 요일 한 글자 — 날짜보다 한 단 옅다
 */
private fun dayColor(
    cell: CalendarCell,
    colors: HifisColors,
    picked: Boolean,
    dim: Boolean = false,
): Color = when {
    picked -> colors.ink
    else -> weekendColor(cell.weekday, colors)
        ?: if (dim) colors.inkTertiary else colors.inkSecondary
}

/**
 * 오늘의 굵기 — **색을 안 건드린다**
 *
 * 달을 넘겨 보다가도 돌아올 자리를 잃지 않게 하는 표시다. 색으로 주면 주말색과 싸우고,
 * 점·동그라미로 주면 고른 날 표시(알약·점)와 겹친다. 남은 축이 굵기다.
 */
private fun todayWeight(cell: CalendarCell, base: FontWeight): FontWeight =
    if (cell.isToday) FontWeight.Bold else base

/** 일요일 빨강 · 토요일 파랑 — 달력에서 늘 그렇게 읽는다 (일정 달력과 같다) */
private fun weekendColor(weekday: Int, colors: HifisColors): Color? = when (weekday) {
    SUNDAY -> colors.danger
    SATURDAY -> colors.calendarSaturday
    else -> null
}

private const val DAYS = 7
private const val SUNDAY = 0
private const val SATURDAY = 6

/**
 * 접힌 줄의 한 칸 높이 — **알약 높이가 곧 줄 높이다**
 *
 * **안에 든 것(26 + 4 + 40 = 70)보다 커야 한다.** 68 로 뒀더니 요일 동그라미가 알약 위로
 * 튀어나와 **헤더가 위를 자른 것처럼** 보였다 (대표가 봤다, 2026-09-13).
 * 위아래로 5 씩 남긴다.
 */
private val STRIP_ROW = 80.dp

/** 고른 날 알약 폭 */
private val PILL_WIDTH = 44.dp

/** 요일 한 글자를 감싸는 동그라미 */
private val MARK = 26.dp

/** 그 동그라미와 날짜 사이 */
private val MARK_GAP = 4.dp

/** 날짜 글자가 앉는 칸 — 접힌 줄과 펼친 달이 같은 값을 쓴다 */
private val STAMP = 40.dp

/** 펼친 달의 한 줄 높이 — 날짜(40) + 점 자리 */
private val MONTH_ROW = 50.dp

/** 달 머리글 아래·요일 머리글 아래 */
private val MONTH_HEADER_GAP = 12.dp
private val WEEKDAY_GAP = 4.dp

/** 앞뒤 달 화살표 — 누르는 자리와 그림 */
private val ARROW_TAP = 40.dp
private val ARROW_ICON = 20.dp

/** `펼쳐보기` 줄 — 높이와 글자 좌우 여백 */
private val BAR_HEIGHT = 36.dp
private val BAR_INSET = 8.dp
private val BAR_ICON = 18.dp

/** 알약이 옮겨 가는 빠르기 — `ModeSwitch` 와 같은 값이다 */
private const val SLIDE_MS = 240

/** 달이 펴지고 접히는 빠르기 — 줄 수가 통째로 바뀌는 자리라 알약보다 길다 */
private const val FOLD_MS = 320
