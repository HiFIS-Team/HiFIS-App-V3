package app.hifis.shared.auth

/** 로그인한 직원 — 서버 `/auth/me` 응답에 대응한다. */
data class Account(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    /** 소속 조직(본사). 한 사람은 한 조직에만 속한다. */
    val orgName: String,
    /** 소속 센터. 본사 소속이면 null 이다. */
    val centerName: String?,
)

enum class Role { MASTER, ADMIN, MANAGER, MEMBER }

/**
 * 인증 실패 사유.
 *
 * **화면이 문구를 직접 만들지 않는다.** 서버가 바뀌어도 화면이 안 흔들리도록
 * 사유만 올려보내고, 문구는 화면 쪽에서 한 곳에 모아 둔다.
 */
enum class AuthError {
    /** 이메일이 없거나 비밀번호가 틀렸다 — 둘을 구분해서 알려주지 않는다 */
    INVALID_CREDENTIALS,
    /** 초대 코드가 없거나 이미 쓰였거나 기한이 지났다 */
    INVALID_INVITE,
    /** 이미 가입된 이메일 */
    EMAIL_TAKEN,
    NETWORK,
    UNKNOWN,
}

/** 성공이면 값을, 실패면 사유를 담는다. */
sealed interface AuthResult<out T> {
    data class Ok<T>(val value: T) : AuthResult<T>
    data class Fail(val error: AuthError) : AuthResult<Nothing>
}
