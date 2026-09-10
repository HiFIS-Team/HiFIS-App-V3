import SwiftUI
import UIKit
import SharedKit

/// 앱 셸 — 하단 탭바와 탭 화면을 들고 있다
///
/// 탭 목록은 `shared` 의 `MainTab` 하나만 읽는다. 여기서 새로 세우지 않는다
/// (V2 는 이 목록이 플랫폼마다 따로라 화면이 목록에서 새어 나갔다).
///
/// 바는 **`UITabBarController` 그대로** 다 — iOS 26 이 이 탭바를 리퀴드 글래스로
/// 그려 준다. 유리를 직접 흉내 내지 않는다.
///
/// 안드로이드는 반대로 머티리얼 3 `NavigationBar` 를 쓴다 —
/// **네비게이션은 각자 자기 OS 표준으로 간다.**
struct MainScreen: View {
    var body: some View {
        // **`ignoresSafeArea()` 를 붙인다.** 안 붙이면 SwiftUI 가 컨테이너를 홈
        // 인디케이터만큼 밀어 올리고 그 안에서 UIKit 이 또 제 여백을 잡아,
        // 바가 25pt 쯤 높이 뜬다 (파일·건강 앱과 대 보고 확인했다).
        //
        // 붙여도 **UIKit 이 보는 안전영역은 그대로 34pt 다** (찍어서 확인했다).
        // 그러니 `additionalSafeAreaInsets` 를 손대면 두 번 빼는 셈이 된다 — 건드리지 않는다.
        MainTabBar().ignoresSafeArea()
    }
}

/// 탭바만 UIKit 이다 — **SwiftUI `TabView` 에는 고른 칸 그림을 줄 자리가 없다**
///
/// `.tabItem` 도 iOS 18 의 `Tab` 도 그림을 하나만 받는다. 그래서 고른 값을 보고
/// 그림을 직접 갈아 끼웠더니, **바뀌는 시점이 어긋났다** — 그림은 누르는 즉시
/// 바뀌는데 색은 유리가 옮겨간 뒤에 따라와서, 그 사이에 "파란 선 아이콘"이라는
/// 어중간한 상태가 보였다.
///
/// UIKit 에는 그 자리가 있다 — `UITabBarItem(title:image:selectedImage:)`.
/// 넘겨 두면 **UIKit 이 제 애니메이션에 맞춰 알아서 바꾼다.** 시점이 어긋날 일이 없다.
///
/// 화면은 그대로 SwiftUI 다 (`UIHostingController`). 바꾼 것은 껍데기뿐이다.
private struct MainTabBar: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UITabBarController {
        let controller = UITabBarController()
        // 전체 목록에서 하단바에 자리가 있는 화면을 누르면 **그 탭으로 옮긴다**.
        // 컨트롤러를 약하게 잡는다 — 화면이 컨트롤러를 되잡으면 둘 다 안 풀린다
        let go: (MainTab) -> Void = { [weak controller] tab in
            // **iOS 하단바 목록을 본다** — 근태는 여기 없다 (홈 바로가기로 내려갔다).
            // 없는 탭이면 아무 일도 안 한다. 화면이 생기면 그때 잇는다
            guard let controller,
                  let index = MainTab.companion.ios.firstIndex(where: { $0 == tab })
            else { return }
            controller.selectedIndex = index
        }
        // **검색은 덮기만 한다** — 화면을 갈아 끼우지 않으니 닫으면 하던 자리로 돌아온다
        let search: () -> Void = { [weak controller] in
            guard let controller else { return }
            controller.present(SearchOverlayController(onClose: {}), animated: true)
        }
        // **출퇴근 스캔은 옆에서 밀려 들어온다** — 지금 보고 있는 탭의 내비게이션이 민다.
        // 탭바는 그 동안 숨는다 (`hidesBottomBarWhenPushed`) — 카메라 위에 탭바가 떠 있으면
        // 딴 자리로 넘어온 것이 아니다
        let scan: () -> Void = { [weak controller] in
            guard let nav = controller?.selectedViewController as? UINavigationController else { return }
            let page = UIHostingController(rootView: AttendanceScanView(onBack: {}))
            // 컨트롤러를 약하게 잡는다 — AI 페이지와 같은 이유다 (아래 `present`)
            page.rootView = AttendanceScanView { [weak page] in
                page?.navigationController?.popViewController(animated: true)
            }
            page.hidesBottomBarWhenPushed = true
            nav.pushViewController(page, animated: true)
        }
        if #available(iOS 18.0, *) {
            // **`tabs` 로 세운다.** `viewControllers` 로는 아래 AI 자리를 못 만든다.
            // 화면의 `tabBarItem` 은 그대로 둔다 — 고른 칸의 채운 그림이 거기 있다
            var tabs: [UITab] = MainTab.companion.ios.map { tab in
                UITab(title: tab.label, image: UIImage(named: tab.icon), identifier: tab.name) { _ in
                    hosted(tab, go: go, search: search, scan: scan)
                }
            }

