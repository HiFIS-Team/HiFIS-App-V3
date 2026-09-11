import SwiftUI
import SharedKit

/// 지금 어느 제품에 들어와 있나 — **셸이 들고 화면은 읽기만 한다**
///
/// 화면마다 `product` 를 손으로 넘기지 않는다. 제품은 홈·업무·일정이 신경 쓸 것이
/// 아니라 **그 화면들을 담고 있는 껍데기의 상태**다.
///
/// 그리고 iOS 는 넘길 수도 없다 — 탭 화면은 `UIHostingController` 안에 한 번
/// 만들어져 앉아 있어서, 생성 때 박아 넣은 값은 나중에 바뀌어도 안 따라온다.
/// 관찰되는 객체를 하나 심어 두면 값이 바뀔 때 화면이 알아서 다시 그린다.
/// (안드로이드는 `LocalProduct` — 같은 생각을 각자 OS 방식으로 한다.)
final class ShellState: ObservableObject {
    @Published private(set) var product: Product = Product.companion.default_

    /// 고르개가 쓰는 차례 — `Product` 를 index 로 바꿔 들고 있는다
    var index: Int {
        get { Product.companion.all.firstIndex(of: product) ?? 0 }
        set { move(to: Product.companion.all[min(max(newValue, 0), Product.companion.all.count - 1)]) }
    }

    func move(to next: Product) {
        guard next != product else { return }
        product = next
    }

    /// 셸이 서로 녹아드는 데 걸리는 시간 — 안드로이드 `SHELL_FADE` 와 같은 값
    ///
    /// **색을 따로 물들이지 않는다.** 두 겹이 겹쳐 보이는 동안 색도 저절로 옮겨 간다.
    /// 색까지 따로 보간하면 **나가는 겹이 중간색으로 칠해져** — HiFIS 가 빠지는 동안
    /// 보라색이 된다. 겹마다 제 색이어야 한다.
    static let fade: CFTimeInterval = 0.42
}

/// 이 겹의 제품과 브랜드색을 아래로 내려보내는 껍데기
///
/// **제품 하나가 색 한 벌이다.** 셸이 통째로 녹아드니 색을 따로 움직일 일이 없다 —
/// 나가는 겹은 제 색 그대로 흐려지고 들어오는 겹이 제 색으로 짙어진다.
struct ShellScope<Content: View>: View {
    /// **이 겹이 그리는 제품** — 셸에서 읽지 않는다. 녹아드는 동안 겹마다 다르다
    let product: Product
    @ViewBuilder var content: () -> Content

    var body: some View {
        let palette = HifisBrand.palette(product)
        content()
            .environment(\.product, product)
            .environment(\.brand, palette.brand)
            .environment(\.brandGradientStart, palette.start)
            .environment(\.brandGradientEnd, palette.end)
    }
}

/// 제품 고르개 — **헤더 바로 아래**, 탭 화면 전부에 선다
///
/// 칸 고르개(공통/개인 · 전체/안읽음)와 **같은 부품**이다 (대표 요청, 2026-09-11).
/// 트레이너는 출근을 HiFIS 에서 찍고 수업은 TeamFIS 에서 해서 하루에 여러 번 오간다 —
/// 한 탭에만 두면 그 탭을 거쳐 가야 한다.
///
/// 제품이 바뀌면 **탭바까지 통째로 바뀐다.** 그래서 탭바 안이 아니라 그 위에 선다.
///
/// ## 알약이 안 미끄러진다 (`slides: false`)
///
/// 제품을 옮기면 셸이 통째로 새로 서고, **이 고르개도 같이 새로 선다.**
/// 새로 선 알약에는 미끄러져 올 자리가 없어서 그냥 제자리에 나타난다.
///
/// 그런데 TeamFIS ↔ WeFIS 는 둘 다 자리 화면이라 화면이 안 바뀌어서 **거기서만
/// 미끄러졌다.** 셋 중 하나만 다르게 움직이니 그게 더 이상하게 보였다
/// (대표가 봤다 — "하이피스랑 팀피스는 딱딱 바로, 팀피스랑 위피스는 자연스럽고").
///
/// 그래서 **셋 다 즉시 옮긴다.** 앱이 통째로 갈리는 자리라 알약만 천천히 따라가는 것도
/// 어차피 맞지 않는다. 미끄러지는 것은 화면이 그대로 있는 칸 고르개(전체/안읽음 ·
/// 공통/개인)의 몫이다.
struct ProductSwitch: View {
    @EnvironmentObject private var shell: ShellState
    /// **보여주는 것은 이 겹의 제품이다** — 나가는 겹은 나가는 제품을 고른 채로 흐려진다
    @Environment(\.product) private var product

    var body: some View {
        ModeSwitch(
            segments: Product.companion.labels,
            // **셸이 서로 녹아들게 감싼다.** 색만 물들고 화면은 툭 갈리면 따로 논다
            selected: Binding(
                get: { Product.companion.all.firstIndex(of: product) ?? 0 },
                set: { next in
                    withAnimation(.easeInOut(duration: ShellState.fade)) { shell.index = next }
                }
            ),
            slides: false
        )
    }
}
