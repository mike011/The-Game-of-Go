import Foundation
import GoEngine
import Observation
import OSLog

/// Owns the current game, applies player actions, and saves the game so it can be continued after relaunch.
@MainActor
@Observable
final class GameStore {
    private(set) var game: Game

    /// A short message explaining why the last move was rejected; cleared automatically by the view.
    var rejection: MoveError?

    /// Increments on every rejected move so views can trigger error feedback even when the same error repeats.
    private(set) var rejectionCount = 0

    @ObservationIgnored private let saveURL: URL?
    @ObservationIgnored private let logger = Logger(subsystem: "charland.games.go", category: "GameStore")

    /// - Parameter saveURL: Where to persist the game. Pass `nil` to keep the game in memory only (previews, tests).
    init(saveURL: URL? = GameStore.defaultSaveURL, game: Game? = nil) {
        self.saveURL = saveURL
        if let game {
            self.game = game
        } else if let saveURL, let data = try? Data(contentsOf: saveURL),
                  let saved = try? JSONDecoder().decode(Game.self, from: data) {
            self.game = saved
        } else {
            self.game = Game()
        }
    }

    static var defaultSaveURL: URL {
        URL.applicationSupportDirectory.appending(path: "CurrentGame.json")
    }

    // MARK: - Actions

    func play(at point: Point) {
        perform { (game: inout Game) throws(MoveError) in try game.play(at: point) }
    }

    func pass() {
        perform { (game: inout Game) throws(MoveError) in try game.pass() }
    }

    func resign() {
        perform { (game: inout Game) throws(MoveError) in try game.resign() }
    }

    func undo() {
        game.undo()
        rejection = nil
        save()
    }

    func newGame(size: Int, komi: Double) {
        game = Game(size: size, komi: komi)
        rejection = nil
        save()
    }

    // MARK: - Private

    private func perform(_ action: (inout Game) throws(MoveError) -> Void) {
        do {
            try action(&game)
            rejection = nil
            save()
        } catch {
            rejection = error
            rejectionCount += 1
        }
    }

    private func save() {
        guard let saveURL else { return }
        do {
            try FileManager.default.createDirectory(
                at: saveURL.deletingLastPathComponent(), withIntermediateDirectories: true
            )
            try JSONEncoder().encode(game).write(to: saveURL, options: .atomic)
        } catch {
            logger.error("Couldn't save the game: \(error.localizedDescription)")
        }
    }
}
