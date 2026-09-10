import AVFoundation
import SwiftUI
import SharedKit

/// 출퇴근 스캔 — **옆에서 밀려 들어오는 카메라**
///
/// 헤더의 스캔 아이콘이 이 화면을 연다. 매장 카운터에 붙은 종이 QR 을 폰으로 읽는
/// 자리다 (V2 가 카운터 스캐너를 걷어내며 만든 화면). **오른쪽에서 왼쪽으로** 들어와
/// 셸을 탭바까지 덮는다 — 셸 위의 잎이다 (`MainScreen.swift`). AI 처럼
/// 아래에서 올리면 이 화면만 결이 다르다.
///
/// ## 이 화면은 **늘 검다**
///
/// 바닥이 카메라라 테마가 끼어들 자리가 없다. 선과 글자는 **흰색**으로 못 박는다 —
/// `ink` 같은 토큰을 쓰면 라이트가 왔을 때 카메라 위에 검은 글자가 선다.
///
/// ## 조준 규격은 **화면 정가운데**다
///
/// 규격의 중심이 화면 중심이다. 안내 문구는 규격 **아래에 매달린다** — 문구까지 묶어
/// 가운데 맞추면 규격이 문구 절반만큼 위로 밀린다.
///
/// **읽는 것은 아직 안 붙였다.** 서버가 없어서 읽어도 보낼 곳이 없다 (`AttendanceScan`).
struct AttendanceScanView: View {
    let onBack: () -> Void
    @StateObject private var camera = ScanCamera()

    var body: some View {
        ZStack(alignment: .topLeading) {
            ZStack {
                Color.black
                if camera.unavailable {
                    cameraOff
                } else {
                    CameraPreview(session: camera.session)
                    guide
                }
            }
            // **가운데는 화면의 가운데다.** 안전영역 안에서 맞추면 위(다이내믹 아일랜드 59)와
            // 아래(홈 인디케이터 34)가 달라서 규격이 12pt 쯤 아래로 내려앉는다 — 재서 잡았다.
            // 뒤로가기만 바깥 층에서 안전영역을 지킨다
            .ignoresSafeArea()
            backButton
        }
        .onAppear { camera.start() }
        .onDisappear { camera.stop() }
    }

    /// 카메라 위 안내 — 규격은 정가운데, 문구는 그 아래에 매달린다
    private var guide: some View {
        VStack(spacing: Self.guideGap) {
            ScanFrame()
            Text(AttendanceScan.shared.GUIDE)
                .font(HifisFont.body)
                .foregroundStyle(.white)
        }
        // 세로 크기를 규격만큼만 잡고 안은 위로 붙인다 — 그래야 가운데 맞춤이
        // 규격을 맞추고 문구는 아래로 삐져나간다 (SwiftUI 는 넘친 것을 안 자른다)
        .frame(height: ScanFrame.side, alignment: .top)
    }

    /// 카메라를 못 열었다 — 권한을 막았거나 (시뮬레이터처럼) 카메라가 없다
    private var cameraOff: some View {
        VStack(spacing: 0) {
            Image("ic_camera_off")
                .renderingMode(.template)
                .resizable()
                .frame(width: Self.offIcon, height: Self.offIcon)
                .foregroundStyle(.white.opacity(Self.offDim))
            Spacer().frame(height: 18)
            Text(AttendanceScan.shared.NO_CAMERA)
                .font(HifisFont.body)
                .foregroundStyle(.white)
            Spacer().frame(height: 8)
            Text(AttendanceScan.shared.NO_CAMERA_HINT)
                .font(HifisFont.caption)
                .foregroundStyle(.white.opacity(Self.offDim))
        }
        .multilineTextAlignment(.center)
        .padding(.horizontal, Self.offEdge)
    }

    /// 왼쪽 위 뒤로가기 — 그림이 화면 끝 `screenEdge` 에 서는 것은 헤더와 같다
    private var backButton: some View {
        Button(action: onBack) {
            Image("ic_chevron_left")
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                .foregroundStyle(.white)
                .frame(width: HifisSize.headerIconButton, height: HifisSize.headerIconButton)
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel("뒤로")
        .frame(height: HifisSize.headerHeight)
        .padding(.leading, HifisSize.screenEdge - HifisSize.headerIconInset)
    }

    /// 규격과 안내 문구 사이
    private static let guideGap: CGFloat = 24
    /// 카메라를 못 열었을 때의 아이콘
    private static let offIcon: CGFloat = 44
    /// 그 화면의 좌우 여백 — 문구가 두 줄로 접히게 넉넉히 둔다
    private static let offEdge: CGFloat = 40
    /// 아이콘과 보조 문구를 낮추는 진하기
    private static let offDim: Double = 0.7
}

/// 조준 규격 — **네 모서리만** 그린다 (V2 에서 대표가 정한 모양, 2026-08-28)
///
/// 네모를 통째로 두르면 그 선이 QR 테두리처럼 보여서 카메라가 무엇을 읽는지 헷갈린다.
/// 모서리만 두면 "이 안에 넣어라"는 뜻은 그대로고 화면은 비운다.
///
/// **네 조각을 따로 그리지 않는다.** 한 조각을 가운데를 축으로 90도씩 돌려 찍는다 —
/// 값을 하나만 고치면 넷이 같이 움직인다. 안드로이드 `ScanFrame` 과 같은 값이다.
struct ScanFrame: View {
    /// 규격 한 변 — 여기에 QR 을 채우면 읽힌다
    static let side: CGFloat = 240
    /// 모서리 곡률
    private static let radius: CGFloat = 28
    /// 모서리에서 뻗는 길이
    private static let arm: CGFloat = 40
    /// 선 굵기 — 끝은 둥글다. 안 그러면 잘린 선처럼 보인다
    private static let stroke: CGFloat = 4

