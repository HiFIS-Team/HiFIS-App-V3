import SwiftUI
import SharedKit

/// 사내톡 목록 — **옆에서 밀려 들어오는 잎** (헤더의 말풍선이 연다)
///
/// 디스코드 대화 목록을 옮겼다 (대표 지목, 2026-09-10). 위에서부터
/// 단추 줄(검색·직원 찾기·새 대화) · 접속 중인 동료 가로줄 · 대화방 세로 목록,
/// 그리고 화면 맨 아래에 **내 칸**이 붙는다. 안드로이드 `ChatListScreen` 과 같은 화면이다.
///
/// **왼쪽 세로 띠는 안 가져왔다.** 디스코드에서 그 자리는 서버 목록인데
/// 사내톡은 회사 하나라 넣을 것이 없다. 빈 띠를 세우면 화면 폭만 먹는다.
///
/// **아직 서버가 없다.** 목록은 `ChatRoom.demo` 이고 눌러도 방이 안 열린다 — 방 화면이 없다.
struct ChatListView: View {
    let onBack: () -> Void

    /// 시각은 한 번만 잡는다 — 매 프레임 흐르면 `40분` 이 보는 중에 바뀐다
    private let now: Kotlinx_datetimeLocalDateTime
    private let rooms: [ChatRoom]
    private let mates: [ChatMate]
    private let me: ChatMate

    init(onBack: @escaping () -> Void) {
        self.onBack = onBack
        let parts = Foundation.Calendar.current.dateComponents(
            [.year, .month, .day, .hour, .minute], from: Date()
        )
        // 시각은 `shared` 가 짓는다 — 생성자를 직접 부르면 같은 이름이 둘이라 어느 것이 나올지 모른다
        let now = NotificationBox.shared.at(
            year: Int32(parts.year ?? 2026),
            month: Int32(parts.month ?? 1),
            day: Int32(parts.day ?? 1),
            hour: Int32(parts.hour ?? 0),
            minute: Int32(parts.minute ?? 0)
        )
        self.now = now
        rooms = ChatRoom.companion.demo(now: now)
        mates = ChatRoom.companion.demoMates()
        me = ChatRoom.companion.demoMe()
    }

