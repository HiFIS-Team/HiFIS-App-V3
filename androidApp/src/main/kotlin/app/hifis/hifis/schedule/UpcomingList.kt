package app.hifis.hifis.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.tintOf
import app.hifis.shared.schedule.DaySchedule
import app.hifis.shared.schedule.ScheduleEvent

/**
 * 다가오는 일정 — 날짜별로 묶어 세운다
 *
 * 왼쪽에 날짜, 오른쪽에 그 날 일정들. **일정이 없는 날은 아예 빠진다** (`Calendar.upcoming`).
 */
@Composable
fun UpcomingList(groups: List<DaySchedule>) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.cardRadius)

    Column(
        Modifier
            .padding(horizontal = Dimens.screenEdge)
            .padding(top = 20.dp)
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .padding(vertical = 20.dp),
    ) {
        Text(
            "다가오는 일정",
            style = HifisType.body.copy(fontWeight = FontWeight.Bold),
            color = colors.ink,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(14.dp))

        if (groups.isEmpty()) {
            Text(
                "앞으로 2주간 잡힌 일정이 없어요",
                style = HifisType.caption,
                color = colors.inkTertiary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            return@Column
        }

        groups.forEachIndexed { index, group ->
            if (index > 0) {
                Box(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.line),
                )
            }
            DayGroup(group)
        }
    }
}

@Composable
private fun DayGroup(group: DaySchedule) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // 날짜 기둥 — 줄이 몇 개든 폭이 같아야 오른쪽 일정들이 안 흔들린다
        Column(
            Modifier.width(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                group.day.toString(),
                style = HifisType.body.copy(fontWeight = FontWeight.Bold),
                color = weekdayColor(group.weekday, colors, dim = false),
            )
            Text(
                group.weekdayLabel,
                style = HifisType.caption,
                color = colors.inkTertiary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            group.events.forEach { event -> EventRow(event) }
        }
    }
}

@Composable
private fun EventRow(event: ScheduleEvent) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.rowRadius)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            // **카드 안이라 `background` 를 쓴다.** `surface` 를 쓰면 카드와 같은 색이라
            // 줄이 안 보인다 (공지 카드의 평범한 줄과 같은 규칙)
            .background(colors.background, shape)
            .tap(label = event.title) { }
            .padding(horizontal = Dimens.rowPaddingH, vertical = Dimens.rowPaddingV),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(Dimens.calendarDot + 1.dp)
                .background(tintOf(event.kind), CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            event.title,
            style = HifisType.label.copy(fontWeight = FontWeight.SemiBold),
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            event.timeText,
            style = HifisType.caption.copy(fontFeatureSettings = HifisType.TABULAR),
            color = colors.inkTertiary,
        )
    }
}
