import SwiftUI
import SharedKit

/// 업무 — **오늘 할 일 그 자체**다
///
/// 하단바 탭이라 열자마자 오늘 점검할 것이 보여야 한다. V2 는 여기가 탭 다섯 개
/// (환경정비·동료 평가·회원 친절도·수업 개수·센터 기여도) 중 하나를 고르는 줄이었는데,
/// **매일 하는 일과 가끔 보는 것이 한 줄에 서 있어서** 매일 하는 사람이 매일 한 번 더 골랐다.
/// V3 는 공통 업무와 내 업무만 둔다 (2026-09-10 대표 결정).
///
/// 제목 아래 두 칸으로 나뉜다. **둘은 도는 방식이 다르다** —
/// 공통 업무는 하루에 여러 번 해서 횟수가 늘고, 내 업무는 한 번씩 체크해서
/// 다 하면 완료·남으면 누락이다. 그래서 한쪽은 칩 격자, 한쪽은 체크 목록이다.
///
/// 안드로이드 `WorkScreen` 과 같은 화면이다.
struct WorkView: View {
    var onSearch: () -> Void = {}
    var onScan: () -> Void = {}
    var onChat: () -> Void = {}
    var onNotification: () -> Void = {}

    /// **지점이 정한 항목표다.** 서버가 붙으면 그 지점 것을 받아 쓴다
    private let items = EnvItem.companion.base
    /// 오늘 몇 번 했는지 — **화면에만 있다.** 서버가 붙으면 그날 것을 받아 채운다
    @State private var counts: [String: Int] = [:]
    @State private var tasks = MyTask.companion.demo
    @State private var mine = false