    var body: some View {
        VStack(spacing: 0) {
            header
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer().frame(height: Self.headerBodyGap)
                    actionRow
                    Spacer().frame(height: Self.sectionGap)
                    mateRow
                    Spacer().frame(height: Self.sectionGap)
                    if rooms.isEmpty {
                        Text(ChatBox.shared.EMPTY)
                            .font(HifisFont.body)
                            .foregroundStyle(HifisColor.inkSecondary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, Self.emptyPad)
                    } else {
                        ForEach(rooms, id: \.id) { room in
                            RoomRow(room: room, now: now)
                        }
                    }
                    Spacer().frame(height: Self.sectionGap)
                }
            }
            MeBar(me: me)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(HifisColor.background.ignoresSafeArea())
    }

    /// 잎 헤더 — 왼쪽 뒤로가기, 그 옆에 화면 이름. 알림함과 같은 줄이다
    private var header: some View {
        HStack(spacing: 0) {
            HeaderIconButton(icon: "ic_chevron_left", label: "뒤로", action: onBack)
            Spacer().frame(width: Self.headerTitleGap)
            Text(ChatBox.shared.TITLE)
                .font(HifisFont.header)
                .foregroundStyle(HifisColor.ink)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, HifisSize.screenEdge - HifisSize.headerIconInset)
        .frame(height: HifisSize.headerHeight)
    }

    /// 단추 줄 — 검색 · 직원 찾기 · 새 대화
    ///
    /// 참고한 화면은 가운데가 `친구 추가하기` 였다. 회사에는 친구를 맺는 절차가 없어서
    /// (다 같은 조직이다) **직원 찾기**로 바꿨다 — 조직도에서 사람을 골라 1:1 을 여는 자리다.
    /// 오른쪽 `+` 는 여럿을 부르는 새 대화라 둘이 하는 일이 다르다.
    ///
    /// **아직 셋 다 갈 곳이 없다.**
    private var actionRow: some View {
        let box = ChatBox.shared
        return HStack(spacing: 8) {
            Button {} label: {
                Image("ic_search")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                    .foregroundStyle(HifisColor.inkSecondary)
                    .frame(width: Self.actionHeight, height: Self.actionHeight)
                    .background(HifisColor.surface, in: Circle())
                    .contentShape(Circle())
            }
            .buttonStyle(TapStyle())
            .accessibilityLabel(box.SEARCH)

            Button {} label: {
                HStack(spacing: 8) {
                    Image("ic_people")
                        .renderingMode(.template)
                        .resizable()
                        .frame(width: 20, height: 20)
                        .foregroundStyle(HifisColor.ink)
                    Text(box.FIND_STAFF)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(HifisColor.ink)
                }
                .frame(maxWidth: .infinity)
                .frame(height: Self.actionHeight)
                .background(
                    HifisColor.surface,
                    in: RoundedRectangle(cornerRadius: Self.actionRadius, style: .continuous)
                )
                .contentShape(Rectangle())
            }
            .buttonStyle(TapStyle())

            // **새 대화만 브랜드색이다.** 이 줄에서 새로 만드는 것은 이것뿐이라
            // 화면당 강조 한 곳 규칙을 여기에 쓴다
            Button {} label: {
                Image("ic_plus")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                    .foregroundStyle(.white)
                    .frame(width: Self.actionHeight, height: Self.actionHeight)
                    .background(
                        HifisColor.brand,
                        in: RoundedRectangle(cornerRadius: Self.actionRadius, style: .continuous)
                    )
                    .contentShape(Rectangle())
            }
            .buttonStyle(TapStyle())
            .accessibilityLabel(box.NEW_ROOM)
        }
        .padding(.horizontal, HifisSize.screenEdge)
    }

    /// 접속 중인 동료 — 가로로 한 장씩
    ///
    /// 참고한 화면은 사진만 있고 이름이 없었다. 우리는 **사진이 없어서** 글자 아바타라
    /// 이름을 같이 적는다 — 글자만 두면 누구인지 못 읽는다.
    @ViewBuilder
    private var mateRow: some View {
        if !mates.isEmpty {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(mates, id: \.id) { mate in
                        Button {} label: {
                            VStack(spacing: 8) {
                                Avatar(name: mate.name, size: Self.mateAvatar, presence: mate.presence)
                                Text(mate.name)
                                    .font(HifisFont.caption)
                                    .foregroundStyle(HifisColor.inkSecondary)
                                    .lineLimit(1)
                            }
                            .padding(.horizontal, 6)
                            .frame(width: Self.mateCard, height: Self.mateCard)
                            .background(
                                HifisColor.surface,
                                in: RoundedRectangle(cornerRadius: Self.mateRadius, style: .continuous)
                            )
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(TapStyle())
                    }
                }
                .padding(.horizontal, HifisSize.screenEdge)
            }
        }
    }

    /// 뒤로가기 터치 자리와 화면 이름 사이 — 알림함과 같은 값
    private static let headerTitleGap: CGFloat = 2
    /// 헤더와 단추 줄 사이
    private static let headerBodyGap: CGFloat = 12
    /// 단추 줄 · 동료 줄 · 방 목록을 가르는 여백
    fileprivate static let sectionGap: CGFloat = 16
    /// 단추 줄 높이와 모서리 — 셋이 같은 높이로 서야 줄이 맞는다
    private static let actionHeight: CGFloat = 46
    private static let actionRadius: CGFloat = 14
    /// 접속 중 동료 카드 — 한 장 크기·모서리·안의 아바타
    private static let mateCard: CGFloat = 104
    private static let mateRadius: CGFloat = 16
    private static let mateAvatar: CGFloat = 52
    /// 대화가 하나도 없을 때 그 자리의 위아래 여백
    private static let emptyPad: CGFloat = 52
}

