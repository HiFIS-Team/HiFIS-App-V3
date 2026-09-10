import SwiftUI

/// 떠 있는 AI 채팅 단추 — **탭 다섯 곳에 다 뜬다**
///
/// 그래서 화면이 아니라 **셸(`MainScreen`)이 들고 있다.** 화면마다 얹으면
/// 언젠가 한 탭만 빠지는데, 늘 같은 자리에 있는 것이 이 단추의 전부다.
/// (업무·근태는 아직 `ComingSoonView` 라 껍데기조차 안 쓴다 — 셸에 두면 그것도 덮는다.)
///
/// **그림자를 쓰는 유일한 자리다.** 화면 안은 평평하게 가지만 이건 본문 위로
/// 떠 있어야 해서, 굴러 올라오는 카드와 겹칠 때 층이 안 갈리면 얹힌 것처럼 안 보인다.
///
/// 브랜드색을 면으로 깐다 — 강조 하나를 여기 쓴다.
struct AiChatButton: View {
    var onTap: () -> Void = {}

    var body: some View {
        Button(action: onTap) {
            Image("ic_ai")
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.aiChatIcon, height: HifisSize.aiChatIcon)
                .foregroundStyle(.white)
                .frame(width: HifisSize.aiChatButton, height: HifisSize.aiChatButton)
                .background(HifisColor.brand, in: Circle())
                .shadow(color: .black.opacity(0.35), radius: 10, y: 4)
                .contentShape(Circle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel("AI 채팅")
    }
}
