import SwiftUI
import UIKit
import SharedKit

/// 앱 셸 — 하단 탭바와 탭 화면, 그 위에 덮이는 **잎**을 들고 있다
///
/// ```
/// ZStack
/// ├── MainTabBar   ← 탭 화면 + 하단 유리 탭바. 잎이 덮어도 **안 움직인다**
/// └── 잎           ← 오른쪽에서 밀려 들어와 셸을 통째로 덮는다 (탭바까지)
/// ```
///
/// 탭 목록은 `shared` 의 `MainTab` 하나만 읽는다. 여기서 새로 세우지 않는다
/// (V2 는 이 목록이 플랫폼마다 따로라 화면이 목록에서 새어 나갔다).
///
/// 바는 **`UITabBarController` 그대로** 다 — iOS 26 이 이 탭바를 리퀴드 글래스로
/// 그려 준다. 유리를 직접 흉내 내지 않는다.
///
/// ## 상세 화면은 **탭 안에서 밀지 않는다** — 셸 위에 잎으로 얹는다
///
/// 처음에는 탭마다 `UINavigationController` 를 깔고 그 안에서 push 했다. 그러면
/// **탭바가 아래로 내려가며 사라진다** (`hidesBottomBarWhenPushed`) — 대표가 보고
/// 바로 걸렀다. V2 도 MyFIS 도 화면이 옆에서 들어와 **탭바를 덮고** 지나간다.
/// `NavigationStack` 도 안 쓴다 — 내비 바는 화면들이 나눠 쓰는 크롬이라 화면이
/// 바뀔 때마다 시스템이 아이템을 morph 시키고, 하단 유리 탭바를 덮지도 못한다.
///
/// 그래서 잎은 SwiftUI 층이다. 탭바 컨트롤러는 손대지 않고, 잎이 `.move(edge: .trailing)`
/// 으로 들어와 덮는다. 안드로이드 `MainScreen` 의 `slideInHorizontally` 와 같은 그림이다.
///
/// 안드로이드는 반대로 머티리얼 3 `NavigationBar` 를 쓴다 —
/// **네비게이션은 각자 자기 OS 표준으로 간다.**
struct MainScreen: View {
    /// 어느 제품에 들어와 있나 — **셸이 들고 화면들이 읽는다**
    ///
    /// 제품이 바뀌면 탭 목록이 통째로 바뀐다. HiFIS 말고는 아직 화면이 없어서
    /// 자리 문구만 뜨고 **탭바는 아예 안 선다** — 없는 화면으로 가는 칸을 세울 수 없다.
    @StateObject private var shell = ShellState()
    /// 옆에서 밀려 들어와 셸을 덮는 잎들 — 한 번에 하나만 뜬다 (헤더가 덮이면 더 못 연다)
    @State private var scanOpen = false
    @State private var notificationOpen = false
    @State private var chatOpen = false
    /// 왼쪽 가장자리에서 끌고 있는 거리 — 잎이 손가락을 따라온다
    @State private var drag: CGFloat = 0

    /// 들어올 때 — 안드로이드 `tween(320)` 과 같은 값
    private static let push = Animation.easeOut(duration: 0.32)
    /// 나갈 때 — 안드로이드 `tween(260)` 과 같은 값
    private static let pop = Animation.easeIn(duration: 0.26)
    /// 여기서 시작한 끌기만 뒤로가기로 본다
    private static let edge: CGFloat = 24
    /// 이만큼 끌었으면 손을 떼도 닫는다
    private static let closeDistance: CGFloat = 90