/// 대화방 한 줄 — 아바타 · 이름 · 마지막 말 · 시각
///
/// **안 읽은 방만 진하다.** 읽은 방은 이름도 미리보기도 가라앉아서 훑을 때 새것만 튄다.
/// 알림 끈 방은 줄 전체가 흐려지고 시각 앞에 꺼진 종이 붙는다.
private struct RoomRow: View {
    let room: ChatRoom
    let now: Kotlinx_datetimeLocalDateTime

    var body: some View {
        let unread = room.unreadCount > 0
        Button {
            // 아직 갈 곳이 없다 — 방 화면이 생기면 잇는다
        } label: {
            HStack(spacing: 0) {
                if room.isGroup {
                    GroupAvatar(name: room.title, size: Self.avatar)
                } else {
                    Avatar(name: room.title, size: Self.avatar, presence: room.presence)
                }
                Spacer().frame(width: 12)
                VStack(alignment: .leading, spacing: 2) {
                    Text(room.title)
                        .font(.system(size: 16, weight: unread ? .semibold : .medium))
                        .foregroundStyle(HifisColor.ink)
                        .lineLimit(1)
                    Text(ChatBox.shared.preview(room: room))
                        .font(.system(size: 14))
                        .foregroundStyle(unread ? HifisColor.inkSecondary : HifisColor.inkTertiary)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Spacer().frame(width: 10)
                VStack(alignment: .trailing, spacing: 6) {
                    HStack(spacing: 4) {
                        if room.muted {
                            Image("ic_bell_off")
                                .renderingMode(.template)
                                .resizable()
                                .frame(width: Self.muteIcon, height: Self.muteIcon)
                                .foregroundStyle(HifisColor.inkTertiary)
                                .accessibilityLabel("알림 꺼짐")
                        }
                        Text(ChatBox.shared.timeLabel(at: room.at, now: now))
                            .font(HifisFont.caption)
                            .foregroundStyle(HifisColor.inkTertiary)
                    }
                    if unread {
                        // **여기는 숫자를 쓴다.** 헤더 종·말풍선의 점과 다르다 — 저기는 "볼 게 있다"
                        // 하나만 말하면 되지만, 방이 여럿이면 어느 방이 얼마나 밀렸는지가 곧 순서다
                        Text("\(room.unreadCount)")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, 7)
                            .frame(height: Self.badgeHeight)
                            .background(HifisColor.brand, in: Capsule())
                    }
                }
            }
            .padding(.horizontal, HifisSize.screenEdge)
            .padding(.vertical, Self.padV)
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        // 알림을 끈 방은 통째로 가라앉는다 — 안 읽어도 재촉하지 않는 방이다
        .opacity(room.muted ? Self.mutedDim : 1)
    }

    private static let padV: CGFloat = 8
    private static let avatar: CGFloat = 54
    private static let muteIcon: CGFloat = 14
    private static let badgeHeight: CGFloat = 20
    /// 알림 끈 방을 얼마나 가라앉히나
    private static let mutedDim: Double = 0.55
}

/// 내 칸 — 화면 맨 아래에 붙는다. 내 아바타·이름·상태와 알림 끄기
private struct MeBar: View {
    let me: ChatMate

