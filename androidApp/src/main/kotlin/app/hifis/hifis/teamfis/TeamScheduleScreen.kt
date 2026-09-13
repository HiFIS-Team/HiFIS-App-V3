package app.hifis.hifis.teamfis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hifis.hifis.shell.TabPage
import app.hifis.hifis.ui.FoldCalendar
import app.hifis.hifis.ui.FoldCalendarBar
import app.hifis.shared.schedule.Calendar
import java.time.LocalDate as JavaDate

/**
 * TeamFIS 일정 — **헤더 밑 달력이 먼저다** (2026-09-13 대표)
 *
 * 업무 화면에 세운 [FoldCalendar] 를 그대로 쓴다. 같은 앱 안에서 날을 고르는 자리가
 * 둘이면 안 된다 — 달력은 `ui/` 에 두고 두 화면이 나눠 쓴다.
 *
 * **HiFIS 일정과 다른 화면이다** (2026-09-11 대표). 저쪽은 달 격자에 일정 점을 찍고
 * `다가오는 일정` 을 이어 붙이는데, 여기는 PT 수업을 날짜로 훑는 자리라 짜임이 다르다.
 *
 * > **달력 아래는 아직 비어 있다.** 고른 날에 뭘 세울지 안 정했다 —
 * > 정해지면 `SPEC.md` 에 먼저 적고 만든다.
 */
@Composable
fun TeamScheduleScreen(
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onScan: () -> Unit = {},
    onNotification: () -> Unit = {},
    onChat: () -> Unit = {},
) {
    TabPage(
        modifier,
        onSearch = onSearch,
        onScan = onScan,
        onNotification = onNotification,
        onChat = onChat,
    ) {
        // **날짜는 공용 모듈이 짓는다** — 플랫폼마다 만들면 한쪽만 어긋난다
        val today = remember {
            JavaDate.now().let { Calendar.dateOf(it.year, it.monthValue, it.dayOfMonth) }
        }
        // 화면을 돌려도 펴 둔 채로 있는다
        var expanded by rememberSaveable { mutableStateOf(false) }
        // 고른 날과 펼쳤을 때 보이는 달 — **화살표는 달만 옮긴다**
        var picked by remember { mutableStateOf(today) }
        var month by remember { mutableStateOf(today) }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            FoldCalendar(
                picked = picked,
                today = today,
                month = month,
                expanded = expanded,
                onPick = {
                    picked = it
                    // 고른 날이 든 달을 보여 준다 — 옆 달을 눌러 넘어갔을 때 뒤에 남지 않게
                    month = it
                },
                onMonth = { month = it },
                modifier = Modifier.padding(top = CALENDAR_TOP),
            )
            FoldCalendarBar(
                expanded = expanded,
                onToggle = { expanded = !expanded },
                modifier = Modifier.padding(top = BAR_CALENDAR_GAP),
            )
        }
    }
}

/** 헤더와 달력 사이 — 업무 화면과 같은 값이다 */
private val CALENDAR_TOP = 8.dp

/** 달력과 `펼쳐보기` 줄 사이 — 그 줄은 달력에 딸린 것이라 바짝 붙인다 */
private val BAR_CALENDAR_GAP = 4.dp
