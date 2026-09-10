package app.hifis.shared.work

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkBoardTest {
    @Test
    fun `총 횟수는 항목 것을 다 더한다`() {
        assertEquals(0, WorkBoard.total(emptyMap()))
        assertEquals(7, WorkBoard.total(mapOf("e-wash" to 3, "e-dry" to 0, "e-fold" to 4)))
        assertEquals("총 7회", WorkBoard.totalLabel(7))
        assertEquals("총 0회", WorkBoard.totalLabel(0))
    }

    @Test
    fun `기본 항목표는 이름도 자리도 안 겹친다`() {
        assertEquals(EnvItem.base.size, EnvItem.base.map { it.name }.toSet().size)
        assertEquals(EnvItem.base.size, EnvItem.base.map { it.id }.toSet().size)
    }

    /**
     * **차례가 하루 일하는 흐름이다** — 빨래 → 청소 → 관리 → 홍보 → 기타.
     *
     * 배점 순으로 늘어놓으면 현수막이 청소 사이에 끼고 화장실청소가 홍보 뒤로 간다.
     * 항목을 더하다가 묶음 사이에 끼워 넣으면 여기서 걸린다.
     */
    @Test
    fun `항목은 일하는 흐름 차례로 선다`() {
        val order = EnvItem.base.map { it.group }
        assertEquals(order.sortedBy { it.ordinal }, order)
        // 다섯 묶음이 다 한 번씩 나온다 — 하나가 통째로 빠지면 잡는다
        assertEquals(EnvGroup.entries.toSet(), order.toSet())
    }

    @Test
    fun `칩 글자 크기를 재려면 긴 이름이 하나는 있어야 한다`() {
        assertTrue(EnvItem.base.any { it.name.length >= 5 })
    }

    @Test
    fun `내 업무는 체크한 만큼 찬다`() {
        val tasks = MyTask.demo
        assertEquals(2, WorkBoard.doneCount(tasks))
        assertEquals("2/5", WorkBoard.progressLabel(tasks))
        assertEquals(0.4f, WorkBoard.progress(tasks))
    }

    @Test
    fun `다 하면 숫자 대신 완료가 뜬다`() {
        val all = MyTask.demo.map { it.check() }
        assertEquals("완료", WorkBoard.progressLabel(all))
        assertEquals(1f, WorkBoard.progress(all))
    }

    @Test
    fun `할 일이 없으면 0 으로 나누지 않는다`() {
        assertEquals(0f, WorkBoard.progress(emptyList()))
        assertEquals("0/0", WorkBoard.progressLabel(emptyList()))
    }

    @Test
    fun `체크는 되돌릴 수 없다`() {
        val task = MyTask.demo.first { !it.checked }
        assertTrue(task.check().checked)
    }
}