    var body: some View {
        HStack(spacing: 0) {
            Avatar(name: me.name, size: Self.avatar, presence: me.presence)
            Spacer().frame(width: 10)
            VStack(alignment: .leading, spacing: 0) {
                Text(me.name)
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(HifisColor.ink)
                Text(ChatBox.shared.presenceLabel(presence: me.presence))
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
            }
            Spacer(minLength: 0)
            Button {} label: {
                Image("ic_bell")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                    .foregroundStyle(HifisColor.inkSecondary)
                    .frame(width: HifisSize.headerIconButton, height: HifisSize.headerIconButton)
                    .contentShape(Rectangle())
            }
            .buttonStyle(TapStyle())
            .accessibilityLabel("알림 끄기")
        }
        .padding(.horizontal, 10)
        .frame(height: Self.height)
        .background(
            HifisColor.surface,
            in: RoundedRectangle(cornerRadius: Self.radius, style: .continuous)
        )
        .padding(.horizontal, HifisSize.screenEdge)
        .padding(.vertical, 8)
    }

    private static let height: CGFloat = 60
    private static let radius: CGFloat = 20
    private static let avatar: CGFloat = 40
}

/// 사람 아바타 — **사진이 없다.** 이름 글자를 색 원에 넣는다
///
/// 글자와 색은 `shared` 가 정한다 (`ChatBox.initial` · `ChatBox.colorIndex`) —
/// 두 플랫폼이 같은 사람에게 같은 글자·같은 색을 준다.
private struct Avatar: View {
    let name: String
    let size: CGFloat
    let presence: ChatPresence?

    var body: some View {
        Circle()
            .fill(HifisEventColor.at(Int32(ChatBox.shared.colorIndex(name: name))))
            .frame(width: size, height: size)
            .overlay {
                Text(ChatBox.shared.initial(name: name))
                    // 원 지름에 견줘 잡는다 — 자리마다 크기가 달라서 값을 박으면 큰 원이 허전하다
                    .font(.system(size: size * 0.34, weight: .bold))
                    .foregroundStyle(.white)
            }
            .overlay(alignment: .bottomTrailing) {
                if let presence { PresenceDot(presence: presence) }
            }
    }
}

/// 그룹방 아바타 — 사람 하나로 못 말한다. 색 원에 사람 둘
///
/// **색은 방 이름에서 나온다.** 처음에 브랜드색으로 두었더니 그룹방 셋이 다 같은
/// 파랑이라 목록에서 안 갈렸다. 브랜드색은 이 화면에서 `새 대화` 하나만 쓴다.
private struct GroupAvatar: View {
    let name: String
    let size: CGFloat

    var body: some View {
        Circle()
            .fill(HifisEventColor.at(Int32(ChatBox.shared.colorIndex(name: name))))
            .frame(width: size, height: size)
            .overlay {
                Image("ic_people")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: size * 0.5, height: size * 0.5)
                    .foregroundStyle(.white)
            }
    }
}

/// 접속 상태 점 — 아바타 오른쪽 아래
///
/// **바탕색 테를 먼저 깔고 그 안에 색 원을 넣는다.** 테두리로 두르면 바깥
/// 안티에일리어싱 틈으로 아래 색이 비쳐 흐린 테가 생긴다 (헤더 배지에서 겪었다).
private struct PresenceDot: View {
    let presence: ChatPresence

    var body: some View {
        Circle()
            .fill(HifisColor.background)
            .frame(width: Self.dot + Self.ring * 2, height: Self.dot + Self.ring * 2)
            .overlay {
                Circle()
                    .fill(color)
                    .frame(width: Self.dot, height: Self.dot)
            }
            .offset(x: 1, y: 1)
    }

    /// 접속 상태 색 — **`Tone` 을 안 쓴다**
    ///
    /// 뜻이 다르다. 방해 금지는 *문제*(`bad`)가 아니라 본인이 켠 상태고, 접속 중도
    /// *잘 됐다*(`good`)가 아니다. 색만 겹칠 뿐이라 그 표에 끌어다 붙이면 다음에
    /// 뜻 색을 손볼 때 여기가 같이 움직인다.
    private var color: Color {
        if presence == ChatPresence.online { return HifisColor.success }
        if presence == ChatPresence.busy { return HifisColor.danger }
        return HifisColor.inkTertiary
    }

    private static let dot: CGFloat = 12
    private static let ring: CGFloat = 2.5
}
