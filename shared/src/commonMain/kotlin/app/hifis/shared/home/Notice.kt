package app.hifis.shared.home

/**
 * 공지 한 건 — 홈 `오늘 소식` 카드의 한 줄
 *
 * 홈은 **제목과 필독 여부만** 본다. 본문·작성자·읽은 수·댓글은 공지 화면에서 본다
 * (V2 `_Notice` 가 그것들을 다 들고 있다). 홈에 쓸 자리가 없는 값을 여기 끌어오지 않는다.
 */
data class Notice(
    val title: String,
    /** 상단 고정된 중요 공지 — 화면에 `필독` 으로 뜬다 (V2 `pinned`) */
    val pinned: Boolean,
) {
    companion object {
        /** 필독 줄에 붙는 글자 */
        const val PINNED_LABEL = "필독"

        /**
         * 카드 오른쪽에 찍는 날짜 꼴 — **양 플랫폼이 같은 문자열을 쓴다**
         *
         * 자바 `DateTimeFormatter` 와 파운데이션 `DateFormatter` 가 같은 규칙(LDML)이라
         * 그대로 통한다. `월`·`일` 은 영문자가 아니라 둘 다 글자 그대로 찍는다.
         */
        const val DATE_PATTERN = "M월 d일 (E)"

        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 서버가 붙으면 지운다. 이 값을 보고 서버에 칸을 만들지 않는다.
         */
        val demo = listOf(
            Notice("9월 안전교육 필수 이수 안내", pinned = true),
            Notice("추석 연휴 근무표 확정", pinned = false),
            Notice("신규 회원권 프로모션 시작", pinned = false),
        )
    }
}
