package app.hifis.hifis.attendance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.attendance.AttendanceScan
import kotlinx.coroutines.awaitCancellation

/**
 * 출퇴근 스캔 — **옆에서 밀려 들어오는 카메라**
 *
 * 헤더의 스캔 아이콘이 이 화면을 연다. 매장 카운터에 붙은 종이 QR 을 폰으로 읽는
 * 자리다 (V2 가 카운터 스캐너를 걷어내며 만든 화면). **오른쪽에서 왼쪽으로** 들어온다 —
 * 상세 화면이 가는 길이고, AI 처럼 아래에서 올리면 이 화면만 결이 다르다.
 *
 * ## 이 화면은 **늘 검다**
 *
 * 바닥이 카메라라 테마가 끼어들 자리가 없다. 선과 글자는 **흰색**으로 못 박는다 —
 * `ink` 같은 토큰을 쓰면 라이트가 왔을 때 카메라 위에 검은 글자가 선다.
 *
 * ## 조준 규격은 **화면 정가운데**다
 *
 * 규격의 중심이 화면 중심이다. 안내 문구는 규격 **아래에 매달린다** — 문구까지 묶어
 * 가운데 맞추면 규격이 문구 절반만큼 위로 밀린다.
 *
 * **읽는 것은 아직 안 붙였다.** 서버가 없어서 읽어도 보낼 곳이 없다 (`AttendanceScan`).
 */
@Composable
fun AttendanceScanScreen(onBack: () -> Unit) {
    // 시스템 뒤로가기도 이 화면을 닫는다 — 액티비티가 하나라 안 잡으면 앱이 통째로 나간다
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    // 권한을 막았거나 카메라가 없다 — **에뮬레이터·시뮬레이터가 아니라 실기기에서도** 생긴다
    var cameraOff by remember { mutableStateOf(false) }
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        if (!it) cameraOff = true
    }
    // 열자마자 묻는다 — 이 화면은 카메라 말고는 할 일이 없다
    LaunchedEffect(Unit) { if (!granted) ask.launch(Manifest.permission.CAMERA) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            // **뒤로 새는 터치를 막는다.** 셸 위에 얹힌 화면이라 빈 자리를 누르면
            // 그 아래 하단바가 받는다 — 누르는 자리가 하나라도 있어야 이 층에서 멈춘다
            .pointerInput(Unit) {},
    ) {
        if (cameraOff) {
            CameraOff()
        } else {
            if (granted) CameraPreview(onFail = { cameraOff = true })
            Guide()
        }
        BackButton(onBack)
    }
}

/**
 * 카메라 미리보기 — **CameraX 가 그린다**
 *
 * `EMBEDDED`(TextureView) 로 둔다. 이 화면은 셸 위에서 **밀려 들어오는 층**이라 카메라도
 * 보통 뷰처럼 같이 움직여야 한다. 기본값 `EXTERNAL`(SurfaceView) 은 화면에 구멍을 뚫고
 * 따로 합성하는 방식이라 움직이는 층 안에서는 쓰지 않는다.
 *
 * 화면을 나가면 `finally` 가 푼다 — 액티비티 생명주기에 묶여 있어서 그냥 두면
 * 화면이 닫혀도 카메라가 켜진 채로 남는다.
 */
@Composable
private fun CameraPreview(onFail: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var request by remember { mutableStateOf<SurfaceRequest?>(null) }

    LaunchedEffect(owner) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request = it }
        }
        try {
            provider.unbindAll()
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
        } catch (e: IllegalArgumentException) {
            // 뒤 카메라가 없다
            onFail()
            return@LaunchedEffect
        }
        try {
            awaitCancellation()
        } finally {
            provider.unbindAll()
        }
    }

    request?.let {
        CameraXViewfinder(
            surfaceRequest = it,
            implementationMode = ImplementationMode.EMBEDDED,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** 카메라 위 안내 — 규격은 정가운데, 문구는 그 아래에 매달린다 */
@Composable
private fun Guide() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 세로 크기를 규격만큼만 잡고 안은 위로 붙여 넘치게 둔다 —
        // 그래야 가운데 맞춤이 규격을 맞추고 문구는 아래로 삐져나간다
        Column(
            Modifier
                .height(FRAME_SIDE)
                .wrapContentHeight(Alignment.Top, unbounded = true),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScanFrame()
            Spacer(Modifier.height(GUIDE_GAP))
            Text(AttendanceScan.GUIDE, style = HifisType.body, color = Color.White)
        }
    }
}