    var body: some View {
        TabPage(onSearch: onSearch, onScan: onScan, onChat: onChat, onNotification: onNotification) {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    ScreenTitle(WorkBoard.shared.TITLE)
                    ModeSwitch(
                        left: WorkBoard.shared.COMMON,
                        right: WorkBoard.shared.MINE,
                        rightSelected: $mine
                    )
                    .padding(.horizontal, HifisSize.screenEdge)
                    Spacer().frame(height: Self.switchBodyGap)
                    if mine { myTasks } else { checklist }
                }
                .padding(.bottom, 24)
            }

        }
    }

    /// 공통 업무 점검 — 항목이 **2열**로 내려가고, 칩 좌우 −/+ 로 오늘 횟수를 올린다
    ///
    /// **카드를 안 두른다** (V2 2026-09-01 대표 요청). 머리말은 바탕 위에 서고 칩이
    /// 화면 폭을 다 쓴다 — 카드 여백이 좌우로 빠지면서 칩이 그만큼 넓어져 누르기 편하다.
    private var checklist: some View {
        let board = WorkBoard.shared
        let total = board.total(counts: counts.mapValues { KotlinInt(int: Int32($0)) })

        return VStack(alignment: .leading, spacing: 0) {
            // 머리말은 **칩 밖**이다 — 카드가 없으니 이 줄이 곧 묶음의 경계다
            HStack(spacing: 0) {
                Text(board.TODAY_ITEMS)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(HifisColor.ink)
                Spacer(minLength: 0)
                // 누르면 오늘 수행 내역이 열린다 — **아직 그 판을 안 만들었다**
                Button {} label: {
                    Text(board.totalLabel(total: total))
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(HifisColor.brand)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .contentShape(Rectangle())
                }
                .buttonStyle(TapStyle())
                .accessibilityLabel("오늘 내역")
            }
            .padding(.horizontal, 4)

            Spacer().frame(height: 12)

            if items.isEmpty {
                Text(board.EMPTY_ITEMS)
                    .font(HifisFont.body)
                    .foregroundStyle(HifisColor.inkSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, Self.emptyPad)
            } else {
                grid
            }
        }
        .padding(.horizontal, HifisSize.screenEdge)
    }

    private var grid: some View {
        GeometryReader { proxy in
            let chipWidth = (proxy.size.width - Self.gridGap) / CGFloat(Self.columns)
            let fontSize = Self.chipFontSize(items.map(\.name), chipWidth: chipWidth)
            VStack(spacing: Self.gridGap) {
                ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                    HStack(spacing: Self.gridGap) {
                        ForEach(row, id: \.id) { item in
                            CountChip(
                                label: item.name,
                                count: counts[item.id] ?? 0,
                                fontSize: fontSize
                            ) { delta in
                                let next = (counts[item.id] ?? 0) + delta
                                // 0 아래로는 안 내려간다 — 안 한 것을 덜 할 수는 없다
                                if next >= 0 { counts[item.id] = next }
                            }
                        }
                        // 항목이 홀수면 마지막 줄 오른쪽이 빈다 — 남은 칸을 채워
                        // 왼쪽 칩이 두 칸으로 늘어나지 않게 한다
                        ForEach(0..<(Self.columns - row.count), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
        // 격자 높이는 줄 수가 정한다 — `GeometryReader` 는 제 높이를 안 정해서 밖에서 준다
        .frame(height: CGFloat(rows.count) * Self.chipHeight + CGFloat(rows.count - 1) * Self.gridGap)
    }

    /// 두 개씩 끊은 줄들
    private var rows: [[EnvItem]] {
        stride(from: 0, to: items.count, by: Self.columns).map {
            Array(items[$0..<min($0 + Self.columns, items.count)])
        }
    }

    /// 모든 칩이 **함께 쓸** 글자 크기 — 제일 긴 이름이 들어가는 값으로 맞춘다
    ///
    /// 칩마다 알아서 줄이면(`minimumScaleFactor`) 긴 이름(`화장실청소`)만 작아 보인다.
    /// 격자에서 그러면 그 칸만 덜 중요한 것처럼 읽힌다.
    ///
    /// **굵은 글씨로 잰다.** 한 칩은 굵어지는데 보통 글씨로 재 두면 누른 순간 넘친다.
    static func chipFontSize(_ labels: [String], chipWidth: CGFloat) -> CGFloat {
        let available = chipWidth - buttonWidth * 2 - 6
        guard available > 0 else { return chipFontBase }
        var size = chipFontBase
        for label in labels {
            let font = UIFont.systemFont(ofSize: chipFontBase, weight: .bold)
            let width = (label as NSString).size(withAttributes: [.font: font]).width
            if width > available {
                let fit = chipFontBase * available / width
                if fit < size { size = fit }
            }
        }
        return min(max(size, chipFontMin), chipFontBase)
    }

    /// 내 업무 — **하루에 한 번씩 체크**한다
    ///
    /// **면을 안 깐다** (V2 와 같다). 회색 박스를 줄마다 두면 다섯 개짜리 목록이
    /// 회색 덩어리 다섯으로 읽힌다. 줄 사이는 얇은 선이 가른다.
    private var myTasks: some View {
        let board = WorkBoard.shared
        return VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Text(board.MY_TODAY)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(HifisColor.ink)
                Spacer(minLength: 0)
                // 다 했으면 숫자 대신 `완료` 다 — 남은 것이 없다는 말이 숫자보다 빠르다
                Text(board.progressLabel(tasks: tasks))
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(HifisColor.brand)
            }
            .padding(.horizontal, 4)

            Spacer().frame(height: 12)

            if tasks.isEmpty {
                Text(board.EMPTY_TASKS)
                    .font(HifisFont.body)
                    .foregroundStyle(HifisColor.inkSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, Self.emptyPad)
            } else {
                // 진행 막대 — 머리말 숫자와 같은 말을 하지만 **눈이 먼저 닿는다**
                GeometryReader { proxy in
                    ZStack(alignment: .leading) {
                        Capsule().fill(HifisColor.fieldFill)
                        Capsule()
                            .fill(HifisColor.brand)
                            .frame(width: proxy.size.width * CGFloat(board.progress(tasks: tasks)))
                    }
                }
                .frame(height: Self.barHeight)
                Spacer().frame(height: 6)

                ForEach(Array(tasks.enumerated()), id: \.element.id) { index, task in
                    if index > 0 {
                        Rectangle()
                            .fill(HifisColor.line)
                            .frame(height: 1)
                            .padding(.horizontal, 4)
                    }
                    TaskRow(task: task) { check(task) }
                }
            }
        }
        .padding(.horizontal, HifisSize.screenEdge)
    }

    /// 화면을 먼저 바꾼다. 서버가 붙으면 그 뒤에 보낸다
    private func check(_ picked: MyTask) {
        tasks = tasks.map { $0.id == picked.id ? $0.check() : $0 }
    }

    /// 격자 칸 수 — 폰은 둘이다
    fileprivate static let columns = 2
    /// 칩 사이 (가로·세로 같다)
    fileprivate static let gridGap: CGFloat = 10
    /// 칩 높이 — **44 보다 넉넉하게 준다.** 하루에 수십 번 누르는 자리다
    fileprivate static let chipHeight: CGFloat = 56
    /// 칩 모서리 — 카드(24)보다 작다. 격자 안에 여럿이 서는 칸이다
    fileprivate static let chipRadius: CGFloat = 14
    /// −/+ 한 개의 폭 — **48 이다** (V2 2026-09-01 대표 요청, "누르기 편하게")
    fileprivate static let buttonWidth: CGFloat = 48
    /// 그 안의 면과 아이콘
    fileprivate static let buttonPlate: CGFloat = 26
    fileprivate static let buttonIcon: CGFloat = 14
    /// 칩 글자 — 기본과 하한. 이보다 줄면 읽을 수가 없어서 차라리 잘라 낸다
    fileprivate static let chipFontBase: CGFloat = 14
    fileprivate static let chipFontMin: CGFloat = 10
    /// 한 칩의 면·테두리 진하기
    fileprivate static let activeFill: Double = 0.16
    fileprivate static let activeLine: Double = 0.45
    /// 스위치와 본문 사이
    private static let switchBodyGap: CGFloat = 16
    /// 내 업무 진행 막대 두께
    private static let barHeight: CGFloat = 6
    /// 목록이 비었을 때 그 자리의 위아래 여백
    private static let emptyPad: CGFloat = 52
}

/// 횟수 칩 — 왼쪽 −, 가운데 이름, 오른쪽 +
///
/// **한 칩은 브랜드색으로 물든다.** 오늘 뭘 했는지가 격자를 훑을 때 한눈에 갈려야 한다.
///
/// **횟수 숫자는 안 적는다.** V2 도 그랬다 — 칩에는 했는지 여부만 두고 몇 번인지는
/// 머리말의 `총 N회` 와 내역에서 본다. 좁은 칩에 숫자까지 넣으면 이름이 그만큼 줄어든다.
private struct CountChip: View {
    let label: String
    let count: Int
    let fontSize: CGFloat
    let onAdjust: (Int) -> Void

    var body: some View {
        let active = count > 0
        HStack(spacing: 0) {
            AdjustButton(
                icon: "ic_minus",
                label: "\(label) 하나 줄이기",
                // 안 한 항목은 줄일 것이 없다 — 색으로 그렇다고 말한다
                tint: active ? HifisColor.danger : HifisColor.inkTertiary
            ) { onAdjust(-1) }

            // 가운데를 누르면 그 항목 내역이 열린다 — **아직 그 판을 안 만들었다**.
            // −/+ 는 제 자리가 따로 있어서 섞이지 않는다
            Button {} label: {
                Text(label)
                    .font(.system(size: fontSize, weight: active ? .bold : .medium))
                    .foregroundStyle(active ? HifisColor.brand : HifisColor.ink)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .contentShape(Rectangle())
            }
            .buttonStyle(TapStyle())

            AdjustButton(
                icon: "ic_plus",
                label: "\(label) 하나 늘리기",
                tint: HifisColor.brand
            ) { onAdjust(1) }
        }
        .frame(maxWidth: .infinity)
        .frame(height: WorkView.chipHeight)
        .background(
            active ? HifisColor.brand.opacity(WorkView.activeFill) : HifisColor.surface,
            in: RoundedRectangle(cornerRadius: WorkView.chipRadius, style: .continuous)
        )
        .overlay(
            RoundedRectangle(cornerRadius: WorkView.chipRadius, style: .continuous)
                .strokeBorder(
                    active ? HifisColor.brand.opacity(WorkView.activeLine) : HifisColor.line,
                    lineWidth: 1
                )
        )
        // 눌린 뒤 색이 **스며들 듯** 바뀐다 — 하루에 수십 번 누르는 자리라 툭 튀면 피곤하다
        .animation(.easeOut(duration: 0.18), value: active)
    }
}

/// 칩 좌우의 −/+ — **눌림 표시를 안 준다**
///
/// 누른 결과가 칩 색으로 바로 보여서 따로 표시할 것이 없다 (V2 와 같다).
/// 아이콘 뒤에 면을 한 겹 깔아 단추처럼 보이게 한다.
private struct AdjustButton: View {
    let icon: String
    let label: String
    let tint: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(icon)
                .renderingMode(.template)
                .resizable()
                .frame(width: WorkView.buttonIcon, height: WorkView.buttonIcon)
                .foregroundStyle(tint)
                .frame(width: WorkView.buttonPlate, height: WorkView.buttonPlate)
                .background(HifisColor.fieldFill, in: Circle())
                .frame(width: WorkView.buttonWidth, height: WorkView.chipHeight)
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel(label)
    }
}

