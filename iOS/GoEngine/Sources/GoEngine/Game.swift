/// A move a player can make on their turn.
public enum Move: Hashable, Codable, Sendable {
    case place(Point)
    case pass
    case resign
}

/// Why a move was rejected.
public enum MoveError: Error, Equatable, Sendable {
    case gameOver
    case outOfBounds
    case occupied
    /// The stone would have no liberties and would not capture anything.
    case suicide
    /// The move would repeat an earlier board position (positional superko).
    case ko

    public var message: String {
        switch self {
        case .gameOver: "The game is over"
        case .outOfBounds: "That point is off the board"
        case .occupied: "That point is already taken"
        case .suicide: "Suicide isn't allowed"
        case .ko: "Ko: you can't repeat a previous position"
        }
    }
}

/// How a game ended.
public enum GameResult: Hashable, Codable, Sendable {
    /// Both players passed in a row; the game was scored.
    case scored(Score)
    /// The given player resigned.
    case resignation(Stone)

    public var winner: Stone? {
        switch self {
        case .scored(let score): score.winner
        case .resignation(let loser): loser.opponent
        }
    }

    public var summary: String {
        switch self {
        case .resignation(let loser):
            return "\(loser.opponent.name) wins by resignation"
        case .scored(let score):
            guard let winner = score.winner else { return "Draw" }
            return "\(winner.name) wins by \(score.margin.formatted)"
        }
    }
}

/// The complete state of a game of Go, including history for undo and ko detection.
public struct Game: Codable, Sendable, Equatable {
    /// Everything needed to step back one move.
    private struct Snapshot: Codable, Sendable, Equatable {
        var board: Board
        var toPlay: Stone
        var captures: [Stone: Int]
        var consecutivePasses: Int
        var result: GameResult?
    }

    public private(set) var board: Board
    public private(set) var toPlay: Stone = .black
    /// Number of opponent stones each player has captured (their prisoners).
    public private(set) var captures: [Stone: Int] = [.black: 0, .white: 0]
    public private(set) var moves: [Move] = []
    public private(set) var consecutivePasses = 0
    public private(set) var result: GameResult?
    public let komi: Double

    private var history: [Snapshot] = []
    private var seenPositions: [Board: Int] = [:]

    public init(size: Int = 9, komi: Double = 6.5) {
        self.board = Board(size: size)
        self.komi = komi
        seenPositions[board] = 1
    }

    /// Starts from an existing position, for tests and puzzles.
    public init(board: Board, toPlay: Stone = .black, komi: Double = 6.5) {
        self.board = board
        self.toPlay = toPlay
        self.komi = komi
        seenPositions[board] = 1
    }

    public var isOver: Bool { result != nil }

    /// The most recent stone placed, if the last move was a placement.
    public var lastPlacement: Point? {
        if case .place(let point) = moves.last { return point }
        return nil
    }

    /// For each point that currently holds a stone, the move number (1-based) that placed it.
    public var moveNumbers: [Point: Int] {
        var numbers: [Point: Int] = [:]
        for (index, move) in moves.enumerated() {
            if case .place(let point) = move { numbers[point] = index + 1 }
        }
        return numbers.filter { board[$0.key] != nil }
    }

    /// Checks whether `point` is a legal move for the player to move, without playing it.
    public func canPlay(at point: Point) -> Bool {
        (try? resultingBoard(placing: point)) != nil
    }

    /// Places a stone for the player whose turn it is.
    /// - Returns: The number of opponent stones captured.
    @discardableResult
    public mutating func play(at point: Point) throws(MoveError) -> Int {
        let (newBoard, captured) = try resultingBoard(placing: point)
        saveSnapshot()
        board = newBoard
        captures[toPlay, default: 0] += captured
        consecutivePasses = 0
        moves.append(.place(point))
        seenPositions[board, default: 0] += 1
        toPlay = toPlay.opponent
        return captured
    }

    /// The current player passes. Two passes in a row end and score the game.
    public mutating func pass() throws(MoveError) {
        guard !isOver else { throw .gameOver }
        saveSnapshot()
        moves.append(.pass)
        consecutivePasses += 1
        toPlay = toPlay.opponent
        if consecutivePasses >= 2 {
            result = .scored(score())
        }
    }

    /// The current player resigns, ending the game.
    public mutating func resign() throws(MoveError) {
        guard !isOver else { throw .gameOver }
        saveSnapshot()
        moves.append(.resign)
        result = .resignation(toPlay)
    }

    public var canUndo: Bool { !history.isEmpty }

    /// Takes back the last move (including a pass or resignation).
    public mutating func undo() {
        guard let snapshot = history.popLast() else { return }
        if case .place = moves.last {
            seenPositions[board, default: 1] -= 1
            if seenPositions[board] == 0 { seenPositions[board] = nil }
        }
        moves.removeLast()
        board = snapshot.board
        toPlay = snapshot.toPlay
        captures = snapshot.captures
        consecutivePasses = snapshot.consecutivePasses
        result = snapshot.result
    }

    /// Area (Chinese) scoring: stones on the board plus empty regions bordered by only one colour, plus komi for White.
    /// All stones on the board are treated as alive, so dead stones should be captured before both players pass.
    public func score() -> Score {
        let stones = board.stoneCount
        var territory: [Stone: Int] = [.black: 0, .white: 0]
        var visited: Set<Point> = []

        for start in board.allPoints where board[start] == nil && !visited.contains(start) {
            var region: Set<Point> = [start]
            var borders: Set<Stone> = []
            var frontier = [start]
            while let current = frontier.popLast() {
                for neighbor in board.neighbors(of: current) {
                    if let stone = board[neighbor] {
                        borders.insert(stone)
                    } else if region.insert(neighbor).inserted {
                        frontier.append(neighbor)
                    }
                }
            }
            visited.formUnion(region)
            if borders.count == 1, let owner = borders.first {
                territory[owner, default: 0] += region.count
            }
        }

        return Score(
            black: Double(stones[.black, default: 0] + territory[.black, default: 0]),
            white: Double(stones[.white, default: 0] + territory[.white, default: 0]) + komi
        )
    }

    // MARK: - Private

    private func resultingBoard(placing point: Point) throws(MoveError) -> (Board, Int) {
        guard !isOver else { throw .gameOver }
        guard board.contains(point) else { throw .outOfBounds }
        guard board[point] == nil else { throw .occupied }

        var next = board
        next[point] = toPlay

        var captured = 0
        for neighbor in next.neighbors(of: point) where next[neighbor] == toPlay.opponent {
            if let group = next.group(at: neighbor), group.liberties.isEmpty {
                for stone in group.stones {
                    next[stone] = nil
                }
                captured += group.stones.count
            }
        }

        if captured == 0, let own = next.group(at: point), own.liberties.isEmpty {
            throw .suicide
        }
        if seenPositions[next] != nil {
            throw .ko
        }
        return (next, captured)
    }

    private mutating func saveSnapshot() {
        history.append(Snapshot(
            board: board, toPlay: toPlay, captures: captures,
            consecutivePasses: consecutivePasses, result: result
        ))
    }
}

/// Final points for each player under area scoring.
public struct Score: Hashable, Codable, Sendable {
    public let black: Double
    public let white: Double

    public var winner: Stone? {
        if black == white { return nil }
        return black > white ? .black : .white
    }

    public var margin: Double { abs(black - white) }
}

extension Double {
    /// "6.5 points", "1 point".
    var formatted: String {
        let number = truncatingRemainder(dividingBy: 1) == 0 ? String(Int(self)) : String(self)
        return number + (self == 1 ? " point" : " points")
    }
}
