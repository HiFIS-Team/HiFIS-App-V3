package app.hifis.hifis

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.hifis.hifis.shell.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 헤더가 상태바 밑까지 제 색으로 올라간다 — 헤더가 statusBarsPadding 으로
        // 제 자리를 잡으므로 여기서 인셋을 따로 빼지 않는다.
        //
        // **앱이 늘 어두우므로 시스템 바 글자도 밝게 못 박는다.** 기본값은 기기 설정을
        // 따라가서, 라이트 기기에서 어두운 헤더 위에 검은 시계가 뜬다
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
        )
        // **테마는 `MainScreen` 이 두른다** — 브랜드색이 제품에 따라 갈려서,
        // 제품 상태를 들고 있는 자리가 색도 정해야 한다
        setContent { MainScreen() }
    }
}
