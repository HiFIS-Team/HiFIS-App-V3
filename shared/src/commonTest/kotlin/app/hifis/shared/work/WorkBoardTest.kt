package app.hifis.shared.work

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkBoardTest {
    @Test
    fun `총 횟수는 항목 것을 다 더한다`() {
        assertEquals(0, WorkBoard.total(emptyMap()))
        assertEquals(7, WorkBoard.total(mapOf("e1" to 3, "e2" to 0, "e3" to 4)))
        assertEquals("총 7회", WorkBoard.totalLabel(7))
        assertEquals("총 0회", WorkBoard.totalLabel(0))
    }

    @Test
    fun `데모 항목은 이름이 겹치지 않는다`() {
        val names = EnvItem.demo.map { it.name }
        assertEquals(names.size, names.toSet().size)
        assertEquals(EnvItem.demo.size, EnvItem.demo.map { it.id }.toSet().size)
    }

    @Test
    fun `칩 글자 크기를 재려면 긴 이름이 하나는 있어야 한다`() {
        assertTrue(EnvItem.demo.any { it.name.length >= 5 })
    }
}
