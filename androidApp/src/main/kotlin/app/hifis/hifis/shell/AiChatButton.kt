package app.hifis.hifis.shell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
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
 * **iOS 는 이 자리가 탭바가 그리는 유리 동그라미다.** 여기서 유리를 흉내내면 늘 가짜가
 * 되므로 (`DESIGN.md` 의 리퀴드 글래스 규칙) `surface` 면에 `line` 테두리로 대신한다.
 * 안에 드는 그림은 **양쪽이 같다** — FS 마크가 제 그라데이션 그대로 선다.
 */
@Composable
fun AiChatButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    Box(
        modifier
            .size(Dimens.aiChatButton)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            // **면은 `surface` 다.** iOS 는 여기가 시스템 유리인데 흉내내면 가짜가 되므로
            // `DESIGN.md` 가 정한 대체로 간다. 테두리가 유리의 테를 대신해 윤곽을 준다
            .background(colors.surface, CircleShape)
            .border(1.dp, colors.line, CircleShape)
            .tap(label = "AI 채팅", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // **FS 마크가 제 그라데이션 그대로 선다** — iOS 탭바 동그라미와 같은 그림이다.
        // 브랜드색 면 위였다면 마크의 청록→보라와 부딪혀서 못 썼다
        Image(
            painterResource(R.drawable.brand_mark),
            contentDescription = "AI 채팅",
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(Dimens.aiChatMark),
        )
    }
}
