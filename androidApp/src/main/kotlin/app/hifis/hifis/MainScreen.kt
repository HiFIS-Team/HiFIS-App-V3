package app.hifis.hifis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.ai.AiChatScreen
import app.hifis.hifis.attendance.AttendanceScanScreen
import app.hifis.hifis.search.SearchOverlay
import app.hifis.hifis.home.HomeScreen
import app.hifis.hifis.ui.component.AiChatButton
import app.hifis.hifis.more.MoreScreen
import app.hifis.hifis.schedule.ScheduleScreen
import app.hifis.hifis.ui.NoInteraction
import app.hifis.hifis.ui.component.TabPage
import app.hifis.hifis.ui.theme.Dimens
import android.app.Activity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
    // AI 페이지는 **탭이 아니라 덮고 올라오는 자리**다 — 화면을 돌려도 열린 채로 남는다
    var aiOpen by rememberSaveable { mutableStateOf(false) }
    // 검색도 **덮기만 한다** — 화면을 갈아 끼우지 않으니 닫으면 하던 자리로 돌아온다
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    // 출퇴근 스캔은 **옆에서 밀려 들어오는 상세 화면**이다 — 헤더의 스캔 아이콘이 연다
    var scanOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(HifisTheme.colors.background),
    ) {
        Box(Modifier.weight(1f)) {
            when (selected) {
                MainTab.HOME -> HomeScreen(
                    onSearch = { searchOpen = true },
                    onScan = { scanOpen = true },
                )
                MainTab.SCHEDULE -> ScheduleScreen(
                    onSearch = { searchOpen = true },
                    onScan = { scanOpen = true },
                )
                // 전체 목록에서 하단바에 자리가 있는 화면을 누르면 **그 탭으로 옮긴다**
                MainTab.MORE -> MoreScreen(
                    onTab = { index = MainTab.all.indexOf(it) },
                    onSearch = { searchOpen = true },
                    onScan = { scanOpen = true },
                )
                // 나머지는 아직 화면이 없다
                MainTab.WORK,
                MainTab.ATTENDANCE,
                -> ComingSoon(
                    selected,
                    onSearch = { searchOpen = true },
                    onScan = { scanOpen = true },
                )
            }

            // **탭이 아니라 셸이 들고 있다** — 다섯 곳에 다 떠 있어야 한다.
            // 하단바는 이 Box 밖(아래)이라 겹칠 일이 없다
            AiChatButton(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Dimens.aiChatMargin),
            ) { aiOpen = true }
        }
        MainBottomBar(selected) { index = MainTab.all.indexOf(it) }
    }

    // **셸 위로 통째로 덮는다.** 하단바까지 가려야 딴 자리로 넘어온 것이 된다.
    // 아래에서 올라왔다가 아래로 내려간다 — iOS 의 `coverVertical` 과 같은 결이다
    AnimatedVisibility(
        visible = aiOpen,
        enter = slideInVertically(tween(320)) { it },
        exit = slideOutVertically(tween(260)) { it },
    ) {
        // **이 화면만 밝다.** 앱은 다크로 못 박혀 있지만 여기는 예외라 여기서 갈라 준다
        HifisTheme(dark = false) {
            AiChatScreen(onClose = { aiOpen = false })
        }
    }

    // **상세 화면은 옆에서 밀려 들어온다.** 오른쪽에서 왔다가 오른쪽으로 돌아간다 —
    // iOS 의 내비게이션 push 와 같은 결이다. 하단바까지 덮는다 (`DESIGN.md`)
    AnimatedVisibility(
        visible = scanOpen,
        enter = slideInHorizontally(tween(320)) { it },
        exit = slideOutHorizontally(tween(260)) { it },
    ) {
        AttendanceScanScreen(onBack = { scanOpen = false })
    }

    // 검색은 **헤더 아래로 내려오는 판**이라 하단바까지 덮는다
    if (searchOpen) SearchOverlay(onClose = { searchOpen = false })

    // 밝은 화면이 떠 있는 동안에는 상태바 글자도 어둡게 — 안 그러면 흰 바탕에 흰 시계다
    SystemBarsForAi(light = aiOpen)
}

/**
 * AI 페이지가 떠 있는 동안 시스템 바 글자를 뒤집는다
 *
 * 앱은 늘 어두워서 `MainActivity` 가 바 글자를 밝게 못 박아 뒀다. 그 위로 **밝은 화면**이
 * 올라오면 흰 바탕에 흰 글자가 되어 시계·배터리가 사라진다.
 */
@Composable
private fun SystemBarsForAi(light: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    LaunchedEffect(light) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = light
            isAppearanceLightNavigationBars = light
        }
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
private fun ComingSoon(tab: MainTab, onSearch: () -> Unit, onScan: () -> Unit) {
    // **`TabPage` 를 쓴다.** 헤더가 거기 있어서, 안 쓰면 이 두 탭에서만
    // 검색·사내톡·알림으로 갈 방법이 사라진다 (iOS `ComingSoonView` 도 같다)
    TabPage(onSearch = onSearch, onScan = onScan) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "${tab.label} — 준비 중",
                color = HifisTheme.colors.inkTertiary,
                fontSize = 15.sp,
            )
        }
    }
}
