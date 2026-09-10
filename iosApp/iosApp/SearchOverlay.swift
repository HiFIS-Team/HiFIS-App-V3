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
        .clipShape(RoundedRectangle(cornerRadius: Self.radius, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: Self.radius, style: .continuous)
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

    /// 판 모서리 — 카드(24)보다 작다. 화면을 덮는 판이라 각이 덜 둥근 편이 단단해 보인다
    fileprivate static let radius: CGFloat = 20
    /// 입력 줄 높이
    fileprivate static let fieldHeight: CGFloat = 54
    /// 결과 자리 — 아직 빈 상태 한 줄만 든다
    fileprivate static let resultHeight: CGFloat = 120
    /// 헤더와 판 사이 — 헤더에 붙으면 헤더가 늘어난 것처럼 보인다
    fileprivate static let gap: CGFloat = 12
}

/// 검색을 덮어 올리는 자리 — **흐림은 UIKit 이 그린다**
///
/// SwiftUI 의 `.ultraThinMaterial` 로는 안 된다. 그 재질은 **제가 속한 나무의 뒤**를
/// 뜨는데, 여기는 딴 컨트롤러 위로 덮는 자리라 볼 것이 없다 (유리에서 같은 것을 겪었다).
/// `UIVisualEffectView` 는 창 안에서 제 뒤를 보므로 아래 화면이 그대로 흐려진다.
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
        view.backgroundColor = .clear

        // **세기는 투명도로 잡는다.** 재질을 그대로 씌우면 뒤가 통째로 지워지는데,
        // 우리 화면은 거의 검정이라 조금만 흐려도 남는 게 없다.
        // (`UIViewPropertyAnimator.fractionComplete` 로 조절해 봤지만 안 먹었다 —
        // 0 으로 둬도 효과가 통째로 걸렸다.)
        // 투명도를 낮추면 **흐린 것과 원래 것이 섞여** 형태가 살아난다.
        let blur = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        blur.alpha = Self.blurAlpha
        blur.frame = view.bounds
        blur.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(blur)

        // **판 밖을 누르면 닫힌다** — 나가는 가장 빠른 길이다
        let tap = UITapGestureRecognizer(target: self, action: #selector(close))
        view.addGestureRecognizer(tap)

        let panel = UIHostingController(rootView: SearchPanel())
        panel.view.backgroundColor = .clear
        addChild(panel)
        view.addSubview(panel.view)
        panel.didMove(toParent: self)

        panel.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            panel.view.leadingAnchor.constraint(
                equalTo: view.leadingAnchor, constant: HifisSize.screenEdge
            ),
            panel.view.trailingAnchor.constraint(
                equalTo: view.trailingAnchor, constant: -HifisSize.screenEdge
            ),
            // 헤더 높이만큼 내려서 **헤더 아래에서** 내려온 것처럼 보이게 한다
            panel.view.topAnchor.constraint(
                equalTo: view.safeAreaLayoutGuide.topAnchor,
                constant: HifisSize.headerHeight + SearchPanel.gap
            ),
            panel.view.heightAnchor.constraint(
                equalToConstant: SearchPanel.fieldHeight + 1 + SearchPanel.resultHeight
            ),
        ])
    }

    /// 흐림을 얼마나 얹나 — 0 이면 맨눈, 1 이면 재질 그대로다.
    /// 글자는 못 읽되 **무엇이 있었는지는 보여야** 하는 자리로 잡았다
    private static let blurAlpha: CGFloat = 0.55

    @objc private func close() {
        dismiss(animated: true) { [onClose] in onClose() }
    }
}
