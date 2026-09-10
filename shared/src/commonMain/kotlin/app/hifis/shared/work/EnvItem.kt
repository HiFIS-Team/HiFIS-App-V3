package app.hifis.shared.work

/**
 * 공통 업무를 묶는 **하루 일하는 흐름**
 *
 * 배점 순이 아니다. 빨래를 돌리고, 청소하고, 관리하고, 홍보하고, 나머지 순이다.
 * 화면에 머리말로 뜨지는 않는다 — 칩이 그 차례대로 서는 것으로 충분하다.
 */
enum class EnvGroup(val title: String) {
    LAUNDRY("빨래"),
    CLEAN("청소"),
    CARE("관리"),
    PROMO("홍보"),
    ETC("기타"),
}

/**
 * 공통 업무 점검 항목 하나 (서버 `EnvItem`)
 *
 * **지점마다 다르다.** 무엇을 점검할지는 지점이 정하고 서버가 그 지점 것만 보내 준다.
 * 그래서 화면이 항목을 박아 두지 않는다 — 목록을 받아서 그대로 세운다.
 * [base] 는 서버가 지점에 항목이 없을 때 만들어 주는 **기본표**를 그대로 옮긴 것이다.
 *
 * @property points 항목당 기여 점수. **화면에 아직 안 띄운다** — 표의 일부라 같이 들고 온다
 */
data class EnvItem(
    val id: String,
    val name: String,
    val points: Int,
    val group: EnvGroup,
) {
    companion object {
        /**
         * 기본 환경정비 항목 — **서버 `BASE_ENV_ITEMS` 와 같은 표다** (이름·배점·차례)
         *
         * ## 차례를 임의로 바꾸지 않는다
         *
         * **하루 일하는 흐름**이다 (빨래 → 청소 → 관리 → 홍보 → 기타). 칩을 위에서
         * 아래로 훑으며 누르게 돼 있어서, 실제 일하는 차례와 다르면 손이 왔다 갔다 한다.
         * 배점 순으로 늘어놓으면 현수막이 청소 사이에 끼고 화장실청소가 홍보 뒤로 간다.
         *
         * 배점은 대표가 여러 번 손본 값이다 (빨래정리 3→2, 화장실청소 5→2, TM회원관리 1→5,
         * 스토리 3→2, 전단지 10→1, 블로그 10→3). **여기서 새로 정하지 않는다.**
         *
         * > 서버가 붙으면 이 표는 지운다. 그때부터 지점 것을 받아 쓴다.
         */
        val base = listOf(
            // 빨래 — 돌리고, 말리고, 갠다. 이 차례가 뒤집히면 안 된다
            EnvItem("e-wash", "세탁", 1, EnvGroup.LAUNDRY),
            EnvItem("e-dry", "건조기", 1, EnvGroup.LAUNDRY),
            EnvItem("e-fold", "빨래정리", 2, EnvGroup.LAUNDRY),
            // 청소 — 넓은 곳부터 탈의실, 화장실 순
            EnvItem("e-zone", "구역청소", 2, EnvGroup.CLEAN),
            EnvItem("e-hall", "복도청소", 2, EnvGroup.CLEAN),
            EnvItem("e-locker", "락커정리", 2, EnvGroup.CLEAN),
            EnvItem("e-mbooth", "남탈부스", 5, EnvGroup.CLEAN),
            EnvItem("e-mclean", "남탈청소", 2, EnvGroup.CLEAN),
            EnvItem("e-wbooth", "여탈부스", 5, EnvGroup.CLEAN),
            EnvItem("e-wclean", "여탈청소", 2, EnvGroup.CLEAN),
            EnvItem("e-toilet", "화장실청소", 2, EnvGroup.CLEAN),
            // 관리
            EnvItem("e-gear", "기구관리", 2, EnvGroup.CARE),
            EnvItem("e-guide", "회원지도", 2, EnvGroup.CARE),
            EnvItem("e-tm", "TM회원관리", 5, EnvGroup.CARE),
            // 홍보 — 온라인부터 오프라인
            EnvItem("e-post", "게시물", 3, EnvGroup.PROMO),
            EnvItem("e-story", "스토리", 2, EnvGroup.PROMO),
            EnvItem("e-flyer", "전단지", 1, EnvGroup.PROMO),
            EnvItem("e-banner", "현수막", 10, EnvGroup.PROMO),
            EnvItem("e-scroll", "족자", 5, EnvGroup.PROMO),
            EnvItem("e-blog", "블로그", 3, EnvGroup.PROMO),
            // 기타 — 어쩌다 하는 것들.
            // 클레임해결은 컴플레인 한 건을 끝까지 처리한 값이라 다른 항목보다 높다
            EnvItem("e-claim", "클레임해결", 15, EnvGroup.ETC),
            EnvItem("e-etc", "기타", 1, EnvGroup.ETC),
        )
    }
}

/**
 * 내 업무 한 줄 — **하루에 한 번씩 체크한다**
 *
 * 공통 업무와 도는 방식이 다르다.
 *
 * | | 하루에 |
 * |---|---|
 * | 공통 업무 | 여러 번 — 할 때마다 횟수가 는다 |
 * | 내 업무 | **한 번씩 체크** — 다 하면 완료, 남으면 누락 |
 *
 * **체크는 되돌릴 수 없다.** 한 번 누르면 그 줄은 잠긴다 — 누를 자리가 아니게 된다.
 *
 * @property value 체크할 때 적어 넣은 값 (`신규 3 · 재등록 5`). 체크한 뒤에만 찬다
 */
data class MyTask(
    val id: String,
    val content: String,
    val checked: Boolean,
    val value: String? = null,
) {
    /** 체크한 줄 — 되돌릴 수 없어서 새 값을 만들어 갈아 끼운다 */
    fun check(): MyTask = copy(checked = true)

    companion object {
        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 서버가 붙으면 지운다. 이 값을 보고 서버에 칸을 만들지 않는다.
         */
        val demo = listOf(
            MyTask("t1", "오픈 점검 (조명·음악·온도)", checked = true),
            MyTask("t2", "신규 상담 기록 정리", checked = true, value = "신규 3 · 재등록 5"),
            MyTask("t3", "인바디 기기 소독", checked = false),
            MyTask("t4", "회원 문의 회신", checked = false),
            MyTask("t5", "마감 시재 확인", checked = false),
        )
    }
}
