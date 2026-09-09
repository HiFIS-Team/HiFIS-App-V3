package app.hifis.hifis.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.painterResource
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 헤더에 서는 아이콘 버튼 — 테두리도 배경도 없다
 *
 * **터치 자리와 그림 크기가 다르다** ([Dimens.headerIconButton] 44 / [Dimens.headerIcon] 22).
 * 손가락이 닿는 넓이는 44 로 두고 보이는 것만 22 다.
 *
 * 물결은 머티리얼 것을 그대로 쓴다 — 안드로이드에서 눌린 느낌이 나야 하는데
 * 직접 그리면 OS 기본과 미묘하게 어긋난다.
 */
@Composable
fun HeaderIconButton(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** 걸린 상태 (지점을 하나 고른 중 등) — 그림이 브랜드색으로 바뀐다 */
    active: Boolean = false,
    /** 안 읽음 점. **숫자는 안 쓴다** — 몇 개인지는 들어가서 본다 */
    badge: Boolean = false,
) {
    val colors = HifisTheme.colors
    Box(
        modifier = modifier
            .size(Dimens.headerIconButton)
            .tap(label = label, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(Dimens.headerIcon)) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = if (active) colors.brand else colors.ink,
                modifier = Modifier.size(Dimens.headerIcon),
            )
            if (badge) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        // 아이콘 그림 밖으로 조금 나와 앉는다 — 안에 두면 선과 겹쳐 뭉갠다
                        .offset(x = Dimens.badgeRing * 2, y = -Dimens.badgeRing)
                        .size(Dimens.badgeDot + Dimens.badgeRing * 2)
                        // 바탕색 원이 먼저다. 아이콘 선 위에 겹쳐도 점이 또렷하게 뜬다
                        .background(colors.surface, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    // **테두리(border)로 두르지 않는다.** 빨간 원 위에 테를 얹으면
                    // 테의 바깥 안티에일리어싱 틈으로 아래 빨강이 비쳐 분홍 테가 생긴다.
                    // 큰 원 안에 작은 원을 넣으면 두 색이 겹치는 자리가 없다
                    Box(
                        Modifier
                            .size(Dimens.badgeDot)
                            .background(colors.danger, CircleShape),
                    )
                }
            }
        }
    }
}
