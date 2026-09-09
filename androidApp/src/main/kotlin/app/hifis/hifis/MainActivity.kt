package app.hifis.hifis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.hifis.hifis.ui.theme.HifisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 헤더가 상태바 밑까지 제 색으로 올라간다 — 헤더가 statusBarsPadding 으로
        // 제 자리를 잡으므로 여기서 인셋을 따로 빼지 않는다
        enableEdgeToEdge()
        setContent {
            HifisTheme {
                MainScreen()
            }
        }
    }
}
