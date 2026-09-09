package app.hifis.shared

/** 각 플랫폼이 실제 구현을 제공하는지 확인하기 위한 최소 스캐폴드. */
interface Platform {
    val name: String
}

expect fun currentPlatform(): Platform
