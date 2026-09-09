package app.hifis.hifis.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.hifis.shared.nav.HomeShortcut

/**
 * 바로가기 칸을 가르는 색 — **여기서만 쓴다**
 *
 * [HifisColors] 와 따로 둔 것은 성격이 달라서다. 저쪽은 **뜻**이 있는 색이고
 * (`success` = 잘 돌아간다, `danger` = 문제), 이건 **뜻이 없는 표식**이다.
 * 글자를 안 읽고도 찾던 것을 집으라고 색을 다르게 줄 뿐이다.
 *
 * > ⚠️ **다른 화면으로 가져가지 않는다.** 여섯 색이 앱 곳곳에 퍼지면
 * > 브랜드 파랑 하나로 강조하던 규칙이 무너진다.
 *
 * 상태색(`success`·`warning`·`danger`)과 색이 비슷한 칸이 있지만 자리가 다르다 —
 * 상태색은 **배지**에만 나오고 이건 **아이콘 네모**에만 나온다.
 */
@Composable
fun tintOf(shortcut: HomeShortcut): Color {
    val dark = HifisTheme.colors.isDark
    return when (shortcut) {
        HomeShortcut.PROJECT -> if (dark) Color(0xFF8D98FF) else Color(0xFF5B6BF0)
        HomeShortcut.MEETING -> if (dark) Color(0xFF3FC7D2) else Color(0xFF12A5B0)
        HomeShortcut.APPROVAL -> if (dark) Color(0xFFB08CFF) else Color(0xFF8A5CF0)
        HomeShortcut.STAFF -> if (dark) Color(0xFF4FC98C) else Color(0xFF2AA76A)
        HomeShortcut.SALARY -> if (dark) Color(0xFFFFB055) else Color(0xFFE8912A)
        HomeShortcut.NOTICE -> if (dark) Color(0xFFFF8DA0) else Color(0xFFE85D75)
    }
}

/**
 * 색 면을 얼마나 옅게 까나 — 상태 배지와 **같은 값**이다
 *
 * 다크는 어두운 면 위라 같은 값이면 안 보여서 조금 더 준다.
 */
@Composable
fun tintFillAlpha(): Float = if (HifisTheme.colors.isDark) 0.20f else 0.12f
