package app.hifis.shared.work

/**
 * 공통 업무 점검 항목 하나 (서버 `EnvItem` 이 올 자리)
 *
 * **지점마다 다르다.** 무엇을 점검할지는 지점이 정하고, 서버가 그 지점 것만 보내 준다.
 * 그래서 화면이 항목을 박아 두지 않는다 — 목록을 받아서 그대로 세운다.
 *
 * V2 는 여기에 `points`(항목당 기여 점수)·`editable`·`sortOrder` 도 들고 있었다.
 * **목록 화면이 안 쓰는 값이라 안 가져왔다** — 점수를 보여주는 자리가 생기면 그때 받는다.
 */
data class EnvItem(val id: String, val name: String) {
    companion object {
        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 서버가 붙으면 지운다. 이 값을 보고 서버에 칸을 만들지 않는다.
         * 글자 수가 짧은 것과 긴 것을 섞어 뒀다 — 칩 글자 크기가 제일 긴 이름에
         * 맞춰지는지 보려면 긴 것이 하나는 있어야 한다.
         */
        val demo = listOf(
            EnvItem("e1", "환경정비"),
            EnvItem("e2", "화장실청소"),
            EnvItem("e3", "기구정리"),
            EnvItem("e4", "수건정리"),
            EnvItem("e5", "클레임해결"),
            EnvItem("e6", "신규상담"),
        )
    }
}

/**
 * 업무 화면 문구 — 두 플랫폼이 **같은 말**을 한다
 *
 * 업무 탭에는 **공통 업무와 내 업무만** 든다 (2026-09-10 대표 결정).
 * 동료 평가·회원 친절도·센터 기여도는 V2 에서 여기 탭으로 같이 있었는데,
 * 매일 하는 일과 가끔 보는 것이 한 줄에 서 있어서 매일 하는 사람이 매일 한 번 더 골랐다.
 * 수업 개수는 PT 라 TeamFIS 로 간다 — 이 앱에 없다.
 *
 * **지금은 공통 업무만 있다.** 내 업무가 생기면 둘을 고르는 칸이 위에 붙는다.
 */
object WorkBoard {
    const val TITLE = "업무"

    /** 위에 붙을 두 칸 — 내 업무가 생길 때 쓴다 */
    const val COMMON = "공통 업무"
    const val MINE = "내 업무"

    /** 칩 격자의 머리말 */
    const val TODAY_ITEMS = "오늘 점검 항목"

    /** 그 머리말 오른쪽 — 누르면 오늘 수행 내역이 열린다 */
    fun totalLabel(total: Int): String = "총 ${total}회"

    /** 점검 항목이 하나도 없다 — 지점이 아직 안 정했다 */
    const val EMPTY = "점검 항목이 아직 없어요."

    /** 오늘 몇 번 했는지 다 더한다 — 머리말 오른쪽 숫자 */
    fun total(counts: Map<String, Int>): Int = counts.values.sum()
}