    var body: some View {
        GeometryReader { proxy in
            ZStack {
                // **`ignoresSafeArea()` 를 붙인다.** 안 붙이면 SwiftUI 가 컨테이너를 홈
                // 인디케이터만큼 밀어 올리고 그 안에서 UIKit 이 또 제 여백을 잡아,
                // 바가 25pt 쯤 높이 뜬다 (파일·건강 앱과 대 보고 확인했다).
                //
                // 붙여도 **UIKit 이 보는 안전영역은 그대로 34pt 다** (찍어서 확인했다).
                // 그러니 `additionalSafeAreaInsets` 를 손대면 두 번 빼는 셈이 된다 — 건드리지 않는다.
                // 제품이 바뀌면 **셸을 통째로 갈아 끼운다** — 탭 목록이 제품마다 다르다
                if shell.product == Product.hifis {
                    MainTabBar(
                        shell: shell,
                        onScan: { withAnimation(Self.push) { scanOpen = true } },
                        onNotification: { withAnimation(Self.push) { notificationOpen = true } },
                        onChat: { withAnimation(Self.push) { chatOpen = true } }
                    )
                    .ignoresSafeArea()
                    // 덮인 셸에는 손이 닿지 않는다
                    .allowsHitTesting(!scanOpen && !notificationOpen && !chatOpen)
                } else {
                    // **탭바가 없다.** 탭 목록은 제품마다 다르다 —
                    // HiFIS 것을 그대로 두면 없는 화면으로 가는 칸이 네 개 선다.
                    // 헤더와 제품 고르개는 그대로라 돌아올 길이 있다
                    ProductComingSoonView(
                        product: shell.product,
                        onScan: { withAnimation(Self.push) { scanOpen = true } },
                        onNotification: { withAnimation(Self.push) { notificationOpen = true } },
                        onChat: { withAnimation(Self.push) { chatOpen = true } }
                    )
                    .environmentObject(shell)
                    .allowsHitTesting(!scanOpen && !notificationOpen && !chatOpen)
                }

                if scanOpen {
                    leaf(AttendanceScanView(onBack: back), width: proxy.size.width)
                }
                if notificationOpen {
                    leaf(NotificationView(onBack: back), width: proxy.size.width)
                }
                if chatOpen {
                    leaf(ChatListView(onBack: back), width: proxy.size.width)
                }
            }
        }
    }

    /// 잎 하나 — 오른쪽에서 들어와 셸을 덮고, 왼쪽 끝을 끌면 손가락을 따라온다
    private func leaf<Leaf: View>(_ view: Leaf, width: CGFloat) -> some View {
        view
            .offset(x: drag)
            .zIndex(1)
            .transition(.move(edge: .trailing))
            .gesture(edgeBack(width: width))
    }

    private func back() {
        withAnimation(Self.pop) { closeLeaves() }
        drag = 0
    }

    /// 어느 잎이 떠 있든 다 내린다 — 한 번에 하나뿐이라 가려 닫을 것이 없다
    private func closeLeaves() {
        scanOpen = false
        notificationOpen = false
        chatOpen = false
    }

