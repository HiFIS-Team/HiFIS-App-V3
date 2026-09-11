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
    @Published var product: Product = Product.companion.default_

    /// 고르개가 쓰는 차례 — `Product` 를 index 로 바꿔 들고 있는다
    var index: Int {
        get { Product.companion.all.firstIndex(of: product) ?? 0 }
        set { product = Product.companion.all[min(max(newValue, 0), Product.companion.all.count - 1)] }
    }
}

/// 제품 고르개 — **헤더 바로 아래**, 탭 화면 전부에 선다
///
/// 칸 고르개(공통/개인 · 전체/안읽음)와 **같은 부품**이다 (대표 요청, 2026-09-11).
/// 트레이너는 출근을 HiFIS 에서 찍고 수업은 TeamFIS 에서 해서 하루에 여러 번 오간다 —
/// 한 탭에만 두면 그 탭을 거쳐 가야 한다.
///
/// 제품이 바뀌면 **탭바까지 통째로 바뀐다.** 그래서 탭바 안이 아니라 그 위에 선다.
struct ProductSwitch: View {
    @EnvironmentObject private var shell: ShellState

    var body: some View {
        ModeSwitch(
            segments: Product.companion.labels,
            selected: Binding(get: { shell.index }, set: { shell.index = $0 })
        )
    }
}
