import Foundation
import Testing
@testable import GoEngine

@Suite("Placing stones")
struct PlacementTests {
    @Test func blackMovesFirstAndTurnsAlternate() throws {
        var game = Game()
        #expect(game.toPlay == .black)
        try game.play(at: Point(4, 4))
        #expect(game.board[Point(4, 4)] == .black)
        #expect(game.toPlay == .white)
        try game.play(at: Point(3, 3))
        #expect(game.board[Point(3, 3)] == .white)
        #expect(game.toPlay == .black)
    }

    @Test func cannotPlayOnOccupiedPoint() throws {
        var game = Game()
        try game.play(at: Point(0, 0))
        #expect(throws: MoveError.occupied) { try game.play(at: Point(0, 0)) }
        #expect(game.toPlay == .white, "A rejected move doesn't use up the turn")
    }

    @Test func cannotPlayOffTheBoard() {
        var game = Game()
        #expect(throws: MoveError.outOfBounds) { try game.play(at: Point(9, 0)) }
        #expect(throws: MoveError.outOfBounds) { try game.play(at: Point(-1, 3)) }
    }

    @Test(arguments: Board.supportedSizes)
    func supportsStandardSizes(size: Int) throws {
        var game = Game(size: size)
        try game.play(at: Point(size - 1, size - 1))
        #expect(game.board.size == size)
        #expect(game.board.starPoints.allSatisfy(game.board.contains))
    }
}

@Suite("Capturing")
struct CaptureTests {
    /// Port of the Android `testCaptureSingleStone` scenario: White's W4 is surrounded by B1, B3, B5 and B7.
    @Test func captureSingleStone() throws {
        var game = Game()
        let moves = [(3, 0), (6, 0), (2, 1), (3, 1), (4, 1), (6, 1)]
        for (x, y) in moves { try game.play(at: Point(x, y)) }

        let captured = try game.play(at: Point(3, 2))
        #expect(captured == 1)
        #expect(game.board[Point(3, 1)] == nil)
        #expect(game.captures[.black] == 1)
        #expect(game.captures[.white] == 0)
    }