    /// 왼쪽 가장자리에서 오른쪽으로 쓸면 잎을 걷는다 — UIKit push 의 뒤로 끌기와 같은 손짓이다
    ///
    /// 화면 전체에 걸되 **가장자리에서 시작한 것만** 받고, `minimumDistance` 를 줘서
    /// 움직이지 않는 탭은 제스처가 되지 않게 한다 — 안 그러면 잎 안의 단추 탭을 삼킨다.
    private func edgeBack(width: CGFloat) -> some Gesture {
        DragGesture(minimumDistance: 12, coordinateSpace: .local)
            .onChanged { value in
                guard value.startLocation.x <= Self.edge else { return }
                // 세로로 긋는 손짓은 안쪽 몫이다
                guard abs(value.translation.width) > abs(value.translation.height) else { return }
                drag = max(0, value.translation.width)
            }
            .onEnded { value in
                guard value.startLocation.x <= Self.edge else { return }
                let flung = value.predictedEndTranslation.width > 240
                guard drag > Self.closeDistance || flung else {
                    withAnimation(Self.pop) { drag = 0 }
                    return
                }
                // **손가락 위치에서 이어서 화면 밖까지 밀어낸 뒤 걷는다.**
                // 바로 걷으면 `.transition` 이 `offset` 과 같이 돌아 화면이 한 번 튄다
                withAnimation(Self.pop) { drag = width }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.26) {
                    var snap = Transaction()
                    snap.disablesAnimations = true
                    withTransaction(snap) {
                        closeLeaves()
                        drag = 0
                    }
                }
            }
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
    /// 탭 화면마다 심어 준다 — 화면은 이걸 읽어 제품 고르개를 그린다
    let shell: ShellState
    /// 헤더의 스캔 아이콘·종·말풍선 — 셸이 잎을 올린다 (`MainScreen`)
    let onScan: () -> Void
    let onNotification: () -> Void
    let onChat: () -> Void

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
        // **출퇴근 스캔·알림함은 셸 위에 잎으로 얹힌다** — 여기서는 셸에 알리기만 한다
        let scan = onScan
        let notification = onNotification
        let chat = onChat
        if #available(iOS 18.0, *) {
            // **`tabs` 로 세운다.** `viewControllers` 로는 아래 AI 자리를 못 만든다.
            // 화면의 `tabBarItem` 은 그대로 둔다 — 고른 칸의 채운 그림이 거기 있다
            var tabs: [UITab] = MainTab.companion.ios.map { tab in
                UITab(title: tab.label, image: UIImage(named: tab.icon), identifier: tab.name) { _ in
                    hosted(tab, go: go, search: search, scan: scan, notification: notification, chat: chat)
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
                hosted($0, go: go, search: search, scan: scan, notification: notification, chat: chat)
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

    /// 탭 하나를 담는 화면 — 고른 칸에 **채운 그림**을 쓰도록 `tabBarItem` 을 같이 심는다
    private func hosted(
        _ tab: MainTab,
        go: @escaping (MainTab) -> Void,
        search: @escaping () -> Void,
        scan: @escaping () -> Void,
        notification: @escaping () -> Void,
        chat: @escaping () -> Void
    ) -> UIHostingController<AnyView> {
        let host = UIHostingController(
            rootView: AnyView(
                screen(
                    for: tab, go: go, search: search, scan: scan,
                    notification: notification, chat: chat
                )
                // **여기서 심어야 한다.** 탭 화면은 이 컨트롤러 안에 한 번 만들어져
                // 앉아 있어서, 바깥에서 값을 넘기면 바뀌어도 안 따라온다
                .environmentObject(shell)
            )
        )
        host.tabBarItem = UITabBarItem(
            title: tab.label,
            image: UIImage(named: tab.icon),
            selectedImage: UIImage(named: tab.iconFilled)
        )
        return host
    }

    private func screen(
        for tab: MainTab,
        go: @escaping (MainTab) -> Void,
        search: @escaping () -> Void,
        scan: @escaping () -> Void,
        notification: @escaping () -> Void,
        chat: @escaping () -> Void
    ) -> AnyView {
        if tab == MainTab.home {
            return AnyView(
                HomeView(onSearch: search, onScan: scan, onNotification: notification, onChat: chat)
            )
        }
        if tab == MainTab.work {
            return AnyView(
                WorkView(onSearch: search, onScan: scan, onChat: chat, onNotification: notification)
            )
        }
        if tab == MainTab.schedule {
            return AnyView(
                ScheduleView(onSearch: search, onScan: scan, onNotification: notification, onChat: chat)
            )
        }
        if tab == MainTab.more {
            return AnyView(
                MoreView(onTab: go, onSearch: search, onScan: scan, onNotification: notification, onChat: chat)
            )
        }
        return AnyView(
            ComingSoonView(
                label: tab.label, onSearch: search, onScan: scan,
                onNotification: notification, onChat: chat
            )
        )
    }
}

/// 아직 셸이 없는 제품 — **탭바가 없다**
///
/// 헤더와 제품 고르개는 `TabPage` 가 그려 줘서 **돌아올 길이 있다.**
/// TeamFIS·WeFIS 셸이 생기면 지운다.
private struct ProductComingSoonView: View {
    let product: Product
    var onScan: () -> Void = {}
    var onNotification: () -> Void = {}
    var onChat: () -> Void = {}

    var body: some View {
        // 헤더 아이콘을 안 이으면 이 화면에서만 사내톡·알림으로 갈 길이 없어진다
        TabPage(onScan: onScan, onChat: onChat, onNotification: onNotification) {
            Text(Product.companion.comingSoon(product: product))
                .font(.system(size: 15))
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
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
    var onNotification: () -> Void = {}
    var onChat: () -> Void = {}

    var body: some View {
        // **`TabPage` 를 쓴다.** 헤더와 AI 단추가 거기 있어서, 안 쓰면 이 두 탭에서만
        // 사내톡·알림으로 갈 방법도 AI 단추도 사라진다
        TabPage(onSearch: onSearch, onScan: onScan, onChat: onChat, onNotification: onNotification) {
            Text("\(label) — 준비 중")
                .font(.system(size: 15))
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}