/**
 * 조준 규격 — **네 모서리만** 그린다 (V2 에서 대표가 정한 모양, 2026-08-28)
 *
 * 네모를 통째로 두르면 그 선이 QR 테두리처럼 보여서 카메라가 무엇을 읽는지 헷갈린다.
 * 모서리만 두면 "이 안에 넣어라"는 뜻은 그대로고 화면은 비운다.
 *
 * **네 조각을 따로 그리지 않는다.** 한 조각을 가운데를 축으로 90도씩 돌려 찍는다 —
 * 값을 하나만 고치면 넷이 같이 움직인다. iOS `ScanFrame` 과 같은 값이다.
 */
@Composable
private fun ScanFrame() {
    Canvas(Modifier.size(FRAME_SIDE)) {
        val radius = FRAME_RADIUS.toPx()
        val arm = FRAME_ARM.toPx()
        val stroke = FRAME_STROKE.toPx()
        // 선 굵기의 절반만큼 안으로 들여서 선의 바깥이 규격 끝에 선다 —
        // 0 에 그리면 절반이 캔버스 밖으로 나간다
        val inset = stroke / 2
        // 왼쪽 위 한 조각 — 아래에서 올라와 모서리를 돌아 오른쪽으로 뻗는다
        val corner = Path().apply {
            moveTo(inset, inset + radius + arm)
            lineTo(inset, inset + radius)
            arcTo(
                rect = Rect(Offset(inset, inset), Offset(inset + radius * 2, inset + radius * 2)),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false,
            )
            lineTo(inset + radius + arm, inset)
        }
        repeat(4) { turn ->
            rotate(degrees = 90f * turn) {
                drawPath(corner, Color.White, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
    }
}

/** 카메라를 못 열었다 — 권한을 막았거나 (에뮬레이터처럼) 카메라가 없다 */
@Composable
private fun CameraOff() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = OFF_EDGE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_camera_off),
            contentDescription = null, // 바로 아래 글이 말한다
            tint = Color.White.copy(alpha = OFF_DIM),
            modifier = Modifier.size(OFF_ICON),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            AttendanceScan.NO_CAMERA,
            style = HifisType.body,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            AttendanceScan.NO_CAMERA_HINT,
            style = HifisType.caption,
            color = Color.White.copy(alpha = OFF_DIM),
            textAlign = TextAlign.Center,
        )
    }
}

/** 왼쪽 위 뒤로가기 — 그림이 화면 끝 [Dimens.screenEdge] 에 서는 것은 헤더와 같다 */
@Composable
private fun BackButton(onBack: () -> Unit) {
    Box(
        Modifier
            .statusBarsPadding()
            .height(Dimens.headerHeight)
            .padding(start = Dimens.screenEdge - Dimens.headerIconInset),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(Dimens.headerIconButton)
                .clip(CircleShape)
                .tap(label = "뒤로", onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_chevron_left),
                contentDescription = "뒤로",
                tint = Color.White,
                modifier = Modifier.size(Dimens.headerIcon),
            )
        }
    }
}

/** 규격 한 변 — 여기에 QR 을 채우면 읽힌다 */
private val FRAME_SIDE = 240.dp

/** 모서리 곡률 */
private val FRAME_RADIUS = 28.dp

/** 모서리에서 뻗는 길이 */
private val FRAME_ARM = 40.dp

/** 선 굵기 — 끝은 둥글다. 안 그러면 잘린 선처럼 보인다 */
private val FRAME_STROKE = 4.dp

/** 규격과 안내 문구 사이 */
private val GUIDE_GAP = 24.dp

/** 카메라를 못 열었을 때의 아이콘 */
private val OFF_ICON = 44.dp

/** 그 화면의 좌우 여백 — 문구가 두 줄로 접히게 넉넉히 둔다 */
private val OFF_EDGE = 40.dp

/** 아이콘과 보조 문구를 낮추는 진하기 */
private const val OFF_DIM = 0.7f