    @Test func captureGroupInCorner() throws {
        let board = Board(rows: [
            "W W B . . . . . .",
            "B B . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        ])
        // The white pair has already lost every liberty; place it via a position where Black fills the last one.
        var start = board
        start[Point(1, 1)] = nil
        var game = Game(board: start, toPlay: .black)
        let captured = try game.play(at: Point(1, 1))
        #expect(captured == 2)
        #expect(game.board[Point(0, 0)] == nil)
        #expect(game.board[Point(1, 0)] == nil)
    }

    @Test func suicideIsRejected() {
        let board = Board(rows: [
            ". W . . . . . . .",
            "W . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        ])
        var game = Game(board: board, toPlay: .black)
        #expect(throws: MoveError.suicide) { try game.play(at: Point(0, 0)) }
        #expect(!game.canPlay(at: Point(0, 0)))
    }

    @Test func moveWithNoLibertiesIsAllowedWhenItCaptures() throws {
        // Black at (0,0) has no liberties of its own but captures White at (1,0).
        let board = Board(rows: [
            ". W B . . . . . .",
            "W B . . . . . . .",
            "B . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        ])
        var game = Game(board: board, toPlay: .black)
        let captured = try game.play(at: Point(0, 0))
        #expect(captured == 2)
        #expect(game.board[Point(0, 0)] == .black)
    }

    @Test func koCannotBeRetakenImmediately() throws {
        let board = Board(rows: [
            ". B W . . . . . .",
            "B W . W . . . . .",
            ". B W . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        ])
        var game = Game(board: board, toPlay: .black)
        // Black captures the white stone at (1,1).
        #expect(try game.play(at: Point(2, 1)) == 1)
        // White may not immediately recapture at (1,1).
        #expect(throws: MoveError.ko) { try game.play(at: Point(1, 1)) }

        // After a move elsewhere the recapture is legal.
        try game.play(at: Point(8, 8))
        try game.play(at: Point(8, 7))
        #expect(try game.play(at: Point(1, 1)) == 1)
    }
}

@Suite("Ending the game")
struct EndingTests {
    @Test func twoPassesEndAndScoreTheGame() throws {
        var game = Game(size: 9, komi: 6.5)
        try game.play(at: Point(4, 4))
        try game.pass()
        #expect(!game.isOver)
        try game.pass()
        #expect(game.isOver)
        // Black owns the whole board (81 points); White has only komi.
        #expect(game.result == .scored(Score(black: 81, white: 6.5)))
        #expect(game.result?.winner == .black)
        #expect(game.result?.summary == "Black wins by 74.5 points")
        #expect(throws: MoveError.gameOver) { try game.play(at: Point(0, 0)) }
    }

    @Test func passCountResetsAfterAPlacement() throws {
        var game = Game()
        try game.pass()
        try game.play(at: Point(0, 0))
        try game.pass()
        #expect(!game.isOver)
    }

    @Test func areaScoringCountsStonesAndSurroundedTerritory() {
        let board = Board(rows: [
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
            ". . B W . . . . .",
        ])
        let score = Game(board: board, komi: 0.5).score()
        #expect(score.black == 27)
        #expect(score.white == 54.5)
        #expect(score.winner == .white)
    }

    @Test func resignationEndsTheGame() throws {
        var game = Game()
        try game.play(at: Point(4, 4))
        try game.resign()
        #expect(game.result == .resignation(.white))
        #expect(game.result?.summary == "Black wins by resignation")
    }
}

@Suite("Undo and persistence")
struct HistoryTests {
    @Test func undoRestoresBoardCapturesAndTurn() throws {
        var game = Game()
        for (x, y) in [(3, 0), (6, 0), (2, 1), (3, 1), (4, 1), (6, 1), (3, 2)] {
            try game.play(at: Point(x, y))
        }
        #expect(game.captures[.black] == 1)

        game.undo()
        #expect(game.board[Point(3, 1)] == .white)
        #expect(game.board[Point(3, 2)] == nil)
        #expect(game.captures[.black] == 0)
        #expect(game.toPlay == .black)
    }

    @Test func undoReopensAFinishedGame() throws {
        var game = Game()
        try game.pass()
        try game.pass()
        #expect(game.isOver)
        game.undo()
        #expect(!game.isOver)
        #expect(game.toPlay == .white)
    }

    @Test func undoClearsKoHistory() throws {
        var game = Game()
        try game.play(at: Point(0, 0))
        game.undo()
        // The undone position must not count as "seen" for superko.
        try game.play(at: Point(0, 0))
        #expect(game.board[Point(0, 0)] == .black)
    }

    @Test func gameRoundTripsThroughJSON() throws {
        var game = Game(size: 13, komi: 7.5)
        try game.play(at: Point(3, 3))
        try game.play(at: Point(9, 9))
        try game.pass()

        let data = try JSONEncoder().encode(game)
        var decoded = try JSONDecoder().decode(Game.self, from: data)
        #expect(decoded == game)

        decoded.undo()
        #expect(decoded.toPlay == .black)
        #expect(decoded.moves.count == 2)
    }

    @Test func moveNumbersOnlyIncludeStonesStillOnTheBoard() throws {
        var game = Game()
        for (x, y) in [(3, 0), (6, 0), (2, 1), (3, 1), (4, 1), (6, 1), (3, 2)] {
            try game.play(at: Point(x, y))
        }
        let numbers = game.moveNumbers
        #expect(numbers[Point(3, 0)] == 1)
        #expect(numbers[Point(3, 2)] == 7)
        #expect(numbers[Point(3, 1)] == nil, "W4 was captured")
        #expect(game.lastPlacement == Point(3, 2))
    }
}

@Suite("Board")
struct BoardTests {
    @Test func textDiagramUsesGoCoordinates() {
        var board = Board(size: 9)
        board[Point(0, 0)] = .black
        board[Point(8, 8)] = .white
        let lines = board.description.split(separator: "\n")
        #expect(lines.first == "   A B C D E F G H J")
        #expect(lines[1] == " 9 X . . . . . . . .")
        #expect(lines.last == " 1 . . . . . . . . O")
    }

    @Test func groupFindsConnectedStonesAndLiberties() {
        let board = Board(rows: [
            "B B . . . . . . .",
            "B . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        ])
        let group = board.group(at: Point(0, 0))
        #expect(group?.stones.count == 3)
        #expect(group?.liberties == [Point(2, 0), Point(1, 1), Point(0, 2)])
        #expect(board.group(at: Point(5, 5)) == nil)
    }
}
