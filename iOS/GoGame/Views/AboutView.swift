import SwiftUI

struct AboutView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("Go, known as 碁 in Japanese, wéiqí in Chinese and baduk in Korean, is an ancient board game for two players that originated in China more than 2,000 years ago. The game is noted for being rich in strategy despite its relatively simple rules.")
                }
                Section("How to Play") {
                    rule("circle.grid.3x3", "Players take turns placing a stone on an empty intersection. Black goes first.")
                    rule("square.dashed", "A stone's liberties are the empty points next to it. Connected stones share their liberties.")
                    rule("xmark.circle", "A group with no liberties left is captured and removed from the board.")
                    rule("nosign", "You can't play a stone that would have no liberties, unless it captures.")
                    rule("arrow.triangle.2.circlepath", "Ko: you can't make a move that repeats an earlier board position.")
                    rule("flag.checkered", "When both players pass in a row the game ends. Each player scores their stones plus the empty points they surround; White also gets komi.")
                }
                Section {
                    Text("Scoring counts every stone on the board as alive, so capture dead stones before passing.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("About Go")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }

    private func rule(_ symbol: String, _ text: String) -> some View {
        Label {
            Text(text)
        } icon: {
            Image(systemName: symbol).foregroundStyle(.tint)
        }
    }
}

#Preview {
    AboutView()
}
