package app.hifis.shared.nav

/**
 * 지점 — **헤더 왼쪽 고르개가 정하고 화면들이 같이 본다**
 *
 * V2 는 조직도·업무·랭킹이 **각자 고르개를 하나씩** 들고 있었다. 화순을 보다가 옆
 * 화면으로 옮기면 다시 전 지점으로 돌아가서, 한 지점을 훑어보려면 화면마다 다시 골라야
 * 했다 (`branch_scope.dart`). V3 도 고르개는 헤더 하나뿐이고 값은 셸이 들고 있는다.
 *
 * **[id] 로 들고 이름은 필요할 때 만든다.** 지점 이름은 바뀔 수 있고 서버가 주고받는
 * 것도 id 라, 이름을 키로 쓰면 이름을 고치는 순간 필터가 끊긴다.
 *
 * **안 고른 상태가 [ALL] 이다** — 값으로는 `null` 이다. V2 도 같은 말을 썼다.
 *
 * > **MEMBER 에게는 이 고르개를 안 세운다** (V2 `branchScopeVisible`). 서버가 본인
 * > 지점으로 고정해서 골라 봐야 바뀌는 것이 없다 — 눌러도 아무 일 없는 자리가 된다.
 * > 로그인이 아직 없어서 지금은 늘 세운다. 붙으면 헤더의 `canPickBranch` 에서 거른다.
 */
data class Branch(val id: String, val name: String) {
    companion object {
        /** 판 머리말 */
        const val TITLE = "지점"

        /** 안 고른 상태의 이름 — **V2 와 같은 말이다** (`allBranchesLabel`) */
        const val ALL = "전 지점"

        /**
         * 아직 서버가 없다 — **V2 에 있는 지점 둘을 그대로 둔다**
         *
         * 차례도 V2 와 같다 (화순 → 첨단, `StaffDirectory._order`).
         * 서버가 붙으면 이 목록은 지운다.
         */
        val demo: List<Branch> = listOf(
            Branch("hwasun", "화순"),
            Branch("chumdan", "첨단"),
        )

        /** 고른 지점 이름 — 안 골랐으면 [ALL] */
        fun nameOf(id: String?): String =
            demo.firstOrNull { it.id == id }?.name ?: ALL
    }
}
