import GoEngine
import SwiftUI

struct NewGameView: View {
    @Environment(GameStore.self) private var store
    @Environment(\.dismiss) private var dismiss

    @AppStorage(SettingsKey.defaultBoardSize) private var size = 9
    @AppStorage(SettingsKey.defaultKomi) private var komi = 6.5

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Picker("Board Size", selection: $size) {
                        ForEach(Board.supportedSizes, id: \.self) { size in
                            Text("\(size)×\(size)").tag(size)
                        }
                    }
                    .pickerStyle(.segmented)
                } header: {
                    Text("Board Size")
                } footer: {
                    Text("9×9 is great for learning and quick games; 19×19 is the full traditional board.")
                }

                Section {
                    Stepper(value: $komi, in: 0...9.5, step: 0.5) {
                        LabeledContent("Komi", value: komi.formatted())
                    }
                } footer: {
                    Text("Points given to White to make up for Black moving first.")
                }
            }
            .navigationTitle("New Game")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Start") {
                        store.newGame(size: size, komi: komi)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

#Preview {
    NewGameView()
        .environment(GameStore(saveURL: nil))
}
