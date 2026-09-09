package app.hifis.hifis.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.home.Notice
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 오늘 소식 — 공지 몇 줄을 홈에 얹는다
 *
 * **필독 줄은 글자만 빨간 게 아니라 줄 바탕까지 옅게 물든다.** 목록을 훑을 때
 * 글자를 읽기 전에 "여기 하나 걸려 있다"가 먼저 보이라고 그렇게 한다.
 *
 * 머리말은 `오늘 근무` 카드와 **같은 크기·색**이다. 카드마다 머리말이 다르면
 * 홈이 여러 사람이 만든 것처럼 보인다.
 */
@Composable
fun TodayNewsCard(
    notices: List<Notice>,
    onOpen: (Notice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.cardRadius)

    // 날짜는 하루에 한 번만 바뀐다 — 매번 다시 만들지 않는다
    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern(Notice.DATE_PATTERN, Locale.KOREAN))
    }

    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .padding(Dimens.cardPadding),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "오늘 소식",
                style = HifisType.label,
                color = colors.inkSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(today, style = HifisType.caption, color = colors.inkTertiary)
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            notices.forEach { notice -> NoticeRow(notice, onOpen) }
        }
    }
}

@Composable
private fun NoticeRow(notice: Notice, onOpen: (Notice) -> Unit) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.rowRadius)

    // 필독이면 줄 바탕이 그 색으로 옅게 물든다. 아니면 평범한 회색 줄이다.
    // 배지(12%)보다 옅게 둔다 — 여기는 글자를 얹는 바닥이라 그만큼 진하면 글자가 흐려진다
    val fill = if (notice.pinned) {
        colors.danger.copy(alpha = if (colors.isDark) 0.14f else 0.07f)
    } else {
        colors.background
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill, shape)
            .clickable(onClickLabel = notice.title) { onOpen(notice) }
            .padding(horizontal = Dimens.rowPaddingH, vertical = Dimens.rowPaddingV),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (notice.pinned) {
            Text(
                Notice.PINNED_LABEL,
                style = HifisType.caption.copy(fontWeight = FontWeight.Bold),
                color = colors.danger,
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = notice.title,
            style = HifisType.body,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
