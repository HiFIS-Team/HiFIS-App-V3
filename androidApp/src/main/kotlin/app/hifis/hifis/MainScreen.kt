package app.hifis.hifis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.home.HomeScreen
import app.hifis.hifis.more.MoreScreen
import app.hifis.hifis.schedule.ScheduleScreen
import app.hifis.hifis.ui.NoInteraction
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.shared.nav.MainTab

/**
 * 앱 셸 — 하단바와 탭 화면을 들고 있다
 *
 * 탭 목록은 `shared` 의 [MainTab] 하나만 읽는다. 여기서 새로 세우지 않는다.
 *
 * 하단바는 **머티리얼 3 표준 `NavigationBar`** 다. 색만 우리 토큰으로 바꾼다 —
 * 눌린 칸의 알약 표시·물결·간격은 OS 가 하던 대로 두는 것이 안드로이드답다.
 * (iOS 는 반대로 그 OS 의 리퀴드 글래스 탭바를 쓴다 — 각자 자기 표준으로 간다.)
 */
@Composable
fun MainScreen() {
    // enum 을 그대로 저장하지 않고 차례를 저장한다 — 화면을 돌려도 탭이 남는다
    var index by rememberSaveable { mutableIntStateOf(0) }
    val selected = MainTab.all[index]

    Column(
        Modifier
            .fillMaxSize()
            .background(HifisTheme.colors.background),
    ) {
        Box(Modifier.weight(1f)) {
            when (selected) {
                MainTab.HOME -> HomeScreen()
                MainTab.SCHEDULE -> ScheduleScreen()
                // 전체 목록에서 하단바에 자리가 있는 화면을 누르면 **그 탭으로 옮긴다**
                MainTab.MORE -> MoreScreen(onTab = { index = MainTab.all.indexOf(it) })
                // 나머지는 아직 화면이 없다
                MainTab.WORK,
                MainTab.ATTENDANCE,
                -> ComingSoon(selected)
            }
        }
        MainBottomBar(selected) { index = MainTab.all.indexOf(it) }
    }
}

/**
 * 탭 아이콘을 그림 자원으로 바꾼다 — **고른 칸은 속을 채운 것**을 쓴다
 *
 * **`when` 이 enum 을 다 덮어야 컴파일된다.** 탭을 추가하면 여기서 걸린다 —
 * 이름으로 자원을 찾는 방식(`getIdentifier`)은 빠뜨려도 빌드가 통과해서 안 쓴다.
 */
private fun drawableOf(tab: MainTab, filled: Boolean): Int = when (tab) {
    MainTab.HOME -> if (filled) R.drawable.ic_home_fill else R.drawable.ic_home
    MainTab.WORK -> if (filled) R.drawable.ic_work_fill else R.drawable.ic_work
    MainTab.SCHEDULE -> if (filled) R.drawable.ic_schedule_fill else R.drawable.ic_schedule
    MainTab.ATTENDANCE ->
        if (filled) R.drawable.ic_attendance_fill else R.drawable.ic_attendance
    MainTab.MORE -> if (filled) R.drawable.ic_more_fill else R.drawable.ic_more
}

@Composable
private fun MainBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    val colors = HifisTheme.colors
    NavigationBar(
        containerColor = colors.surface,
        contentColor = colors.ink,
        tonalElevation = 0.dp,
    ) {
        MainTab.all.forEach { tab ->
            val picked = tab == selected
            NavigationBarItem(
                selected = picked,
                onClick = { onSelect(tab) },
                // 누름 효과(물결)를 끈다 — 화면이 바뀌는 것으로 충분하다.
                // 알약 표시는 `selected` 를 보므로 그대로 남는다
                interactionSource = NoInteraction,
                icon = {
                    Icon(
                        painter = painterResource(drawableOf(tab, filled = picked)),
                        contentDescription = null, // 라벨이 바로 아래에 있다
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(tab.label, fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.brand,
                    selectedTextColor = colors.brand,
                    // 표준 알약 표시는 그대로 두되 색만 브랜드로 옅게 깐다
                    indicatorColor = colors.brand.copy(alpha = 0.12f),
                    unselectedIconColor = colors.inkTertiary,
                    unselectedTextColor = colors.inkTertiary,
                ),
            )
        }
    }
}

/**
 * 아직 안 만든 탭 — **임시다**
 *
 * 빈 화면으로 두면 하단바를 눌렀을 때 앱이 멈춘 것처럼 보인다.
 * 화면이 생기면 지운다. 글꼴·타입 스케일이 정해지기 전이라 크기를 직접 적었다.
 */
@Composable
private fun ComingSoon(tab: MainTab) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "${tab.label} — 준비 중",
            color = HifisTheme.colors.inkTertiary,
            fontSize = 15.sp,
        )
    }
}
