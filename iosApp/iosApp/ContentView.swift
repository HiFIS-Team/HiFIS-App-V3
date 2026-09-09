import SwiftUI
import SharedKit

struct ContentView: View {
    var body: some View {
        MainScreen()
            // SwiftUI 쪽 `@Environment(\.colorScheme)` 도 같이 어둡게 잡는다
            .preferredColorScheme(.dark)
    }
}