/// 업무 한 줄 — 왼쪽 동그라미를 누르면 체크된다
///
/// **다 한 줄은 잠근다** (V2 2026-08-20). 체크는 되돌릴 수 없어서 누를 자리가 아니다.
/// 눌리는 것처럼 보이는데 아무 일이 없으면 고장으로 읽힌다.
///
/// **다 한 줄이 도드라지지 않는다.** 파란 면으로 띄우면 눈이 거기 멈추는데,
/// 봐야 하는 건 아직 안 한 줄이다 — 줄이 그어진 채로 조용히 물러난다.
private struct TaskRow: View {
    let task: MyTask
    let onCheck: () -> Void

    var body: some View {
        let checked = task.checked
        Button(action: { if !checked { onCheck() } }) {
            HStack(spacing: 0) {
                Image(checked ? "ic_check_circle_fill" : "ic_circle")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: Self.check, height: Self.check)
                    .foregroundStyle(checked ? HifisColor.brand : HifisColor.inkTertiary)
                Spacer().frame(width: 12)
                VStack(alignment: .leading, spacing: 3) {
                    // 다 한 줄은 **글자를 눕힌다** — 색만 바꾸면 남은 것과 한눈에 안 갈린다
                    Text(task.content)
                        .font(HifisFont.body)
                        .foregroundStyle(checked ? HifisColor.inkTertiary : HifisColor.ink)
                        .strikethrough(checked, color: HifisColor.inkTertiary)
                    // 체크할 때 적어 넣은 값 — 아직 안 한 줄에는 안 붙는다
                    if let value = task.value, !value.isEmpty {
                        Text(value)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(HifisColor.brand)
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(.horizontal, 4)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .disabled(checked)
    }

    /// 내 업무 줄 왼쪽 동그라미
    private static let check: CGFloat = 22
}

