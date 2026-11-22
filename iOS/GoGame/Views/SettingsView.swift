import SwiftUI

struct SettingsView: View {
    @Environment(\.dismiss) private var dismiss

    @AppStorage(SettingsKey.showMoveNumbers) private var showMoveNumbers = false
    @AppStorage(SettingsKey.showLastMove) private var showLastMove = true
    @AppStorage(SettingsKey.showCoordinates) private var showCoordinates = false
    @AppStorage(SettingsKey.haptics) private var haptics = true

    var body: some View {
        NavigationStack {
            Form {
                Section("Board") {
                    Toggle("Mark Last Move", isOn: $showLastMove)
                    Toggle("Show Move Numbers", isOn: $showMoveNumbers)
                    Toggle("Show Coordinates", isOn: $showCoordinates)
                }
                Section("Feedback") {
                    Toggle("Haptics", isOn: $haptics)
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}

#Preview {
    SettingsView()
}
