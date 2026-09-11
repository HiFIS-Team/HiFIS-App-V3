import SwiftUI
import UIKit
import SharedKit

/// 지점 고르개 — **헤더 아래로 내려오는 판** (2026-09-11 대표)
///
/// 검색판과 **같은 자리·같은 모양**이다 (`PanelOverlay`). 헤더 아래에서 내려오고,
/// 뒤는 옅게 덮이고, 판 밖을 누르면 닫힌다.
///
/// **머리말(`지점`)을 붙인다.** 줄이 `전 지점 · 화순 · 첨단` 뿐이라 무엇을 고르는
/// 자리인지가 글자만으로는 안 드러난다.
///
/// 고른 줄에는 **브랜드색 체크**가 선다. 줄을 통째로 칠하지 않는다 — 판 안에서
/// 면을 칠하면 카드처럼 보여서 누를 것이 하나 더 생긴 것처럼 읽힌다.
struct BranchPanel: View {
    /// 고른 지점 — **nil 이면 전 지점**
    let picked: String?
    /// 지금 제품의 브랜드색 — **값으로 받는다**
    ///
    /// 이 판은 셸의 SwiftUI 나무 **밖**(`UIHostingController`)에 서서 `\.brand` 환경값이
    /// 안 따라온다. `HifisColor.brand` 를 쓰면 TeamFIS 에서도 체크가 파랗다 (대표가 봤다)
    let brand: Color
    let onPick: (String?) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 헤더와 판이 같은 면이라 **가르는 줄**이 없으면 어디까지가 헤더인지 안 보인다
            HifisColor.line.frame(height: 1)
            Text(Branch.companion.TITLE)
                .font(.system(size: 13))
                .foregroundStyle(HifisColor.inkTertiary)
                .padding(.leading, Self.rowEdge)
                .padding(.top, 16)
                .padding(.bottom, 4)

            // **전 지점이 맨 위다.** 안 고른 상태라 첫 줄에 있어야 되돌리기 쉽다
            row(Branch.companion.ALL, on: picked == nil) { onPick(nil) }
            ForEach(Branch.companion.demo, id: \.id) { branch in
                row(branch.name, on: picked == branch.id) { onPick(branch.id) }
            }
            Spacer(minLength: 8)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(HifisColor.surface)
        // 아래 두 귀만 둥글다 — **헤더에 붙어 있는 판**이라 위는 각져야 이어져 보인다
        .clipShape(BottomRounded(radius: PanelOverlay.radius))
    }

    private func row(_ name: String, on: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 0) {
                Text(name)
                    .font(.system(size: 16, weight: on ? .semibold : .regular))
                    .foregroundStyle(on ? HifisColor.ink : HifisColor.inkSecondary)
                Spacer(minLength: 0)
                if on {
                    Image("ic_check")
                        .renderingMode(.template)
                        .resizable()
                        .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                        .foregroundStyle(brand)
                }
            }
            .padding(.horizontal, Self.rowEdge)
            .frame(height: Self.rowHeight)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    /// 줄 하나 높이
    private static let rowHeight: CGFloat = 52
    /// 줄 좌우 여백 — 검색 입력줄(18)과 맞춘다
    private static let rowEdge: CGFloat = 18
}

/// 지점 고르개를 덮어 올리는 자리 — 검색판과 같은 껍데기를 쓴다 (`PanelOverlay`)
final class BranchOverlayController: UIViewController {
    private let shell: ShellState
    /// 헤더 뒤에서 풀려 나오는 판과 뒤를 덮는 흐림 — **전환을 우리가 잡는다**
    private var parts: PanelOverlay.Installed?