    var body: some View {
        ZStack {
            ForEach(0..<4, id: \.self) { turn in
                corner.rotationEffect(.degrees(Double(turn) * 90))
            }
        }
        .frame(width: Self.side, height: Self.side)
    }

    /// 왼쪽 위 한 조각 — 아래에서 올라와 모서리를 돌아 오른쪽으로 뻗는다
    private var corner: some View {
        Path { path in
            // 선 굵기의 절반만큼 안으로 들여서 선의 바깥이 규격 끝에 선다
            let inset = Self.stroke / 2
            let radius = Self.radius
            path.move(to: CGPoint(x: inset, y: inset + radius + Self.arm))
            path.addLine(to: CGPoint(x: inset, y: inset + radius))
            path.addArc(
                center: CGPoint(x: inset + radius, y: inset + radius),
                radius: radius,
                startAngle: .degrees(180),
                endAngle: .degrees(270),
                clockwise: false
            )
            path.addLine(to: CGPoint(x: inset + radius + Self.arm, y: inset))
        }
        .stroke(.white, style: StrokeStyle(lineWidth: Self.stroke, lineCap: .round))
        .frame(width: Self.side, height: Self.side)
    }
}

/// 카메라 — **`AVCaptureSession` 하나를 열고 닫는다**
///
/// ## 답을 이미 아는 것은 **첫 프레임에** 정한다
///
/// 화면이 옆에서 밀려 들어오는 **도중에** 내용을 갈아 끼우면, 새로 든 글은 들어오는
/// 움직임을 안 탄다 — 제자리(화면 가운데)에 그냥 생긴다. 뒤로가기는 페이지와 같이
/// 움직이는데 `카메라를 열 수 없어요` 만 가운데에 서 있는 것을 재서 확인했다.
/// 그래서 권한을 막았거나 카메라가 없는 것(시뮬레이터)은 만들 때 바로 `unavailable` 로
/// 두어 처음부터 그 화면으로 들어온다. 아직 안 물어본 폰만 물어본 뒤에 정한다 —
/// 그때는 시스템 창이 떠 있어서 움직임은 이미 끝나 있다.
///
/// 세션은 딴 큐에서 켜고 끈다. `startRunning` 이 메인을 잡고 있으면
/// 밀려 들어오는 애니메이션이 그 동안 멈춘다.
final class ScanCamera: ObservableObject {
    /// 권한을 막았거나 카메라가 없다
    @Published var unavailable: Bool
    let session = AVCaptureSession()
    private let queue = DispatchQueue(label: "app.hifis.hifis.scan-camera")

    init() {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        unavailable = status == .denied || status == .restricted
            || AVCaptureDevice.default(for: .video) == nil
    }

    func start() {
        guard !unavailable else { return }
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            open()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    if granted { self?.open() } else { self?.fail() }
                }
            }
        default:
            fail()
        }
    }

    func stop() {
        queue.async { [session] in
            if session.isRunning { session.stopRunning() }
        }
    }

    /// 나중에야 안 것 — 화면은 이미 서 있으니 **바뀌는 것만 살짝 녹여** 넣는다
    private func fail() {
        withAnimation(.easeInOut(duration: 0.2)) { unavailable = true }
    }

    private func open() {
        queue.async { [weak self] in
            guard let self else { return }
            guard let device = AVCaptureDevice.default(for: .video),
                  let input = try? AVCaptureDeviceInput(device: device),
                  session.canAddInput(input)
            else {
                DispatchQueue.main.async { self.fail() }
                return
            }
            session.beginConfiguration()
            session.addInput(input)
            session.commitConfiguration()
            session.startRunning()
        }
    }
}

/// 미리보기 층 — `AVCaptureVideoPreviewLayer` 를 뷰의 바닥 층으로 쓴다
///
/// 따로 층을 얹으면 뷰 크기가 바뀔 때 층 크기를 손으로 따라가야 한다.
/// 바닥 층으로 두면 뷰와 같이 커진다.
private struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> PreviewView {
        let view = PreviewView()
        view.previewLayer.session = session
        // 화면을 꽉 채운다 — 비율을 지키려고 위아래에 검은 띠를 남기면 카메라 같지 않다
        view.previewLayer.videoGravity = .resizeAspectFill
        return view
    }

    func updateUIView(_ view: PreviewView, context: Context) {}

    final class PreviewView: UIView {
        override class var layerClass: AnyClass { AVCaptureVideoPreviewLayer.self }
        var previewLayer: AVCaptureVideoPreviewLayer { layer as! AVCaptureVideoPreviewLayer }
    }
}
