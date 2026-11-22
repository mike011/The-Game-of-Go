import GoEngine
import SwiftUI

/// Draws a Go board with its stones and turns taps into intersections.
struct BoardView: View {
    let game: Game
    var showMoveNumbers = false
    var showLastMove = true
    var showCoordinates = false
    let onTap: (Point) -> Void

    var body: some View {
        GeometryReader { proxy in
            let layout = BoardLayout(side: min(proxy.size.width, proxy.size.height),
                                     lines: game.board.size,
                                     hasCoordinates: showCoordinates)
            Canvas { context, _ in
                drawBoard(in: &context, layout: layout)
                drawStones(in: &context, layout: layout)
            }
            .frame(width: layout.side, height: layout.side)
            .contentShape(.rect)
            .onTapGesture { location in
                if let point = layout.point(nearest: location) {
                    onTap(point)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .aspectRatio(1, contentMode: .fit)
        .clipShape(.rect(cornerRadius: 6))
        .shadow(color: .black.opacity(0.25), radius: 8, y: 4)
        .accessibilityElement(children: .contain)
        .accessibilityLabel("Go board, \(game.board.size) by \(game.board.size)")
        .accessibilityChildren { accessibilityGrid }
    }

    // MARK: - Drawing

    private func drawBoard(in context: inout GraphicsContext, layout: BoardLayout) {
        let bounds = CGRect(x: 0, y: 0, width: layout.side, height: layout.side)
        context.fill(Path(bounds), with: .linearGradient(
            Gradient(colors: [.boardLight, .boardDark]),
            startPoint: .zero, endPoint: CGPoint(x: layout.side, y: layout.side)
        ))

        let lineWidth = max(0.75, layout.spacing / 40)
        var grid = Path()
        for i in 0..<layout.lines {
            grid.move(to: layout.position(of: Point(i, 0)))
            grid.addLine(to: layout.position(of: Point(i, layout.lines - 1)))
            grid.move(to: layout.position(of: Point(0, i)))
            grid.addLine(to: layout.position(of: Point(layout.lines - 1, i)))
        }
        context.stroke(grid, with: .color(.boardLine), lineWidth: lineWidth)

        // Thicker outer edge, as on a real board.
        let first = layout.position(of: Point(0, 0))
        let last = layout.position(of: Point(layout.lines - 1, layout.lines - 1))
        context.stroke(Path(CGRect(x: first.x, y: first.y, width: last.x - first.x, height: last.y - first.y)),
                       with: .color(.boardLine), lineWidth: lineWidth * 2)

        let starRadius = max(2, layout.spacing * 0.09)
        for star in game.board.starPoints {
            let center = layout.position(of: star)
            context.fill(Path(ellipseIn: CGRect(x: center.x - starRadius, y: center.y - starRadius,
                                                 width: starRadius * 2, height: starRadius * 2)),
                         with: .color(.boardLine))
        }

        if showCoordinates {
            let font = Font.system(size: layout.spacing * 0.32, weight: .medium, design: .rounded)
            for i in 0..<layout.lines {
                let column = Point(i, layout.lines - 1).name(onBoardOfSize: layout.lines).dropLast(1)
                let row = String(layout.lines - i)
                let x = layout.position(of: Point(i, 0)).x
                let y = layout.position(of: Point(0, i)).y
                let edge = layout.inset / 2
                for (text, at) in [(String(column), CGPoint(x: x, y: edge)),
                                   (String(column), CGPoint(x: x, y: layout.side - edge)),
                                   (row, CGPoint(x: edge, y: y)),
                                   (row, CGPoint(x: layout.side - edge, y: y))] {
                    context.draw(Text(text).font(font).foregroundStyle(Color.boardLine.opacity(0.8)), at: at)
                }
            }
        }
    }

    private func drawStones(in context: inout GraphicsContext, layout: BoardLayout) {
        let radius = layout.spacing * 0.48
        let numbers = showMoveNumbers ? game.moveNumbers : [:]

        for point in game.board.allPoints {
            guard let stone = game.board[point] else { continue }
            let center = layout.position(of: point)
            let rect = CGRect(x: center.x - radius, y: center.y - radius, width: radius * 2, height: radius * 2)

            // Soft drop shadow.
            context.fill(Path(ellipseIn: rect.offsetBy(dx: radius * 0.08, dy: radius * 0.12)),
                         with: .color(.black.opacity(0.3)))

            let highlight = CGPoint(x: center.x - radius * 0.35, y: center.y - radius * 0.4)
            let colors: [Color] = stone == .black
                ? [Color(white: 0.45), Color(white: 0.08)]
                : [.white, Color(white: 0.78)]
            context.fill(Path(ellipseIn: rect), with: .radialGradient(
                Gradient(colors: colors), center: highlight, startRadius: 0, endRadius: radius * 1.6
            ))

            let markColor: Color = stone == .black ? .white : .black
            if let number = numbers[point] {
                let size = radius * (number >= 100 ? 0.7 : 0.9)
                context.draw(Text("\(number)")
                    .font(.system(size: size, weight: .semibold, design: .rounded))
                    .foregroundStyle(markColor), at: center)
            } else if showLastMove, point == game.lastPlacement {
                let ring = radius * 0.45
                context.stroke(Path(ellipseIn: CGRect(x: center.x - ring, y: center.y - ring,
                                                       width: ring * 2, height: ring * 2)),
                               with: .color(markColor), lineWidth: max(1.5, radius * 0.12))
            }
        }
    }

    // MARK: - Accessibility

    /// One VoiceOver element per intersection so the game is playable without sight.
    private var accessibilityGrid: some View {
        let size = game.board.size
        return Grid(horizontalSpacing: 0, verticalSpacing: 0) {
            ForEach(0..<size, id: \.self) { y in
                GridRow {
                    ForEach(0..<size, id: \.self) { x in
                        let point = Point(x, y)
                        Color.clear
                            .accessibilityElement()
                            .accessibilityLabel(accessibilityLabel(for: point))
                            .accessibilityAddTraits(game.board[point] == nil ? .isButton : [])
                            .accessibilityAction { onTap(point) }
                    }
                }
            }
        }
    }

    private func accessibilityLabel(for point: Point) -> String {
        let name = point.name(onBoardOfSize: game.board.size)
        switch game.board[point] {
        case .black: return "\(name), black stone"
        case .white: return "\(name), white stone"
        case nil: return "\(name), empty"
        }
    }
}

/// Converts between board intersections and view coordinates.
struct BoardLayout {
    let side: CGFloat
    let lines: Int
    /// Distance from the edge of the view to the outermost line.
    let inset: CGFloat
    /// Distance between adjacent lines.
    let spacing: CGFloat

    init(side: CGFloat, lines: Int, hasCoordinates: Bool) {
        self.side = side
        self.lines = lines
        // Leave half a cell around the grid, or a full cell when coordinates are drawn in the margin.
        let margins: CGFloat = hasCoordinates ? 2 : 1
        self.spacing = side / (CGFloat(lines - 1) + margins)
        self.inset = spacing * margins / 2
    }

    func position(of point: Point) -> CGPoint {
        CGPoint(x: inset + CGFloat(point.x) * spacing, y: inset + CGFloat(point.y) * spacing)
    }

    func point(nearest location: CGPoint) -> Point? {
        let x = Int(((location.x - inset) / spacing).rounded())
        let y = Int(((location.y - inset) / spacing).rounded())
        guard (0..<lines).contains(x), (0..<lines).contains(y) else { return nil }
        return Point(x, y)
    }
}

extension Color {
    static let boardLight = Color(red: 0.89, green: 0.74, blue: 0.48)
    static let boardDark = Color(red: 0.80, green: 0.62, blue: 0.36)
    static let boardLine = Color(red: 0.22, green: 0.15, blue: 0.08)
}

#Preview {
    var game = Game()
    for (x, y) in [(2, 2), (6, 6), (6, 2), (2, 6), (4, 4), (4, 5)] {
        _ = try? game.play(at: Point(x, y))
    }
    return BoardView(game: game, showMoveNumbers: true, showCoordinates: true) { _ in }
        .padding()
}
