package app.hifis.shared.auth

/**
 * 인증 창구.
 *
 * 서버는 아직 없다 (Spring 으로 새로 짠다). 화면은 이 인터페이스만 보고,
 * 지금은 [FakeAuthRepository] 가 답한다. 서버가 생기면 구현만 갈아 끼운다.
 */
interface AuthRepository {

    suspend fun login(email: String, password: String): AuthResult<Account>

    /**
     * 초대 코드로 가입한다.
     *
     * **가입 신청은 받지 않는다** — 조직 관리자가 발급한 코드가 있어야 한다.
     * 코드 안에 소속 센터·직급·권한이 들어 있어서 가입자가 고를 것이 없다.
     */
    suspend fun signUp(
        inviteCode: String,
        name: String,
        email: String,
        password: String,
    ): AuthResult<Account>
}
