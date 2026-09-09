package app.hifis.hifis.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * 누를 수 있는 자리 — **누름 효과(물결·회색 면)를 안 낸다**
 *
 * 누르면 화면이 넘어가는 것으로 충분하다. 물결이 번지고 화면이 바뀌면
 * 두 가지가 겹쳐 보인다. 요즘 앱들이 대개 이렇게 한다.
 *
 * **`clickable` 을 화면에서 직접 쓰지 말고 이걸 쓴다.** 한 자리만 빠뜨려도
 * 거기만 물결이 남고, 그런 건 그 화면을 열어 봐야 발견된다.
 *
 * > 하단바는 예외다 — OS 표준 부품(`NavigationBar` · `UITabBar`)이 제 것을 그린다.
 * > 거기는 눌린 칸 표시가 곧 반응이라 따로 뺄 것이 없다.
 */
@Composable
fun Modifier.tap(
    label: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClickLabel = label,
    role = role,
    onClick = onClick,
)

/**
 * 아무 일도 안 일어나는 상호작용 통로 — **OS 표준 부품의 누름 효과를 끌 때 쓴다**
 *
 * `NavigationBar` 같은 머티리얼 부품은 물결을 제 안에서 그린다. 밖에서 끌 자리가 없다.
 * 대신 **누름을 알려 주는 통로를 막으면** 그릴 일 자체가 없어진다 —
 * 부품은 그대로 두고 반응만 사라져서, 표준 부품을 버리지 않아도 된다.
 *
 * 고른 칸 표시(알약)는 `selected` 를 보므로 그대로 남는다.
 */
object NoInteraction : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) = Unit
    override fun tryEmit(interaction: Interaction) = true
}
