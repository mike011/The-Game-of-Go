/// A stone colour, which doubles as the identity of a player.
public enum Stone: String, Codable, Sendable, CaseIterable {
    case black
    case white

    public var opponent: Stone { self == .black ? .white : .black }

    public var name: String { self == .black ? "Black" : "White" }
}

/// An intersection on the board. `x` is the column, `y` is the row, both zero-based from the top-left.
public struct Point: Hashable, Codable, Sendable, CustomStringConvertible {
    public let x: Int
    public let y: Int

    public init(_ x: Int, _ y: Int) {
        self.x = x
        self.y = y
    }

    public var description: String { "(\(x), \(y))" }

    /// Column letters used in Go notation; "I" is skipped to avoid confusion with "J".
    static let columnLetters = Array("ABCDEFGHJKLMNOPQRSTUVWXYZ")

    /// The conventional name of this point, e.g. "D4", where rows count up from the bottom.
    public func name(onBoardOfSize size: Int) -> String {
        "\(Point.columnLetters[x])\(size - y)"
    }
}

/// An immutable-by-default snapshot of the stones on a square Go board.
public struct Board: Hashable, Codable, Sendable {
    public static let supportedSizes = [9, 13, 19]

    public let size: Int
    private var cells: [Stone?]

    public init(size: Int = 9) {
        precondition(size >= 2, "A board needs at least 2 lines")
        self.size = size
        self.cells = Array(repeating: nil, count: size * size)
    }

    /// Builds a board from rows of text, where `B`/`X` is black, `W`/`O` is white and anything else is empty.
    /// Handy for tests and previews.
    public init(rows: [String]) {
        self.init(size: rows.count)
        for (y, row) in rows.enumerated() {
            for (x, char) in row.filter({ !$0.isWhitespace }).prefix(size).enumerated() {
                switch char {
                case "B", "X": self[Point(x, y)] = .black
                case "W", "O": self[Point(x, y)] = .white
                default: break
                }
            }
        }
    }

    public func contains(_ point: Point) -> Bool {
        (0..<size).contains(point.x) && (0..<size).contains(point.y)
    }

    public subscript(point: Point) -> Stone? {
        get { cells[point.y * size + point.x] }
        set { cells[point.y * size + point.x] = newValue }
    }

    public var allPoints: [Point] {
        (0..<size).flatMap { y in (0..<size).map { x in Point(x, y) } }
    }

    public func neighbors(of point: Point) -> [Point] {
        [Point(point.x - 1, point.y), Point(point.x + 1, point.y),
         Point(point.x, point.y - 1), Point(point.x, point.y + 1)]
            .filter(contains)
    }

    /// The connected stones of one colour that include `point`, and the empty points touching them.
    public func group(at point: Point) -> (stones: Set<Point>, liberties: Set<Point>)? {
        guard let colour = self[point] else { return nil }
        var stones: Set<Point> = [point]
        var liberties: Set<Point> = []
        var frontier = [point]
        while let current = frontier.popLast() {
            for neighbor in neighbors(of: current) {
                switch self[neighbor] {
                case nil:
                    liberties.insert(neighbor)
                case colour where !stones.contains(neighbor):
                    stones.insert(neighbor)
                    frontier.append(neighbor)
                default:
                    break
                }
            }
        }
        return (stones, liberties)
    }

    public var stoneCount: [Stone: Int] {
        cells.reduce(into: [.black: 0, .white: 0]) { counts, cell in
            if let cell { counts[cell, default: 0] += 1 }
        }
    }

    /// The traditional star points (hoshi) for the common board sizes.
    public var starPoints: [Point] {
        switch size {
        case 9:
            // The four 3-3 points and the centre.
            return [Point(2, 2), Point(6, 2), Point(4, 4), Point(2, 6), Point(6, 6)]
        case 13, 19:
            let lines = size == 13 ? [3, 6, 9] : [3, 9, 15]
            return lines.flatMap { y in lines.map { x in Point(x, y) } }
        default:
            return []
        }
    }
}

extension Board: CustomStringConvertible {
    /// A plain-text diagram of the board, e.g. for sharing or debugging.
    public var description: String {
        let header = "   " + (0..<size).map { String(Point.columnLetters[$0]) }.joined(separator: " ")
        let rows = (0..<size).map { y in
            let label = String(size - y).leftPadded(to: 2)
            let cells = (0..<size).map { x -> String in
                switch self[Point(x, y)] {
                case .black: "X"
                case .white: "O"
                case nil: starPoints.contains(Point(x, y)) ? "+" : "."
                }
            }
            return label + " " + cells.joined(separator: " ")
        }
        return ([header] + rows).joined(separator: "\n")
    }
}

extension String {
    func leftPadded(to width: Int) -> String {
        String(repeating: " ", count: max(0, width - count)) + self
    }
}
