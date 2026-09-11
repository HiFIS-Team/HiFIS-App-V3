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

    // ── 개인 업무 ──

    /** 수요일에 도는 것 — t1 오픈점검 · t2 상담기록 · t4 문의회신 · t5 시재 · t6 재고 */
    private fun wed() = MyTask.demo(today = 3)

    @Test
    fun `요일을 고르면 그날 도는 것만 선다`() {
        val tasks = MyTask.demo(today = 1)
        // 화요일엔 인바디 소독(화·목)이 서고 상담 기록(월·수·금)은 안 선다
        val tue = WorkBoard.tasksOf(tasks, 2).map { it.id }
        assertTrue("t3" in tue, "화요일에 인바디 소독이 없다: $tue")
        assertTrue("t2" !in tue, "화요일에 월수금 업무가 섰다: $tue")
        // 일요일은 아무도 안 돈다 — 빈 목록이지 오류가 아니다
        assertEquals(emptyList(), WorkBoard.tasksOf(tasks, 7))
    }

    @Test
    fun `체크는 누른 요일에만 찍힌다`() {
        // 월·수·금에 도는 업무를 수요일에 체크해도 월·금은 그대로 남는다
        val task = MyTask("x", "상담 기록", weekdays = setOf(1, 3, 5)).check(3)
        assertTrue(task.isChecked(3))
        assertTrue(!task.isChecked(1))
        assertTrue(!task.isChecked(5))
    }

    @Test
    fun `개인 업무는 체크한 만큼 찬다`() {
        val wed = WorkBoard.tasksOf(wed(), 3)
        assertEquals(5, wed.size)
        assertEquals(2, WorkBoard.doneCount(wed, 3))
        assertEquals("2/5", WorkBoard.progressLabel(wed, 3))
        assertEquals(0.4f, WorkBoard.progress(wed, 3))
    }

    @Test
    fun `다 하면 숫자 대신 완료가 뜬다`() {
        val all = WorkBoard.tasksOf(wed(), 3).map { it.check(3) }
        assertEquals("완료", WorkBoard.progressLabel(all, 3))
        assertEquals(1f, WorkBoard.progress(all, 3))
    }

    @Test
    fun `할 일이 없으면 0 으로 나누지 않는다`() {
        assertEquals(0f, WorkBoard.progress(emptyList(), 3))
        assertEquals("0/0", WorkBoard.progressLabel(emptyList(), 3))
    }

    @Test
    fun `체크는 되돌릴 수 없다`() {
        val task = WorkBoard.tasksOf(wed(), 3).first { !it.isChecked(3) }
        assertTrue(task.check(3).isChecked(3))
    }

    /**
     * **오늘만 체크할 수 있다.** 체크는 늘 오늘 날짜로 찍혀서, 다른 요일을
     * 보다 누르면 엉뚱한 날에 남는다.
     */
    @Test
    fun `다른 요일은 보기만 한다`() {
        assertTrue(WorkBoard.canCheck(day = 3, today = 3))
        assertTrue(!WorkBoard.canCheck(day = 2, today = 3))
        assertTrue(!WorkBoard.canCheck(day = 4, today = 3))
    }

    @Test
    fun `머리말은 오늘일 때만 오늘이라고 한다`() {
        assertEquals("오늘 할 일", WorkBoard.dayTitle(day = 3, today = 3))
        assertEquals("금요일 할 일", WorkBoard.dayTitle(day = 5, today = 3))
        assertEquals("오늘 할 일이 없어요.", WorkBoard.emptyLabel(day = 3, today = 3))
        assertEquals("일요일에 정한 업무가 없어요.", WorkBoard.emptyLabel(day = 7, today = 3))
    }

    /**
     * 요일 이름은 **ISO 차례**여야 한다 — 1=월 … 7=일.
     *
     * 일요일을 앞에 두는 차례(미국식)로 바꾸면 [WorkBoard.dayName] 이 통째로
     * 하루씩 밀리는데 화면은 멀쩡해 보인다. 근무표가 월요일에 시작한다.
     */
    @Test
    fun `요일 이름은 월요일에서 시작한다`() {
        assertEquals(7, WorkBoard.DAY_NAMES.size)
        assertEquals("월", WorkBoard.dayName(1))
        assertEquals("일", WorkBoard.dayName(7))
        assertEquals(WorkBoard.DAYS, listOf(1, 2, 3, 4, 5, 6, 7))
    }

    @Test
    fun `오늘은 이레 안에 있다`() {
        assertTrue(WorkBoard.today() in WorkBoard.DAYS, "오늘이 ${WorkBoard.today()} 로 나온다")
    }

    /** 요일 줄을 눌렀을 때 목록이 실제로 갈리는지 보려면 데모가 요일마다 달라야 한다 */
    @Test
    fun `데모는 요일마다 다른 목록을 준다`() {
        val tasks = MyTask.demo(today = 1)
        val byDay = WorkBoard.DAYS.map { day -> WorkBoard.tasksOf(tasks, day).map { it.id } }
        assertTrue(byDay.toSet().size >= 4, "요일별 목록이 너무 비슷하다: $byDay")
    }

    /** 지난 요일이 빈 채로 서 있으면 주 중간에 열었을 때 안 한 것처럼 보인다 */
    @Test
    fun `데모는 지난 요일을 끝낸 것으로 채운다`() {
        val tasks = MyTask.demo(today = 4)
        WorkBoard.DAYS.filter { it < 4 }.forEach { day ->
            val of = WorkBoard.tasksOf(tasks, day)
            if (of.isNotEmpty()) {
                assertEquals(WorkBoard.DONE_ALL, WorkBoard.progressLabel(of, day), "${day} 요일")
            }
        }
    }
}
