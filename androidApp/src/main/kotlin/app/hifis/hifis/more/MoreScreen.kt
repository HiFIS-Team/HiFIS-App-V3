package app.hifis.hifis.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.component.ScreenTitle
import app.hifis.hifis.ui.component.TabPage
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.nav.MainTab
import app.hifis.shared.nav.MoreGroup
import app.hifis.shared.nav.MoreRow

/**
 * 전체 — **앱의 전수 명단**
 *
 * 하단바 다섯 칸과 홈 바로가기 여섯 개로는 화면을 다 못 담는다. 여기가 마지막
 * 그물이라 **어느 화면도 이 목록에서 새어 나가면 안 된다.** 명단은 `shared` 의
 * [MoreRow] 하나뿐이고 `MoreRowTest` 가 빠진 것을 잡는다.
 *
 * **판으로 싸지 않는다.** 흐린 머리말 하나와 그 아래 줄들이 전부다 —
 * 목록을 훑는 화면이라 칸을 그리면 줄마다 테두리를 읽게 된다.
 * 그래서 묶음을 가르는 것은 [Dimens.moreGroupGap] 여백 하나뿐이고,
 * 줄 사이에는 구분선도 꺾쇠도 없다.
 *
 * **줄에 색을 안 쓴다.** 홈 바로가기 여섯 색은 그 격자 전용이다 —
 * 여기까지 번지면 브랜드 파랑 하나로 강조하던 규칙이 무너진다.
 */
@Composable
fun MoreScreen(modifier: Modifier = Modifier, onTab: (MainTab) -> Unit = {}) {
    TabPage(modifier) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            // 제목도 같이 굴러간다 — 붙어 있는 것은 헤더(아이콘 줄)뿐이다
            ScreenTitle("전체")
            MoreGroup.all.forEach { group -> MenuGroup(group, onTab) }
        }
    }
}

@Composable
private fun MenuGroup(group: MoreGroup, onTab: (MainTab) -> Unit) {
    Spacer(Modifier.height(Dimens.moreGroupGap))
    Text(
        group.title,
        style = HifisType.caption.copy(fontWeight = FontWeight.Medium),
        color = HifisTheme.colors.inkTertiary,
        modifier = Modifier.padding(horizontal = Dimens.screenEdge),
    )
    Spacer(Modifier.height(Dimens.moreGroupTitleGap))
    MoreRow.of(group).forEach { row -> MenuRow(row, onTab) }
}

@Composable
private fun MenuRow(row: MoreRow, onTab: (MainTab) -> Unit) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(Dimens.moreRow)
            // 하단바에 자리가 있는 화면은 **탭을 옮긴다** — 화면을 새로 쌓지 않는다.
            // 나머지는 아직 화면이 없어서 눌러도 할 일이 없다
            .tap(label = row.label) { row.tab?.let(onTab) }
            .padding(horizontal = Dimens.screenEdge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(drawableOf(row)),
            contentDescription = null, // 바로 옆에 이름이 있다
            tint = colors.ink,
            modifier = Modifier.size(Dimens.moreIcon),
        )
        Spacer(Modifier.width(14.dp))
        Text(row.label, style = HifisType.body, color = colors.ink)
    }
}

/**
 * 줄 아이콘을 그림 자원으로 바꾼다
 *
 * **`when` 이 enum 을 다 덮어야 컴파일된다.** 줄을 추가하면 여기서 걸린다 —
 * 이름으로 자원을 찾는 방식(`getIdentifier`)은 빠뜨려도 빌드가 통과해서 안 쓴다.
 */
private fun drawableOf(row: MoreRow): Int = when (row) {
    MoreRow.WORK -> R.drawable.ic_work
    MoreRow.PROJECT -> R.drawable.ic_project
    MoreRow.MEETING -> R.drawable.ic_meeting
    MoreRow.SCHEDULE -> R.drawable.ic_schedule
    MoreRow.APPROVAL -> R.drawable.ic_approval
    MoreRow.ATTENDANCE -> R.drawable.ic_attendance
    MoreRow.SALARY -> R.drawable.ic_salary
    MoreRow.STAFF -> R.drawable.ic_staff
    MoreRow.NOTICE -> R.drawable.ic_notice
    MoreRow.RANKING -> R.drawable.ic_ranking
    MoreRow.SETTINGS -> R.drawable.ic_settings
    MoreRow.LOGOUT -> R.drawable.ic_logout
}
