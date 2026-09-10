package app.hifis.hifis.ai

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.shared.ai.AiPrompt
import kotlinx.coroutines.delay

/**
 * AI 채팅 — **아래에서 올라오는 페이지**
 *
 * 떠 있는 AI 단추를 누르면 탭이 바뀌는 대신 이 화면이 통째로 덮고 올라온다.
 * 탭을 옮기면 지금 보던 화면을 잃는데, AI 는 **하던 일을 두고 잠깐 묻는 자리**라
 * 덮고 올라왔다가 닫히는 편이 맞다. iOS 도 같은 결이다 (거기는 탭바가 그 자리를 준다).
 *
 * ## 이 화면만 **밝다**
 *
 * 앱은 다크로 못 박아 두었지만(`DESIGN.md`) 여기는 예외다. 대화 화면은 글자가
 * 길게 이어져서 밝은 바탕이 읽기 편하고, **덮고 올라오는 딴 자리**라 앱 색이
 * 끊겨도 어색하지 않다. 밝기는 [HifisTheme] 을 `dark = false` 로 감싸 낸다 —
 * 부르는 쪽(`MainScreen`)이 그 일을 한다.
 *
 * **유리는 안 쓴다.** iOS 는 입력칸이 시스템 유리인데, 여기서 흉내내면 늘 가짜가 된다
 * (`DESIGN.md` 의 리퀴드 글래스 규칙). 대신 `surface` 면에 `line` 테두리를 두른다.
 *
 * 값은 아직 자리 표시자다 — **AI 도 서버도 안 붙였다.** 보내기는 아무 일도 안 한다.
 */
@Composable
fun AiChatScreen(onClose: () -> Unit) {
    val colors = HifisTheme.colors
    var message by remember { mutableStateOf("") }
    // 글자는 **페이지가 다 올라온 뒤에** 하나씩 든다
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(SETTLE_MILLIS)
        shown = true
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.surface),
    ) {
        Glow(Modifier.align(Alignment.BottomCenter))
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = Dimens.screenEdge, end = Dimens.screenEdge, bottom = 12.dp),
        ) {
            CloseButton(onClose)
            // **글은 헤더 밑에 붙는다.** 빈 자리는 보기와 입력칸 사이로 내린다
            Spacer(Modifier.height(20.dp))
            Entrance(step = 0, shown = shown) { BrandRow() }
            Spacer(Modifier.height(14.dp))
            Entrance(step = 1, shown = shown) {
                Text(
                    AiPrompt.TITLE,
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                    color = colors.ink,
                )
            }
            Spacer(Modifier.height(28.dp))
            AiPrompt.all.forEachIndexed { index, prompt ->
                Entrance(step = 2 + index, shown = shown) {
                    PromptRow(prompt) { message = prompt.label }
                }
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            Entrance(step = 2 + AiPrompt.all.size, shown = shown) {
                InputBar(message) { message = it }
            }
        }
    }
}

/**
 * 바닥에서 옅게 번지는 빛 — **브랜드색 한 가지로만** 낸다
 *
 * 참고한 화면은 여러 색을 섞었는데, 우리는 강조를 브랜드 파랑 하나로 쓰기로 했다.
 */
@Composable
private fun Glow(modifier: Modifier = Modifier) {
    val brand = HifisTheme.colors.brand
    Box(
        modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(
                Brush.verticalGradient(
                    listOf(brand.copy(alpha = 0f), brand.copy(alpha = 0.14f)),
                ),
            ),
    )
}

/**
 * 닫기 — 그림이 화면 끝 [Dimens.screenEdge] 에 서야 한다
 *
 * 터치 자리(44)가 그림(22)보다 넓어서 **가운데 정렬**을 하고 그 차이만큼 왼쪽으로
 * 당긴다. 헤더가 하는 계산과 같다.
 */
@Composable
private fun CloseButton(onClose: () -> Unit) {
    Box(
        Modifier
            .padding(top = 4.dp)
            .offset(x = -Dimens.headerIconInset)
            .size(Dimens.headerIconButton)
            .clip(CircleShape)
            .tap(label = "닫기", onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_close),
            contentDescription = "닫기",
            tint = HifisTheme.colors.ink,
            modifier = Modifier.size(Dimens.headerIcon),
        )
    }
}

/** 마크 + 이름 — 마크는 **제 그라데이션 그대로** 선다 */
@Composable
private fun BrandRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painterResource(R.drawable.brand_mark),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.width(26.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            AiPrompt.BRAND,
            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            color = HifisTheme.colors.inkSecondary,
        )
    }
}

/**
 * 말 걸기 보기 한 줄 — **빈 자리는 안 눌린다**
 *
 * 누르는 자리를 줄 전체로 두면 글 옆 빈 곳을 스쳐도 입력칸이 채워진다.
 * 그래서 [Row] 를 폭에 안 맞추고 내용만큼만 두고, 남는 폭은 밖에서 [Spacer] 가 채운다.
 */
