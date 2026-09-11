package app.hifis.shared.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 제품 목록은 **양 플랫폼이 하나만 읽는다** — 고르개가 플랫폼마다 다르면 안 된다.
 *
 * [MainTab] 이 V2 에서 세 군데로 갈려 화면이 새어 나갔던 것과 같은 자리다.
 */
class ProductTest {

    @Test
    fun `제품은 셋이고 HiFIS 에서 시작한다`() {
        assertEquals(3, Product.all.size, "제품이 셋이 아니다: ${Product.all}")
        assertEquals(Product.HIFIS, Product.default)
        // 고르개 첫 칸이 시작 자리여야 한다 — 아니면 켜자마자 알약이 가운데 있다
        assertEquals(Product.default, Product.all.first())
    }

    @Test
    fun `이름이 겹치지 않는다`() {
        assertEquals(Product.labels.size, Product.labels.toSet().size, "${Product.labels}")
        assertEquals(Product.all.map { it.label }, Product.labels)
    }

    @Test
    fun `이름은 고르개 한 줄에 들어갈 만큼 짧다`() {
        // 셋이 한 알약 줄에 나란히 선다. 길어지면 글자가 잘리거나 줄이 넘친다
        Product.labels.forEach {
            assertTrue(it.length <= 8, "제품 이름이 너무 길다: $it")
            assertTrue(it.isNotBlank())
        }
    }

    @Test
    fun `안 만든 제품은 탭과 같은 말을 쓴다`() {
        assertEquals("TeamFIS — 준비 중", Product.comingSoon(Product.TEAMFIS))
        assertEquals("WeFIS — 준비 중", Product.comingSoon(Product.WEFIS))
    }
}
