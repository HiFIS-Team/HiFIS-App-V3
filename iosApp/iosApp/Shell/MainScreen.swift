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
                // 제품이 바뀌면 **셸을 통째로 갈아 끼운다** — 탭 목록이 제품마다 다르다.
                // 툭 갈리면 앱이 튄 것처럼 보여서 **서로 녹아든다** (안드로이드 `Crossfade` 와 같은 그림).
                //
                // `if` 대신 `ForEach` 로 세우는 이유는 **정체성** 때문이다 — 제품이 바뀌면
                // 하나가 빠지고 하나가 들어와야 `.transition` 이 걸린다
                // **셸은 즉시 갈린다.** 녹이면(`opacity`) 리퀴드 글래스가 사라진다 —
                // 유리는 뒤를 퍼 와 흐리는 효과라 반투명한 겹 안에서는 못 그린다.
                // 대신 얼려 둔 그림이 그 **위에서** 흐려진다 (아래 `frozen`)
                productShell(shell.product)
                    // **`.id` 가 있어야 탭바가 새로 선다.** 없으면 SwiftUI 가 같은
                    // `UIViewControllerRepresentable` 을 재활용해서 칸이 그대로 남는다
                    .id(shell.product.name)
                    // 덮인 셸에는 손이 닿지 않는다
                    .allowsHitTesting(!scanOpen && !notificationOpen && !chatOpen)

                // 전환 직전에 얼려 둔 화면 — 이게 흐려지면서 새 셸이 드러난다.
                // **그림이라 반투명해도 안 깨진다** (유리와 달리)
                if let frozen = shell.frozen {
                    Image(uiImage: frozen)
                        .resizable()
                        .ignoresSafeArea()
                        .transition(.opacity)
                        .allowsHitTesting(false)
                        .zIndex(2)
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

    /// 셸 한 겹 — **제품을 인자로 받는다.** 셸에서 읽으면 나가는 겹이 새 제품을 그린다
    @ViewBuilder
    private func productShell(_ shown: Product) -> some View {
        if !MainTab.companion.ios(product: shown).isEmpty {
            MainTabBar(
                shell: shell,
                product: shown,
                brand: HifisBrand.palette(shown).brand,
                onScan: { withAnimation(Self.push) { scanOpen = true } },
                onNotification: { withAnimation(Self.push) { notificationOpen = true } },
                onChat: { withAnimation(Self.push) { chatOpen = true } }
            )
        } else {
            // **탭바가 없다.** 탭 목록은 제품마다 다르다 —
            // HiFIS 것을 그대로 두면 없는 화면으로 가는 칸이 네 개 선다.
            // 헤더와 제품 고르개는 그대로라 돌아올 길이 있다
            ShellScope(product: shown) {
                ProductComingSoonView(
                    product: shown,
                    onScan: { withAnimation(Self.push) { scanOpen = true } },
                    onNotification: { withAnimation(Self.push) { notificationOpen = true } },
                    onChat: { withAnimation(Self.push) { chatOpen = true } }
                )
            }
            .environmentObject(shell)
        }
    }

    /// 잎 하나 — 오른쪽에서 들어와 셸을 덮고, 왼쪽 끝을 끌면 손가락을 따라온다
    private func leaf<Leaf: View>(_ view: Leaf, width: CGFloat) -> some View {
        ShellScope(product: shell.product) { view }
            .environmentObject(shell)
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

/// 하단 탭바 — **SwiftUI `TabView`**
///
/// 바는 iOS 26 이 리퀴드 글래스로 그린다. 유리·캡슐·움직임에 손대지 않는다.
///
/// ## 칸 그림은 **애플 심볼**이다 (`MainTab.symbol`)
///
/// 하단바에서만 그렇다 — 전체 목록·바로가기·헤더는 그대로 우리 그림이다.
///
/// **고른 칸을 채우는 일을 우리가 하지 않는다.** 유리가 덮은 칸을 채움 벌(`.fill`)로
/// 바꿔 그리는 것은 iOS 가 하는데, **애플 심볼일 때만** 해 준다. 우리 그림을 쓰는 동안은
/// 우리가 시점을 재야 했고 어디에 걸어도 한쪽이 어긋났다 — 특히 **유리를 손으로 끄는
/// 동안**에는 바꿀 신호 자체가 안 왔다 (대표가 세 번 봤다, 2026-09-11).
/// 우리 그림을 커스텀 심볼로 담아 보는 길까지 해 봤다 — `.claude/DEVLOG.md` 에 있다.
private struct MainTabBar: View {
    /// 탭 화면마다 심어 준다 — 화면은 이걸 읽어 제품 고르개를 그린다
    let shell: ShellState
    /// **이 겹이 그리는 제품** — 셸에서 읽지 않는다. 녹아드는 동안 겹마다 다르다
    let product: Product
    /// 제품의 브랜드색 — 바의 tint 다
    let brand: Color
    /// 헤더의 스캔 아이콘·종·말풍선 — 셸이 잎을 올린다 (`MainScreen`)
    let onScan: () -> Void
    let onNotification: () -> Void
    let onChat: () -> Void

    /// 고른 칸 — **이름으로 들고 있다** (`MainTab` 은 값 타입이 아니라 태그로 쓰기 나쁘다).
    /// 제품이 바뀌면 셸이 통째로 새로 서서 홈부터 시작한다 (`MainScreen` 의 `.id`)
    @State private var picked = ""
    /// 검색칸에 친 글자 — 아직 뒤질 것이 없다 (`AppSearch`)
    @State private var query = ""

    /// 바 밖 동그라미의 태그 — 탭 이름과 겹치지 않게 대문자로 둔다
    private static let side = "SIDE"

    private var tabs: [MainTab] { MainTab.companion.ios(product: product) }
    private var slot: MainTab.SideSlot? { MainTab.companion.iosSideSlot(product: product) }

    var body: some View {
        Group {
            if #available(iOS 18.0, *) {
                glass
            } else {
                legacy
            }
        }
        .tint(brand)
        // 첫 칸은 홈이다 (`MoreRowTest` 가 제품마다 지킨다)
        .onAppear { if picked.isEmpty { picked = tabs.first?.name ?? "" } }
    }

    /// 고른 칸 — **AI 동그라미는 여기서 가로챈다**
    private var selection: Binding<String> {
        Binding(
            get: { picked },
            set: { value in
                // **AI 는 탭이 아니다.** 탭을 옮기면 보던 화면을 잃는데, AI 는 하던 일을
                // 두고 잠깐 묻는 자리라 덮고 올라왔다 닫히는 편이 맞다.
                // `picked` 를 그대로 두면 고른 칸이 안 옮겨 간다
                if value == Self.side, slot == MainTab.SideSlot.ai {
                    presentAi()
                    return
                }
                picked = value
            }
        )
    }

    @available(iOS 18.0, *)
    private var glass: some View {
        TabView(selection: selection) {
            ForEach(tabs, id: \.name) { tab in
                Tab(tab.label, systemImage: tab.symbol, value: tab.name) { page(tab) }
            }

            // **동그라미는 바가 그려 준다.** `role: .search` 인 칸은 유리 바에서 떨어져
            // 옆에 동그랗게 서고, 시스템이 **탭바와 같은 유리로** 그린다 (애플뮤직과 같은 자리).
            // 거기 앉는 것은 제품이 정한다 — HiFIS 는 AI, TeamFIS 는 검색
            if let slot {
                Tab(slot.label, image: slot.icon, value: Self.side, role: .search) {
                    sidePage(slot)
                }
            }
        }
    }

    /// iOS 17 이하 — 동그라미 자리가 없다. 탭만 세운다
    private var legacy: some View {
        TabView(selection: selection) {
            ForEach(tabs, id: \.name) { tab in
                page(tab)
                    .tabItem { Label(tab.label, systemImage: tab.symbol) }
                    .tag(tab.name)
            }
        }
    }

    /// 탭 하나의 화면 — `ShellScope` 가 이 겹의 제품과 브랜드색을 내려 준다
    private func page(_ tab: MainTab) -> some View {
        ShellScope(product: product) { screen(for: tab) }
            .environmentObject(shell)
    }

    @ViewBuilder
    private func sidePage(_ slot: MainTab.SideSlot) -> some View {
        if slot == MainTab.SideSlot.search {
            // **바가 검색칸으로 변신한다** (애플뮤직, 2026-09-11 대표).
            // 그 변신은 `.searchable` 을 붙여야 iOS 26 이 해 준다 — 우리가 그리는 것이 아니다.
            // 내비 바는 숨긴다. `NavigationStack` 은 검색칸이 앉을 자리를 만들려고 둔 것뿐이다
            NavigationStack {
                SearchTabView()
                    .toolbar(.hidden, for: .navigationBar)
                    .searchable(text: $query, prompt: slot.label)
            }
        } else {
            // AI 는 여기로 안 온다 — 위 `selection` 이 가로채 페이지를 덮어 올린다
            Color.clear
        }
    }

    private func screen(for tab: MainTab) -> AnyView {
        // 전체 목록에서 하단바에 자리가 있는 화면을 누르면 **그 탭으로 옮긴다**.
        // 없는 탭이면 아무 일도 안 한다 (근태는 iOS 탭에 없다 — 홈 바로가기로 내려갔다)
        let go: (MainTab) -> Void = { target in
            guard tabs.contains(where: { $0 == target }) else { return }
            picked = target.name
        }
        let search = presentSearch
        // **화면은 제품이 가진다.** TeamFIS 의 홈·일정은 HiFIS 것과 디자인이 다를 예정이라
        // 빌려 쓰지 않는다 (2026-09-11 대표) — 아직 자리 문구만 뜬다
        guard product == Product.hifis else {
            return AnyView(
                ComingSoonView(
                    label: tab.label, isHome: tab == MainTab.home,
                    onSearch: search, onScan: onScan,
                    onNotification: onNotification, onChat: onChat
                )
            )
        }
        if tab == MainTab.home {
            return AnyView(
                HomeView(onSearch: search, onScan: onScan, onNotification: onNotification, onChat: onChat)
            )
        }
        if tab == MainTab.work {
            return AnyView(
                WorkView(onSearch: search, onScan: onScan, onChat: onChat, onNotification: onNotification)
            )
        }
        if tab == MainTab.schedule {
            return AnyView(
                ScheduleView(onSearch: search, onScan: onScan, onNotification: onNotification, onChat: onChat)
            )
        }
        if tab == MainTab.more {
            return AnyView(
                MoreView(onTab: go, onSearch: search, onScan: onScan, onNotification: onNotification, onChat: onChat)
            )
        }
        return AnyView(
            ComingSoonView(
                label: tab.label, isHome: tab == MainTab.home,
                onSearch: search, onScan: onScan,
                onNotification: onNotification, onChat: onChat
            )
        )
    }

    // MARK: - 덮어 올리는 두 자리
    //
    // 둘 다 UIKit 으로 올린다. 검색판은 **뒤를 흐리게 덮어야 해서** `UIVisualEffectView`
    // 가 필요하고 (SwiftUI 재질은 제 나무의 뒤만 뜬다), AI 페이지는 탭바까지 통째로
    // 가리는 `.fullScreen` 이라야 한다. 창의 맨 위 컨트롤러에서 올린다

    private func presentSearch() {
        // **검색은 덮기만 한다** — 화면을 갈아 끼우지 않으니 닫으면 하던 자리로 돌아온다
        Self.top()?.present(SearchOverlayController(onClose: {}), animated: true)
    }

    private func presentAi() {
        guard let parent = Self.top() else { return }
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

    /// 창의 맨 위 컨트롤러 — 이미 덮인 것이 있으면 그 위에 올린다
    private static func top() -> UIViewController? {
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
            // **고르개를 여기서도 그린다.** `TabPage` 가 아니라 홈이 들고 있는데
            // 이 제품에는 홈이 없다 — 안 그리면 들어와서 못 나간다
            ProductSwitch()
                .padding(.top, 16)
            Text(Product.companion.comingSoon(product: product))
                .font(.system(size: 15))
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

/// 검색 탭의 본문 — **검색칸은 여기 없다**
///
/// 칸은 탭바 자리에 시스템이 그린다 (`Coordinator.searchScreen`). 여기 드는 것은
/// 그 위에 남는 본문뿐이라, 지금은 **뒤질 것이 없다는 말** 한 줄이다
/// (HiFIS 검색판을 빌려 쓰지 않는다 — 2026-09-11 대표).
private struct SearchTabView: View {
    var body: some View {
        Text(AppSearch.shared.EMPTY)
            .font(.system(size: 15))
            .foregroundStyle(HifisColor.inkTertiary)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(HifisColor.background.ignoresSafeArea())
    }
}

/// 아직 안 만든 탭 — **임시다**
///
/// 빈 화면으로 두면 탭을 눌렀을 때 앱이 멈춘 것처럼 보인다. 화면이 생기면 지운다.
/// 글꼴·타입 스케일이 정해지기 전이라 크기를 직접 적었다.
private struct ComingSoonView: View {
    let label: String
    /// 홈 탭인가 — **고르개가 여기 선다.** 자리 문구여도 세워야 제품에서 나올 수 있다
    var isHome: Bool = false
    var onSearch: () -> Void = {}
    var onScan: () -> Void = {}
    var onNotification: () -> Void = {}
    var onChat: () -> Void = {}

    var body: some View {
        // **`TabPage` 를 쓴다.** 헤더와 AI 단추가 거기 있어서, 안 쓰면 이 두 탭에서만
        // 사내톡·알림으로 갈 방법도 AI 단추도 사라진다
        TabPage(onSearch: onSearch, onScan: onScan, onChat: onChat, onNotification: onNotification) {
            // **고르개는 홈 탭에 선다 — 화면이 자리 문구여도 마찬가지다.**
            // 안 그리면 그 제품에 들어간 사람이 나올 길을 잃는다 (대표가 걸렸다, 2026-09-11)
            if isHome {
                ProductSwitch()
                    .padding(.top, 16)
            }
            Text("\(label) — 준비 중")
                .font(.system(size: 15))
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}
