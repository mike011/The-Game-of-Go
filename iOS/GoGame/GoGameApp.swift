import SwiftUI

@main
struct GoGameApp: App {
    @State private var store = GameStore()

    var body: some Scene {
        WindowGroup {
            GameView()
                .environment(store)
        }
    }
}
