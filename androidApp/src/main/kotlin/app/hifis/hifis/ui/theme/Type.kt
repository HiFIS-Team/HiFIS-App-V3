package app.hifis.hifis.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 글자 크기 한 벌 — **색은 안 들어 있다.** 쓰는 자리에서 토큰으로 준다
 *
 * 같은 크기라도 자리에 따라 색이 달라서 (`ink` · `inkSecondary` · `brand`),
 * 색까지 묶으면 스타일이 크기×색 만큼 늘어난다.
 *
 * ## 글꼴은 **각 OS 기본**이다 — Pretendard 를 안 넣는다
 *
 * V2 는 Pretendard 를 번들에 넣어 두 플랫폼을 맞췄다. V3 는 안 그런다 —
 * 안드로이드는 Roboto·Noto Sans KR, iOS 는 SF Pro·Apple SD Gothic Neo 를 그대로 쓴다.
 * 하단바를 각자 OS 표준으로 둔 것과 같은 결이고, 글꼴 4개(6MB)도 안 진다.
 */
object HifisType {
    /** 시계처럼 크게 세우는 숫자 */
    val display = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, lineHeight = 44.sp)

    /** 화면 제목 — 헤더 바로 아래 한 줄 */
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)

    /** 값 — 스캔 시각처럼 읽어야 하는 것 */
    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)

    /** 머리말·강조 한 줄 */
    val label = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)

    /** 잎 헤더의 화면 이름 — 뒤로가기 옆 한 줄. `title`(22) 은 56 줄에 넣기엔 크다 */
    val header = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)

    /** 곁들이는 글자 */
    val caption = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp)

    /**
     * 숫자가 자리를 지키게 한다 — **시계에 반드시 쓴다**
     *
     * 안 쓰면 `1` 이 좁아서 매초 시계 폭이 흔들린다.
     */
    const val TABULAR = "tnum"
}