@Composable
private fun PromptRow(prompt: AiPrompt, onPick: () -> Unit) {
    val colors = HifisTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .height(Dimens.moreRow)
                .tap(label = prompt.label, onClick = onPick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(drawableOf(prompt)),
                contentDescription = null, // 바로 옆에 글자가 있다
                tint = colors.ink,
                modifier = Modifier.size(Dimens.moreIcon),
            )
            Spacer(Modifier.width(14.dp))
            Text(
                prompt.label,
                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
                color = colors.ink,
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun InputBar(message: String, onChange: (String) -> Unit) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            // **유리를 안 쓴다.** iOS 는 시스템 유리지만 여기서 흉내내면 가짜가 된다
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            BasicTextField(
                value = message,
                onValueChange = onChange,
                textStyle = TextStyle(fontSize = 16.sp, color = colors.ink),
                cursorBrush = SolidColor(colors.brand),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (message.isEmpty()) {
                Text(
                    AiPrompt.PLACEHOLDER,
                    style = TextStyle(fontSize = 16.sp),
                    color = colors.inkTertiary,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        SendButton(message.isNotEmpty())
    }
}

/**
 * 보내기 — **글자가 들어오면 파란 동그라미가 튀어 들어온다**
 *
 * V2 사내톡 입력칸이 하던 움직임을 그대로 옮겼다. 두 벌이 갈아 끼워지면서
 * 들어오는 쪽은 커지며 나타나고 나가는 쪽은 작아지며 사라진다.
 * 들어올 때 1을 살짝 넘겼다 돌아온다 — 튀는 스프링이 그 자리다.
 *
 * 비었을 때는 **동그라미를 안 그린다.** 회색 동그라미를 두면 죽은 단추가 놓인 것처럼 보인다.
 */
@Composable
private fun SendButton(active: Boolean) {
    val colors = HifisTheme.colors
    Box(Modifier.size(SEND_BUTTON), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = !active,
            enter = scaleIn(POP) + fadeIn(),
            exit = scaleOut(tween(160)) + fadeOut(tween(160)),
        ) {
            Icon(
                painterResource(R.drawable.ic_send),
                contentDescription = null,
                tint = colors.inkTertiary,
                modifier = Modifier.size(SEND_ICON),
            )
        }
        AnimatedVisibility(
            visible = active,
            enter = scaleIn(POP) + fadeIn(),
            exit = scaleOut(tween(160)) + fadeOut(tween(160)),
        ) {
            Box(
                Modifier
                    .size(SEND_BUTTON)
                    .background(colors.brand, CircleShape)
                    .tap(label = "보내기") {
                        // 아직 보낼 곳이 없다 — AI 도 서버도 안 붙였다
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_send_fill),
                    contentDescription = "보내기",
                    tint = Color.White,
                    modifier = Modifier.size(SEND_ICON),
                )
            }
        }
    }
}

/**
 * 하나씩 **떠오르듯 든다** — 한 번만, 순서대로
 *
 * 다 같이 나타나면 화면이 한 번에 꽉 차서 어디를 봐야 할지 모른다.
 * 마크 → 물음 → 보기 넷 → 입력칸 순으로 조금씩 늦춰 눈이 따라가게 한다.
 *
 * iOS 는 흐림까지 같이 푸는데(`AiChatView`), 안드로이드는 **투명도와 이동만** 쓴다 —
 * 컴포즈에서 흐림은 API 31 부터라 하한(26)에서 조용히 아무 일도 안 한다.
 */
@Composable
private fun Entrance(step: Int, shown: Boolean, content: @Composable () -> Unit) {
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
            visibilityThreshold = 0.001f,
        ),
        label = "entrance$step",
    )
    // 늦춤은 스프링에 못 주므로 걸음마다 시작을 따로 연다
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(shown) {
        if (shown) {
            delay(step * STAGGER_MILLIS)
            started = true
        }
    }
    val value = if (started) progress else 0f
    Box(
        Modifier
            .alpha(value)
            .offset(y = ENTRANCE_RISE * (1f - value)),
    ) { content() }
}

/** 아래에서 떠오르는 거리 */
private val ENTRANCE_RISE = 16.dp

/** 페이지가 올라오는 것과 안 겹치게 기다리는 시간 */
private const val SETTLE_MILLIS = 220L

/** 한 걸음 늦추는 간격 */
private const val STAGGER_MILLIS = 60L

/** 보내기 동그라미와 그 안의 비행기 — iOS·V2 와 같은 값이다 */
private val SEND_BUTTON = 38.dp
private val SEND_ICON = 20.dp

/** 튀어 들어오는 스프링 — V2 의 `easeOutBack` 자리 */
private val POP = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

/**
 * 보기 아이콘을 그림 자원으로 바꾼다
 *
 * **`when` 이 enum 을 다 덮어야 컴파일된다** — 보기를 추가하면 여기서 걸린다.
 */
private fun drawableOf(prompt: AiPrompt): Int = when (prompt) {
    AiPrompt.TODAY_TASKS -> R.drawable.ic_work
    AiPrompt.BOOK_SCHEDULE -> R.drawable.ic_schedule
    AiPrompt.FIND_MEETING -> R.drawable.ic_meeting
    AiPrompt.DRAFT_APPROVAL -> R.drawable.ic_approval
}