            // **AI 는 바가 그려 준다.** `UISearchTab` 은 바 밖에 따로 서는 자리라
            // 시스템이 탭바와 **같은 유리로** 동그라미를 그린다 —
            // 우리가 유리를 흉내내지 않는다. 아이콘·제목은 갈아 끼울 수 있다.
            // 검색 필드로 펼쳐지는 것은 화면에 `searchable` 을 붙였을 때뿐이라,
            // 안 붙이면 그냥 그 화면으로 간다.
            // 화면 제공자는 **안 쓰인다** — 아래 `shouldSelectTab` 이 선택을 막고
            // 대신 페이지를 올린다. 그래도 nil 이면 UIKit 이 거부해서 빈 것을 하나 둔다
            let ai = UISearchTab { _ in UIViewController() }
            ai.title = "AI"
            // **FS 마크를 제 색 그대로 세운다.** 탭바는 그림을 기본으로 template 처리해서
            // 한 가지 색으로 눌러 버린다 — `alwaysOriginal` 이라야 그라데이션이 산다.
            // 동그라미(유리)는 시스템이 그리니 우리가 댈 것은 이 그림뿐이다
            ai.image = UIImage(named: "brand_mark")?.withRenderingMode(.alwaysOriginal)
            tabs.append(ai)

            controller.tabs = tabs
            controller.delegate = context.coordinator
        } else {
            // iOS 17 이하에는 그 자리가 없다 — 다섯 칸만 세운다
            controller.viewControllers = MainTab.companion.ios.map {
                hosted($0, go: go, search: search, scan: scan)
            }
        }

        controller.tabBar.tintColor = UIColor(HifisColor.brand)
        // **지금은 늘 어둡게 간다.** 이 한 줄이 자식 화면과 탭바까지 다 어둡게 만든다 —
        // 동적 `UIColor` 도 `UITraitCollection.current` 도 여기서 정해진다.
        // 라이트 한 벌은 그대로 두었다. 설정에서 고르게 할 때 `.unspecified` 로 되돌린다
        controller.overrideUserInterfaceStyle = .dark
        return controller
    }

    func updateUIViewController(_ controller: UITabBarController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator() }

    /// 탭바의 대리자 — **AI 는 탭을 옮기지 않고 페이지를 올린다**
    ///
    /// 탭을 옮기면 지금 보던 화면을 잃는다. AI 는 **하던 일을 두고 잠깐 묻는 자리**라
    /// 덮고 올라왔다가 닫히는 편이 맞다. `shouldSelectTab` 에서 `false` 를 돌려
    /// 선택을 막고, 그 자리에서 띄운다.
    final class Coordinator: NSObject, UITabBarControllerDelegate {
        @available(iOS 18.0, *)
        func tabBarController(
            _ tabBarController: UITabBarController,
            shouldSelectTab tab: UITab
        ) -> Bool {
            // **그 자리인지는 타입으로 알아본다.** `UITab.identifier` 는 읽기 전용이고
            // `UISearchTab` 는 만들 때 이름표를 못 준다. 어차피 그 자리는 하나뿐이다
            guard tab is UISearchTab else { return true }
            present(from: tabBarController)
            return false
        }

        private func present(from parent: UITabBarController) {
            let controller = UIHostingController(rootView: AiChatView(onClose: {}))
            // **컨트롤러를 약하게 잡는다.** 닫기 클로저는 화면이, 화면은 컨트롤러가
            // 들고 있어서 강하게 잡으면 서로 물려 페이지가 영영 안 풀린다.
            // 그래서 만든 **뒤에** 갈아 끼운다 — 만들 때는 잡을 대상이 아직 없다
            controller.rootView = AiChatView { [weak controller] in
                controller?.dismiss(animated: true)
            }
            // **이 화면만 밝다.** 앱은 다크로 못 박혀 있지만 여기는 예외라
            // 여기서 갈라 준다 — `HifisColor` 가 알아서 라이트 값을 낸다
            controller.overrideUserInterfaceStyle = .light
            // 아래에서 위로 덮고 올라온다
            controller.modalPresentationStyle = .fullScreen
            controller.modalTransitionStyle = .coverVertical
            parent.present(controller, animated: true)
        }
    }

    /// 탭 하나를 담는 자리 — **화면을 내비게이션 컨트롤러에 넣어서** 준다
    ///
    /// 상세 화면은 옆에서 밀려 들어오는데 (`DESIGN.md`), 그 밀기는 `UINavigationController` 가
    /// 하는 일이라 탭마다 하나씩 깐다. 고른 칸에 **채운 그림**을 쓰도록 `tabBarItem` 은
    /// 화면에 심는다 — 내비게이션 컨트롤러는 제 것이 없으면 뿌리 화면 것을 쓴다
    private func hosted(
        _ tab: MainTab,
        go: @escaping (MainTab) -> Void,
        search: @escaping () -> Void,
        scan: @escaping () -> Void
    ) -> UINavigationController {
        let host = UIHostingController(rootView: screen(for: tab, go: go, search: search, scan: scan))
        host.tabBarItem = UITabBarItem(
            title: tab.label,
            image: UIImage(named: tab.icon),
            selectedImage: UIImage(named: tab.iconFilled)
        )
        return TabNavigationController(rootViewController: host)
    }

    private func screen(
        for tab: MainTab,
        go: @escaping (MainTab) -> Void,
        search: @escaping () -> Void,
        scan: @escaping () -> Void
    ) -> AnyView {
        if tab == MainTab.home { return AnyView(HomeView(onSearch: search, onScan: scan)) }
        if tab == MainTab.schedule { return AnyView(ScheduleView(onSearch: search, onScan: scan)) }
        if tab == MainTab.more { return AnyView(MoreView(onTab: go, onSearch: search, onScan: scan)) }
        return AnyView(ComingSoonView(label: tab.label, onSearch: search, onScan: scan))
    }
}

