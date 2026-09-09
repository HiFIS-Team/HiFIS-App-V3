package app.hifis.hifis.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 화면이 쓰는 색 한 벌 — 라이트/다크가 **같은 이름**을 갖는다
 *
 * 화면에서는 `HifisTheme.colors.ink` 처럼 이름으로만 집는다.
 * `Color(0xFF...)` 를 화면 파일에 적으면 다크에서 그 자리만 안 따라온다.
 */
@Immutable
data class HifisColors(
    /** 화면 바닥 */
    val background: Color,
    /** 헤더·카드처럼 바닥 위에 올라오는 면 */
    val surface: Color,
    /** 본문 글자와 아이콘 */
    val ink: Color,
    /** 보조 설명 */
    val inkSecondary: Color,
    /** 흐린 값·플레이스홀더 */
    val inkTertiary: Color,
    /** 구분선·테두리 */
    val line: Color,
    /** 강조 — 화면당 한 곳에만 쓴다 */
    val brand: Color,
    /** 안 읽음 점·삭제 */
    val danger: Color,

    /** 잘 돌아간다 — 출근 중 */
    val success: Color,
    /** 짚어 볼 것 — 지각·조기 퇴근 */
    val warning: Color,

    /**
     * 게이지·진행 막대를 채우는 그라데이션 두 끝
     *
     * 단색으로 채우면 막대가 납작해 보인다. [brand] 를 기준으로 한 칸 밝게·어둡게 잡았다.
     */
    val brandGradientStart: Color,
    val brandGradientEnd: Color,

    val isDark: Boolean,
)

/**
 * 브랜드 파랑 — `assets/brand/logo.png` **마크 픽셀의 평균색**이다
 *
 * 눈으로 고른 값이 아니라 로고에서 뽑았다. 로고가 바뀌지 않는 한 이 값도 안 바뀐다.
 */
private val Brand = Color(0xFF217FE1)

/**
 * 다크 바닥용 브랜드 파랑 — [Brand] 를 그대로 쓰면 어두운 면에서 탁해진다
 *
 * 밝기만 올린 게 아니라 채도도 같이 잡아 대비를 맞췄다.
 */
private val BrandOnDark = Color(0xFF4A9BFF)

/**
 * 회색조는 **파랑 쪽으로 살짝 기울여** 놨다 (Hue 220 언저리)
 *
 * 중립 회색을 브랜드 파랑 옆에 놓으면 회색이 누렇게 뜬다.
 */
val LightColors = HifisColors(
    background = Color(0xFFF5F6F8),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF15181E),
    inkSecondary = Color(0xFF5A6272),
    inkTertiary = Color(0xFF949BA9),
    line = Color(0xFFE7E9EE),
    brand = Brand,
    danger = Color(0xFFF04452),
    success = Color(0xFF00C471),
    warning = Color(0xFFFF9F0A),
    brandGradientStart = Color(0xFF3590E7),
    brandGradientEnd = Color(0xFF1A6CDD),
    isDark = false,
)

/** 라이트를 뒤집은 게 아니다 — 브랜드·경고색은 어두운 바닥에 맞춰 따로 잡았다 */
val DarkColors = HifisColors(
    background = Color(0xFF0F1116),
    surface = Color(0xFF181B21),
    ink = Color(0xFFF2F4F7),
    inkSecondary = Color(0xFFA3AAB8),
    inkTertiary = Color(0xFF6B7280),
    line = Color(0xFF262A33),
    brand = BrandOnDark,
    danger = Color(0xFFFF6B76),
    success = Color(0xFF2FD98D),
    warning = Color(0xFFFFB340),
    brandGradientStart = Color(0xFF5FA9FF),
    brandGradientEnd = Color(0xFF2F86F5),
    isDark = true,
)
