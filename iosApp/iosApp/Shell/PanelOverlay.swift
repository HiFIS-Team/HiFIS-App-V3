import SwiftUI
import UIKit

/// 헤더 아래로 내려오는 **판**의 공용 껍데기
///
/// 검색판과 지점 고르개가 이걸 나눠 쓴다. 헤더의 단추 둘이 서로 다른 모양의 판을
/// 내면 같은 줄에서 나온 것처럼 안 보인다 — 뒤를 덮는 세기도 같아야 한다.
///
/// **흐림은 UIKit 이 그린다.** SwiftUI 의 `.ultraThinMaterial` 로는 안 된다 —
/// 그 재질은 **제가 속한 나무의 뒤**를 뜨는데, 여기는 딴 컨트롤러 위로 덮는 자리라
/// 볼 것이 없다. `UIVisualEffectView` 는 창 안에서 제 뒤를 보므로 아래 화면이 그대로 흐려진다.
enum PanelOverlay {
    /// 흐림을 얼마나 얹나 — 0 이면 맨눈, 1 이면 재질 그대로다.
    /// 글자는 못 읽되 **무엇이 있었는지는 보여야** 하는 자리로 잡았다
    /// (안드로이드는 같은 자리를 어두운 막 0.55 로 대신한다 — 컴포즈 흐림은 API 31 부터다)
    static let blurAlpha: CGFloat = 0.55

    /// 뒤를 덮는 흐림 + 판을 컨트롤러에 앉힌다
    ///
    /// - Parameters:
    ///   - height: 판 높이. **비우면 내용이 정한다** (줄 수가 달라지는 판)
    ///   - attached: **헤더에 붙여 내려올 것인가.** 붙이면 좌우 여백 없이 화면을 꽉 채우고
    ///     헤더 줄은 덮지 않는다 — 판이 거기서 풀려 나온 것처럼 보여야 한다
    /// - Returns: 판의 뷰. 붙여서 내려오는 판은 이걸 움직여 애니메이션을 건다
    @discardableResult
    static func install<Panel: View>(
        in controller: UIViewController,
        panel: Panel,
        height: CGFloat? = nil,
        attached: Bool = false
    ) -> UIView {
        controller.view.backgroundColor = .clear

        // **세기는 투명도로 잡는다.** 재질을 그대로 씌우면 뒤가 통째로 지워지는데,
        // 우리 화면은 거의 검정이라 조금만 흐려도 남는 게 없다.
        // 투명도를 낮추면 **흐린 것과 원래 것이 섞여** 형태가 살아난다
        let blur = UIVisualEffectView(effect: UIBlurEffect(style: .dark))
        blur.alpha = blurAlpha
        blur.translatesAutoresizingMaskIntoConstraints = false
        controller.view.addSubview(blur)

        // **붙는 판은 헤더를 안 덮는다** — 그 줄이 밝게 남아야 거기서 나온 것으로 읽힌다
        let top = attached
            ? blur.topAnchor.constraint(
                equalTo: controller.view.safeAreaLayoutGuide.topAnchor,
                constant: HifisSize.headerHeight
            )
            : blur.topAnchor.constraint(equalTo: controller.view.topAnchor)
        NSLayoutConstraint.activate([
            top,
            blur.leadingAnchor.constraint(equalTo: controller.view.leadingAnchor),
            blur.trailingAnchor.constraint(equalTo: controller.view.trailingAnchor),
            blur.bottomAnchor.constraint(equalTo: controller.view.bottomAnchor),
        ])

        let host = UIHostingController(rootView: panel)
        host.view.backgroundColor = .clear
        if height == nil {
            // 줄 수가 판마다 달라서 **내용이 높이를 정한다**
            host.sizingOptions = [.intrinsicContentSize]
        }
        controller.addChild(host)
        host.didMove(toParent: controller)

        // 붙는 판은 **상자 안에서 움직인다** — 상자가 잘라 줘야 헤더 뒤로 말려 들어간다
        let box = UIView()
        box.clipsToBounds = true
        box.translatesAutoresizingMaskIntoConstraints = false
        controller.view.addSubview(box)
        box.addSubview(host.view)

        let edge = attached ? 0 : HifisSize.screenEdge
        host.view.translatesAutoresizingMaskIntoConstraints = false
        var rules = [
            box.leadingAnchor.constraint(equalTo: controller.view.leadingAnchor, constant: edge),
            box.trailingAnchor.constraint(equalTo: controller.view.trailingAnchor, constant: -edge),
            // 헤더 아래에서 내려온다 — 붙는 판은 **바로 밑**, 뜨는 판은 한 칸 띄운다
            box.topAnchor.constraint(
                equalTo: controller.view.safeAreaLayoutGuide.topAnchor,
                constant: HifisSize.headerHeight + (attached ? 0 : gap)
            ),
            host.view.leadingAnchor.constraint(equalTo: box.leadingAnchor),
            host.view.trailingAnchor.constraint(equalTo: box.trailingAnchor),
            host.view.topAnchor.constraint(equalTo: box.topAnchor),
            box.bottomAnchor.constraint(equalTo: host.view.bottomAnchor),
        ]
        if let height {
            rules.append(host.view.heightAnchor.constraint(equalToConstant: height))
        }
        NSLayoutConstraint.activate(rules)
        return host.view
    }

    /// 헤더와 판 사이 — 헤더에 붙으면 헤더가 늘어난 것처럼 보인다
    static let gap: CGFloat = 12

    /// 판 모서리 — 카드(24)보다 작다. 화면을 덮는 판이라 각이 덜 둥근 편이 단단해 보인다
    static let radius: CGFloat = 20
}

/// 창의 맨 위 컨트롤러 — UIKit 으로 덮어 올릴 때 쓴다
///
/// 이미 덮인 것이 있으면 그 위에 올린다. 셸이 SwiftUI 라 올릴 자리를 여기서 찾는다.
func topController() -> UIViewController? {
    var controller = UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap(\.windows)
        .first { $0.isKeyWindow }?
        .rootViewController
    while let next = controller?.presentedViewController {
        controller = next
    }
    return controller
}
