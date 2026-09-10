package app.hifis.hifis.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 떠 있는 AI 채팅 단추 — **탭 다섯 곳에 다 뜬다**
 *
 * 그래서 화면이 아니라 **셸(`MainScreen`)이 들고 있다.** 화면마다 얹으면
 * 언젠가 한 탭만 빠지는데, 늘 같은 자리에 있는 것이 이 단추의 전부다.
 * (업무·근태는 아직 `ComingSoon` 이라 껍데기조차 안 쓴다 — 셸에 두면 그것도 덮는다.)
 *
 * **그림자를 쓰는 유일한 자리다.** 화면 안은 평평하게 가지만 이건 본문 위로
 * 떠 있어야 해서, 굴러 올라오는 카드와 겹칠 때 층이 안 갈리면 얹힌 것처럼 안 보인다.
 *
 * 브랜드색을 면으로 깐다 — 강조 하나를 여기 쓴다.
 */
@Composable
fun AiChatButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(Dimens.aiChatButton)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(HifisTheme.colors.brand, CircleShape)
            .tap(label = "AI 채팅", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_ai),
            contentDescription = "AI 채팅",
            tint = Color.White,
            modifier = Modifier.size(Dimens.aiChatIcon),
        )
    }
}
