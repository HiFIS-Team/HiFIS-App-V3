package app.hifis.hifis.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.component.TabPage
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.schedule.Calendar
import app.hifis.shared.schedule.ScheduleEvent
import kotlinx.datetime.LocalDate
import java.time.LocalDate as JavaDate

/**
 * 일정 — 달력 + 다가오는 일정
 *
 * 격자·묶음 계산은 `shared` 의 [Calendar] 가 한다. 두 플랫폼이 같은 달력을 그려야 한다.
 *
 * **큰 제목(`일정관리`)을 안 붙였다.** 하단바에서 `일정` 을 눌러 들어온 자리라
 * 화면 이름을 한 번 더 적으면 그만큼 달력이 밀린다.
 *
 * 값은 아직 [ScheduleEvent.demo] 다 — **서버를 안 붙였다.**
 */
@Composable
fun ScheduleScreen(modifier: Modifier = Modifier) {
    val today = remember {
        JavaDate.now().let { Calendar.dateOf(it.year, it.monthValue, it.dayOfMonth) }
    }
    val events = remember(today) { ScheduleEvent.demo(today) }

    var monthMode by remember { mutableStateOf(true) }
    var anchor by remember { mutableStateOf(today) }
    var picked by remember { mutableStateOf(today) }

    val weeks = remember(anchor, monthMode, today) {
        if (monthMode) Calendar.monthGrid(anchor, today) else Calendar.weekGrid(anchor, today)
    }
    val upcoming = remember(events, today) { Calendar.upcoming(events, today) }

    TabPage(modifier) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            ScheduleControls(
                label = Calendar.monthLabel(anchor),
                monthMode = monthMode,
                onMode = { monthMode = it },
                // 달 보기는 한 달씩, 주 보기는 한 주씩 옮긴다
                onPrev = { anchor = Calendar.step(anchor, monthMode, back = true) },
                onNext = { anchor = Calendar.step(anchor, monthMode, back = false) },
                onAdd = {},
            )
            CalendarGrid(weeks, events, picked) { picked = it }
            UpcomingList(upcoming)
        }
    }
}

@Composable
private fun ScheduleControls(
    label: String,
    monthMode: Boolean,
    onMode: (Boolean) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAdd: () -> Unit,
) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenEdge, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ModeToggle(monthMode, onMode)
        Spacer(Modifier.weight(1f))
        StepButton(R.drawable.ic_chevron_left, "이전", onPrev)
        Text(
            label,
            style = HifisType.body.copy(fontWeight = FontWeight.Bold),
            color = colors.ink,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
        StepButton(R.drawable.ic_chevron_right, "다음", onNext)
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .size(Dimens.stepButton)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.brand, RoundedCornerShape(12.dp))
                .tap(label = "일정 추가", onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** 달/주 전환 — 두 칸짜리 알약 */
@Composable
private fun ModeToggle(monthMode: Boolean, onMode: (Boolean) -> Unit) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(colors.background, RoundedCornerShape(11.dp))
            .padding(3.dp),
    ) {
        ModeChip("월", monthMode) { onMode(true) }
        ModeChip("주", !monthMode) { onMode(false) }
    }
}

@Composable
private fun ModeChip(label: String, on: Boolean, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    Text(
        label,
        style = HifisType.caption.copy(fontWeight = FontWeight.SemiBold),
        color = if (on) colors.ink else colors.inkTertiary,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (on) colors.surface else Color.Transparent, RoundedCornerShape(8.dp))
            .tap(label = label, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun StepButton(icon: Int, label: String, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    Box(
        Modifier
            .size(Dimens.stepButton)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
            .tap(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = label,
            tint = colors.ink,
            modifier = Modifier.size(18.dp),
        )
    }
}
