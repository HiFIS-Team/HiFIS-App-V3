package app.hifis.hifis.shell

import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.shared.nav.Branch
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 지금 보고 있는 지점과 고르개를 여는 길 — **셸이 들고 화면은 읽기만 한다**
 *
 * 화면마다 지점을 손으로 넘기지 않는다. 지점은 홈·업무·일정이 신경 쓸 것이 아니라
 * **그 화면들을 담고 있는 껍데기의 상태**다 ([LocalProduct] 와 같은 생각).
 *
 * @property id 고른 지점 — **null 이면 전 지점**
 */
data class BranchScope(val id: String?, val open: () -> Unit)

/** 헤더가 읽는 자리. 셸(`MainScreen`)이 값을 내려 준다 */
val LocalBranch = compositionLocalOf { BranchScope(null) {} }

/**
 * 지점 고르개 — **헤더 아래로 내려오는 판** (2026-09-11 대표)
 *
 * 검색판과 **같은 자리·같은 모양**이다 (`SearchOverlay`). 헤더 아래에서 내려오고,
 * 뒤는 옅게 덮이고, 판 밖을 누르면 닫힌다 — 헤더의 두 단추가 서로 다른 모양의 판을
 * 내면 같은 줄에서 나온 것처럼 안 보인다.
 *
 * **머리말(`지점`)을 붙인다.** 줄이 `전 지점 · 화순 · 첨단` 뿐이라 무엇을 고르는
 * 자리인지가 글자만으로는 안 드러난다 (V2 는 아이콘 메뉴라 머리말 없이 갔다).
 *
 * 고른 줄에는 **브랜드색 체크**가 선다. 줄을 통째로 칠하지 않는다 — 판 안에서
 * 면을 칠하면 카드처럼 보여서 누를 것이 하나 더 생긴 것처럼 읽힌다.
 */
@Composable
fun BranchOverlay(picked: String?, onPick: (String?) -> Unit, onClose: () -> Unit) {
    val colors = HifisTheme.colors
    // 아래 두 귀만 둥글다 — **헤더에 붙어 있는 판**이라 위는 각져야 이어져 보인다
    val shape = RoundedCornerShape(bottomStart = PANEL_RADIUS, bottomEnd = PANEL_RADIUS)
    val scope = rememberCoroutineScope()
    // 뜬 뒤에 켜야 **들어오는 애니메이션**이 걸린다 (처음부터 true 면 이미 다 내려온 상태다)
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    // 걷을 때도 올라가는 것을 보여 주고 나서 치운다 — 툭 사라지면 헤더로 되말린 것이 안 보인다
    fun leave(then: () -> Unit) {
        shown = false
        scope.launch {
            delay(EXIT_MS)
            then()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // **헤더는 안 덮는다.** 판이 거기서 나온 것처럼 보여야 해서 그 줄만 밝게 남긴다
        Spacer(Modifier.height(Dimens.headerHeight))
        Box(Modifier.fillMaxSize()) {
            // 판 밖을 누르는 것이 나가는 가장 빠른 길이다 (검색판과 같은 규칙)
            androidx.compose.animation.AnimatedVisibility(
                visible = shown,
                enter = fadeIn(tween(SCRIM_MS)),
                exit = fadeOut(tween(SCRIM_MS)),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(colors.background.copy(alpha = SCRIM_ALPHA))
                        .tap(label = "지점 고르개 닫기", onClick = { leave(onClose) }),
                )
            }
            // **위에서 펼쳐진다.** 자리를 옮기는 것이 아니라 헤더 뒤에서 풀려 나오는 결이다
            androidx.compose.animation.AnimatedVisibility(
                visible = shown,
                enter = expandVertically(tween(ENTER_MS), Alignment.Top),
                exit = shrinkVertically(tween(EXIT_MS.toInt()), Alignment.Top),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colors.surface, shape),
                ) {
                    // 헤더와 판이 같은 면이라 **가르는 줄**이 없으면 어디까지가 헤더인지 안 보인다
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.line),
                    )
                    Text(
                        Branch.TITLE,
                        fontSize = 13.sp,
                        color = colors.inkTertiary,
                        modifier = Modifier.padding(
                            start = Dimens.screenEdge,
                            top = 16.dp,
                            bottom = 4.dp,
                        ),
                    )
                    // **전 지점이 맨 위다.** 안 고른 상태라 첫 줄에 있어야 되돌리기 쉽다
                    BranchRow(Branch.ALL, picked == null) { leave { onPick(null) } }
                    Branch.demo.forEach { branch ->
                        BranchRow(branch.name, picked == branch.id) { leave { onPick(branch.id) } }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun BranchRow(name: String, picked: Boolean, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(ROW_HEIGHT)
            .tap(label = name, onClick = onClick)
            .padding(horizontal = Dimens.screenEdge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            fontSize = 16.sp,
            // 고른 줄만 굵게 — 체크가 안 보이는 자리에서도 어디인지 읽힌다
            fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (picked) colors.ink else colors.inkSecondary,
        )
        Spacer(Modifier.weight(1f))
        if (picked) {
            Icon(
                painterResource(R.drawable.ic_check),
                contentDescription = "고름",
                tint = colors.brand,
                modifier = Modifier.size(Dimens.headerIcon),
            )
        }
    }
}

/** 아래 두 귀 — 검색판과 같은 값이다 */
private val PANEL_RADIUS = 20.dp

/** 줄 하나 높이 */
private val ROW_HEIGHT = 52.dp

/** 펼쳐지는 데 걸리는 시간 — 잎(320)보다 짧다. 헤더에 붙은 판이라 길면 굼떠 보인다 */
private const val ENTER_MS = 240

/** 되말리는 시간 — 나가는 것은 늘 더 빠르다 (잎도 320 → 260) */
private const val EXIT_MS = 180L

/** 뒤가 어두워지는 시간 — 판보다 먼저 깔린다 */
private const val SCRIM_MS = 160

/** 뒤를 덮는 진하기 — 검색판과 같은 값이다 (무엇이 있었는지는 보여야 한다) */
private const val SCRIM_ALPHA = 0.55f