/// 탭 하나의 밀어 넣기 자리 — **바는 안 보인다**
///
/// 바는 안 쓴다 — 우리 헤더가 그 자리다. 그런데 바를 숨기면 UIKit 이 **왼쪽 끝을
/// 끌어 돌아가는 손짓까지 같이 끈다.** 대리자를 우리가 맡아 되살린다 — 쌓인 화면이
/// 있을 때만 받는다. 뿌리에서까지 받으면 아무 데도 안 가는 끌기가 생긴다.
private final class TabNavigationController: UINavigationController, UIGestureRecognizerDelegate {
    override func viewDidLoad() {
        super.viewDidLoad()
        setNavigationBarHidden(true, animated: false)
        interactivePopGestureRecognizer?.delegate = self
    }

    func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        viewControllers.count > 1
    }
}

/// 아직 안 만든 탭 — **임시다**
///
/// 빈 화면으로 두면 탭을 눌렀을 때 앱이 멈춘 것처럼 보인다. 화면이 생기면 지운다.
/// 글꼴·타입 스케일이 정해지기 전이라 크기를 직접 적었다.
private struct ComingSoonView: View {
    let label: String
    var onSearch: () -> Void = {}
    var onScan: () -> Void = {}

    var body: some View {
        // **`TabPage` 를 쓴다.** 헤더와 AI 단추가 거기 있어서, 안 쓰면 이 두 탭에서만
        // 사내톡·알림으로 갈 방법도 AI 단추도 사라진다
        TabPage(onSearch: onSearch, onScan: onScan) {
            Text("\(label) — 준비 중")
                .font(.system(size: 15))
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}
