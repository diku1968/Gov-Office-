package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Tasks : Screen("tasks")
    data object Files : Screen("files")
    data object FileDetail : Screen("file_detail/{fileId}") {
        fun createRoute(fileId: Long) = "file_detail/$fileId"
    }
    data object Meetings : Screen("meetings")
    data object More : Screen("more")
    data object Notes : Screen("notes")
    data object VoiceNotes : Screen("voice_notes")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
    data object Search : Screen("search")
    data object ProfileSetup : Screen("profile_setup")
}
