import SwiftUI
import UIKit
import SharedKit

/// 헤더 검색 — **헤더 아래로 내려오는 판**
///
/// 화면을 갈아 끼우지 않는다. 하던 자리를 **덮기만** 하고 닫으면 그대로 돌아온다 —
/// 검색은 지금 보던 것을 버리고 가는 일이 아니다.
///
/// 뒤는 **`UIVisualEffectView` 가 흐리게 덮는다** (`SearchOverlayController`).
/// 유리와 같은 결이다 — 우리가 흐림을 흉내내지 않고 OS 부품을 쓴다.
/// 안드로이드는 그 자리를 어두운 막으로 대신한다 (컴포즈 흐림은 API 31 부터라
/// 하한에서 아무 일도 안 한다).
///
/// 아직 **뒤질 것이 없다** (`AppSearch`). 서버도 색인도 안 붙여서 빈 상태만 뜬다.
struct SearchPanel: View {
    @State private var query = ""
    @FocusState private var focused: Bool

    var body: some View {
        VStack(spacing: 0) {
            field
            Rectangle()
                .fill(HifisColor.line)
                .frame(height: 1)
            // 아직 뒤질 것이 없다 — 서버도 색인도 안 붙였다
            Text(AppSearch.shared.EMPTY)
                .font(HifisFont.body)
                .foregroundStyle(HifisColor.inkSecondary)
                .frame(maxWidth: .infinity)
                .frame(height: Self.resultHeight)
        }
        .background(HifisColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: PanelOverlay.radius, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: PanelOverlay.radius, style: .continuous)
                .strokeBorder(HifisColor.line, lineWidth: 1)
        )
        // 열자마자 글쇠판이 올라온다 — 검색은 바로 치려고 여는 자리다
        .onAppear { focused = true }
    }

    private var field: some View {
        HStack(spacing: 12) {
            Image("ic_search")
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                .foregroundStyle(HifisColor.inkTertiary)
            TextField(AppSearch.shared.PLACEHOLDER, text: $query)
                .font(.system(size: 16))
                .foregroundStyle(HifisColor.ink)
                .textFieldStyle(.plain)
                .focused($focused)
                .submitLabel(.search)
        }
        .padding(.horizontal, 18)
        .frame(height: Self.fieldHeight)
    }

    /// 입력 줄 높이
    fileprivate static let fieldHeight: CGFloat = 54
    /// 결과 자리 — 아직 빈 상태 한 줄만 든다
    fileprivate static let resultHeight: CGFloat = 120
}

/// 검색을 덮어 올리는 자리 — 지점 고르개와 **같은 껍데기**를 쓴다 (`PanelOverlay`)
final class SearchOverlayController: UIViewController {
    private let onClose: () -> Void

    init(onClose: @escaping () -> Void) {
        self.onClose = onClose
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .overFullScreen
        modalTransitionStyle = .crossDissolve
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("스토리보드를 안 쓴다") }

    override func viewDidLoad() {
        super.viewDidLoad()
        PanelOverlay.install(
            in: self,
            panel: SearchPanel(),
            // 입력 줄 + 가르는 줄 + 결과 자리 — 줄 수가 안 바뀌어서 높이를 못 박는다
            height: SearchPanel.fieldHeight + 1 + SearchPanel.resultHeight
        )
        // **판 밖을 누르면 닫힌다** — 나가는 가장 빠른 길이다
        view.addGestureRecognizer(
            UITapGestureRecognizer(target: self, action: #selector(close))
        )
    }

    @objc private func close() {
        dismiss(animated: true) { [onClose] in onClose() }
    }
}
