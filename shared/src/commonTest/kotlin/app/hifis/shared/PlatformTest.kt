package app.hifis.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {
    @Test
    fun platformNameIsNotBlank() {
        assertTrue(currentPlatform().name.isNotBlank())
    }
}