    init(shell: ShellState) {
        self.shell = shell
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .overFullScreen
        // **시스템 전환을 안 쓴다.** 페이드가 우리 애니메이션을 가로채서, 걷을 때 판이
        // 제자리로 툭 내려앉았다가 사라졌다 (대표가 봤다, 2026-09-11)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) { fatalError("스토리보드를 안 쓴다") }

    override func viewDidLoad() {
        super.viewDidLoad()
        parts = PanelOverlay.install(
            in: self,
            panel: BranchPanel(
                picked: shell.branch,
                brand: HifisBrand.palette(shell.product).brand
            ) { [weak self] id in
                self?.shell.pick(branch: id)
                self?.leave()
            },
            attached: true
        )
        // **판 밖을 누르면 닫힌다** — 나가는 가장 빠른 길이다
        view.addGestureRecognizer(
            UITapGestureRecognizer(target: self, action: #selector(close))
        )
    }

    /// **헤더 뒤에서 내려온다.** 자리를 옮기는 것이 아니라 풀려 나오는 결이다 —
    /// 상자가 잘라 주므로 시작 자리는 제 키만큼 위다
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        view.layoutIfNeeded()
        guard let parts, let grow = parts.grow else { return }
        let full = parts.fullHeight(width: view.bounds.width)
        grow.constant = 0
        parts.blur.alpha = 0
        view.layoutIfNeeded()
        grow.constant = full
        UIView.animate(withDuration: Self.enter, delay: 0, options: .curveEaseOut) {
            self.view.layoutIfNeeded()
            parts.blur.alpha = PanelOverlay.blurAlpha
        }
    }

    /// 되말리면서 닫는다 — 툭 사라지면 헤더로 들어간 것이 안 보인다
    ///
    /// **다 걷은 뒤에 치운다.** `dismiss(animated:)` 를 같이 돌리면 그 전환이 우리
    /// 애니메이션을 가로채, 올라가던 판이 제자리로 내려앉았다가 사라진다
    fileprivate func leave() {
        guard let parts, let grow = parts.grow else {
            dismiss(animated: false)
            return
        }
        grow.constant = 0
        UIView.animate(withDuration: Self.exit, delay: 0, options: .curveEaseIn) {
            self.view.layoutIfNeeded()
            parts.blur.alpha = 0
        } completion: { [weak self] _ in
            self?.dismiss(animated: false)
        }
    }

    @objc private func close() { leave() }

    /// 펼쳐지는 시간 — 안드로이드와 같은 값 (240 / 180)
    private static let enter: TimeInterval = 0.24
    private static let exit: TimeInterval = 0.18

    /// 헤더의 지점 단추가 부른다 (`TabPage`)
    static func present(shell: ShellState) {
        guard let top = topController() else { return }
        // **이미 떠 있으면 하나 더 얹지 않는다 — 다시 누르면 닫는다.**
        // 안 막으면 누를 때마다 판이 쌓여서 **두 번 내려온 것처럼** 보인다
        // (대표가 봤다, 2026-09-11 — V2 도 사내톡 필터에서 같은 것을 겪고 자물쇠를 뒀다)
        if let open = top as? BranchOverlayController {
            open.leave()
            return
        }
        // **페이드 없이 올린다** — 들어오는 그림은 `viewWillAppear` 가 직접 그린다
        top.present(BranchOverlayController(shell: shell), animated: false)
    }
}

/// 아래 두 귀만 둥근 모양 — `UnevenRoundedRectangle` 은 iOS 16 에 없다
private struct BottomRounded: Shape {
    let radius: CGFloat

    func path(in rect: CGRect) -> Path {
        Path { path in
            path.move(to: CGPoint(x: rect.minX, y: rect.minY))
            path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
            path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY - radius))
            path.addQuadCurve(
                to: CGPoint(x: rect.maxX - radius, y: rect.maxY),
                control: CGPoint(x: rect.maxX, y: rect.maxY)
            )
            path.addLine(to: CGPoint(x: rect.minX + radius, y: rect.maxY))
            path.addQuadCurve(
                to: CGPoint(x: rect.minX, y: rect.maxY - radius),
                control: CGPoint(x: rect.minX, y: rect.maxY)
            )
            path.closeSubpath()
        }
    }
}
