import GoEngine
import SwiftUI

/// The main screen: the board, each player's status, and the game controls.
struct GameView: View {
    @Environment(GameStore.self) private var store
    @Environment(\.verticalSizeClass) private var verticalSizeClass

    @AppStorage(SettingsKey.showMoveNumbers) private var showMoveNumbers = false
    @AppStorage(SettingsKey.showLastMove) private var showLastMove = true
    @AppStorage(SettingsKey.showCoordinates) private var showCoordinates = false
    @AppStorage(SettingsKey.haptics) private var haptics = true

    @State private var activeSheet: Sheet?
    @State private var confirmingResign = false

    private enum Sheet: Identifiable {
        case newGame, settings, about
        var id: Self { self }
    }

    var body: some View {
        NavigationStack {
            layout
                .padding()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(.systemGroupedBackground))
                .navigationTitle("Go")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar { toolbar }
                .sheet(item: $activeSheet) { sheet in
                    switch sheet {
                    case .newGame: NewGameView()
                    case .settings: SettingsView()
                    case .about: AboutView()
                    }
                }
                .confirmationDialog("Resign this game?", isPresented: $confirmingResign, titleVisibility: .visible) {
                    Button("Resign as \(store.game.toPlay.name)", role: .destructive) { store.resign() }
                }
        }
        .sensoryFeedback(.impact(weight: .light), trigger: store.game.moves.count) { _, _ in haptics }
        .sensoryFeedback(.error, trigger: store.rejectionCount) { _, _ in haptics }
        .sensoryFeedback(.success, trigger: store.game.isOver) { _, isOver in haptics && isOver }
    }

    /// Board beside the controls in landscape on phones, stacked otherwise.
    @ViewBuilder private var layout: some View {
        if verticalSizeClass == .compact {
            HStack(spacing: 20) {
                board
                VStack(spacing: 16) {
                    status
                    Spacer()
                    controls
                }
                .frame(maxWidth: 320)
            }
        } else {
            VStack(spacing: 16) {
                status
                Spacer(minLength: 0)
                board
                Spacer(minLength: 0)
                controls
            }
            .frame(maxWidth: 700)
        }
    }

    private var board: some View {
        BoardView(game: store.game,
                  showMoveNumbers: showMoveNumbers,
                  showLastMove: showLastMove,
                  showCoordinates: showCoordinates) { point in
            store.play(at: point)
        }
        .overlay(alignment: .bottom) { rejectionBanner }
    }

    // MARK: - Status

    @ViewBuilder private var status: some View {
        VStack(spacing: 10) {
            HStack(spacing: 12) {
                PlayerCard(stone: .black, game: store.game)
                PlayerCard(stone: .white, game: store.game)
            }
            if let result = store.game.result {
                ResultCard(result: result, game: store.game) { activeSheet = .newGame }
            } else if store.game.consecutivePasses == 1 {
                Label("\(store.game.toPlay.opponent.name) passed. Pass again to end the game.",
                      systemImage: "info.circle")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .animation(.default, value: store.game.result)
    }

    @ViewBuilder private var rejectionBanner: some View {
        if let rejection = store.rejection {
            Text(rejection.message)
                .font(.subheadline.weight(.semibold))
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(.regularMaterial, in: .capsule)
                .padding(.bottom, 12)
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .task(id: store.rejectionCount) {
                    try? await Task.sleep(for: .seconds(2))
                    withAnimation { store.rejection = nil }
                }
                .accessibilityAddTraits(.updatesFrequently)
                .onAppear {
                    AccessibilityNotification.Announcement(rejection.message).post()
                }
        }
    }

    // MARK: - Controls

    private var controls: some View {
        HStack(spacing: 12) {
            Button("Undo", systemImage: "arrow.uturn.backward") { withAnimation { store.undo() } }
                .disabled(!store.game.canUndo)
            Button("Pass", systemImage: "hand.raised") { store.pass() }
                .disabled(store.game.isOver)
            Button("Resign", systemImage: "flag") { confirmingResign = true }
                .disabled(store.game.isOver)
        }
        .buttonStyle(.bordered)
        .controlSize(.large)
        .labelStyle(.titleAndIcon)
    }

    @ToolbarContentBuilder private var toolbar: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button("New Game", systemImage: "plus") { activeSheet = .newGame }
        }
        ToolbarItem(placement: .topBarTrailing) {
            Menu("More", systemImage: "ellipsis") {
                ShareLink(item: shareText, preview: SharePreview("Go board")) {
                    Label("Share Board", systemImage: "square.and.arrow.up")
                }
                Button("Settings", systemImage: "gear") { activeSheet = .settings }
                Button("About Go", systemImage: "info.circle") { activeSheet = .about }
            }
        }
    }

    /// The modern replacement for the Android app's "Print Board" debug button.
    private var shareText: String {
        let game = store.game
        let status = game.result?.summary ?? "\(game.toPlay.name) to play"
        return """
        \(game.board)

        Move \(game.moves.count) · \(status)
        Captures: Black \(game.captures[.black, default: 0]), White \(game.captures[.white, default: 0]) · Komi \(game.komi.formatted())
        """
    }
}

/// Shows one player's colour, prisoners, and whether it's their turn.
private struct PlayerCard: View {
    let stone: Stone
    let game: Game

    private var isTurn: Bool { !game.isOver && game.toPlay == stone }

    var body: some View {
        HStack(spacing: 10) {
            Circle()
                .fill(stone == .black
                      ? RadialGradient(colors: [Color(white: 0.45), Color(white: 0.08)],
                                       center: .init(x: 0.3, y: 0.3), startRadius: 0, endRadius: 22)
                      : RadialGradient(colors: [.white, Color(white: 0.78)],
                                       center: .init(x: 0.3, y: 0.3), startRadius: 0, endRadius: 22))
                .overlay(Circle().strokeBorder(Color(.separator), lineWidth: 1))
                .frame(width: 28, height: 28)
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 4) {
                    Text(stone.name).font(.headline)
                    if isTurn {
                        Text("· to play")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.tint)
                    }
                }
                Text("\(game.captures[stone, default: 0]) captured")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .contentTransition(.numericText())
            }
            .lineLimit(1)
            .minimumScaleFactor(0.8)
            Spacer(minLength: 0)
        }
        .padding(12)
        .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: 12))
        .overlay {
            RoundedRectangle(cornerRadius: 12)
                .strokeBorder(isTurn ? AnyShapeStyle(.tint) : AnyShapeStyle(.clear), lineWidth: 2)
        }
        .animation(.snappy, value: isTurn)
        .accessibilityElement(children: .combine)
    }
}

/// Announces the winner and offers a new game.
private struct ResultCard: View {
    let result: GameResult
    let game: Game
    let onNewGame: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Text(result.summary).font(.title3.bold())
            if case .scored(let score) = result {
                Text("Black \(score.black.formatted()) – White \(score.white.formatted()) (incl. \(game.komi.formatted()) komi)")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            Button("New Game", action: onNewGame)
                .buttonStyle(.borderedProminent)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.secondarySystemGroupedBackground), in: .rect(cornerRadius: 12))
        .transition(.scale.combined(with: .opacity))
    }
}

#Preview {
    GameView()
        .environment(GameStore(saveURL: nil))
}
