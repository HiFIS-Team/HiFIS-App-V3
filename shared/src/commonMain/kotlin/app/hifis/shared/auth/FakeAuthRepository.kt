package app.hifis.shared.auth

import kotlinx.coroutines.delay

/**
 * 서버가 나오기 전까지 쓰는 가짜 창구.
 *
 * **화면을 만드는 동안 흐름을 다 볼 수 있게** 성공과 실패를 모두 낸다.
 * 실제 서버가 붙으면 이 파일은 지운다.
 */
class FakeAuthRepository : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult<Account> {
        delay(600)  // 눌렀을 때 로딩이 보이도록
        if (email.trim().lowercase() != DEMO_EMAIL || password != DEMO_PASSWORD) {
            return AuthResult.Fail(AuthError.INVALID_CREDENTIALS)
        }
        return AuthResult.Ok(
            Account(
                id = "1",
                name = "김은후",
                email = DEMO_EMAIL,
                role = Role.MASTER,
                orgName = "피트니스스타",
                centerName = null,
            ),
        )
    }

    override suspend fun signUp(
        inviteCode: String,
        name: String,
        email: String,
        password: String,
    ): AuthResult<Account> {
        delay(600)
        if (inviteCode.trim().uppercase() != DEMO_INVITE) {
            return AuthResult.Fail(AuthError.INVALID_INVITE)
        }
        if (email.trim().lowercase() == DEMO_EMAIL) {
            return AuthResult.Fail(AuthError.EMAIL_TAKEN)
        }
        return AuthResult.Ok(
            Account(
                id = "2",
                name = name,
                email = email.trim(),
                role = Role.MEMBER,
                orgName = "피트니스스타",
                centerName = "첨단점",
            ),
        )
    }

    companion object {
        const val DEMO_EMAIL = "master@hifis.app"
        const val DEMO_PASSWORD = "hifis1234"
        const val DEMO_INVITE = "HIFIS-2026"
    }
}
