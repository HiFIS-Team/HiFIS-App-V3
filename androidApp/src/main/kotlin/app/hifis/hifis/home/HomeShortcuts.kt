package app.hifis.hifis.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.tintFillAlpha
import app.hifis.hifis.ui.theme.tintOf
import app.hifis.shared.nav.HomeShortcut

/**
 * 홈 바로가기 — 하단바로 못 가는 여섯 화면
 *
 * 목록은 `shared` 의 [HomeShortcut] 하나만 읽는다. 여기서 새로 세우지 않는다.
 *
 * **머리말을 안 붙였다.** 아이콘과 글자가 스스로 무엇인지 말하고 있어서,
 * `바로가기` 한 줄을 더 얹으면 카드가 그만큼 길어지기만 한다.
 */
@Composable
fun HomeShortcuts(
    onOpen: (HomeShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.cardRadius)

    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            // 좌우는 카드 기본값보다 좁다 — 칸이 스스로 가운데를 잡아서
            // 24 를 주면 아이콘이 안쪽으로 지나치게 몰린다
            .padding(horizontal = 12.dp, vertical = Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HomeShortcut.android.chunked(COLUMNS).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { shortcut ->
                    ShortcutItem(shortcut, onOpen, Modifier.weight(1f))
                }
                // 마지막 줄이 덜 찼을 때 남은 칸을 비워 둔다 — 안 그러면 가운데로 퍼진다
                repeat(COLUMNS - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** 한 줄에 세우는 칸 수 */
private const val COLUMNS = 3

@Composable
private fun ShortcutItem(
    shortcut: HomeShortcut,
    onOpen: (HomeShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    val tint = tintOf(shortcut)
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .tap(label = shortcut.label) { onOpen(shortcut) }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(Dimens.shortcutChip)
                // **면에도 색을 깐다.** 회색 네모에 아이콘만 색을 주면 한눈에는
                // 여섯이 다 같은 상자로 보여서 색이 하는 일이 거의 없다 (둘 다 만들어 대 봤다)
                .background(
                    tint.copy(alpha = tintFillAlpha()),
                    RoundedCornerShape(Dimens.shortcutChipRadius),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(drawableOf(shortcut)),
                contentDescription = null, // 바로 아래에 글자가 있다
                tint = tint,
                modifier = Modifier.size(Dimens.shortcutIcon),
            )
        }
        Text(
            text = shortcut.label,
            // 시계·스캔 시각과 같은 결로 진하게 — 아이콘만 보고 못 찾을 때 읽는 글자다
            style = HifisType.caption.copy(fontWeight = FontWeight.SemiBold),
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/**
 * 아이콘 이름을 그림 자원으로 바꾼다
 *
 * **`when` 이 enum 을 다 덮어야 컴파일된다** — 바로가기를 추가하면 여기서 걸린다.
 */
private fun drawableOf(shortcut: HomeShortcut): Int = when (shortcut) {
    HomeShortcut.PROJECT -> R.drawable.ic_project
    HomeShortcut.MEETING -> R.drawable.ic_meeting
    HomeShortcut.APPROVAL -> R.drawable.ic_approval
    HomeShortcut.STAFF -> R.drawable.ic_staff
    HomeShortcut.SALARY -> R.drawable.ic_salary
    HomeShortcut.NOTICE -> R.drawable.ic_notice
    // 근태는 **iOS 홈에만 선다** (`HomeShortcut.ios`). 안드로이드는 탭에 있어서
    // 안 그려지지만 `when` 은 enum 을 다 덮어야 한다
    HomeShortcut.ATTENDANCE -> R.drawable.ic_attendance
}
