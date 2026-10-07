package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MainViewModel
import com.example.ui.auth.AppLockScreen
import com.example.ui.components.AdBannerView
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.files.FileDetailScreen
import com.example.ui.files.FileScreen
import com.example.ui.files.FileViewModel
import com.example.ui.meetings.MeetingScreen
import com.example.ui.meetings.MeetingViewModel
import com.example.ui.more.MoreScreen
import com.example.ui.navigation.Screen
import com.example.ui.notes.NoteScreen
import com.example.ui.notes.NoteViewModel
import com.example.ui.profile.ProfileSetupScreen
import com.example.ui.tasks.TaskScreen
import com.example.ui.tasks.TaskViewModel
import com.example.ui.reports.ReportScreen
import com.example.ui.reports.ReportViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.GovWorkTheme
import com.example.ui.voicenotes.VoiceNoteScreen
import com.example.ui.voicenotes.VoiceNoteViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as GovWorkApplication
        val container = app.container

        setContent {
            val mainViewModel: MainViewModel = viewModel {
                MainViewModel(container.userProfileRepository)
            }
            val profile by mainViewModel.profile.collectAsStateWithLifecycle()
            val isUnlocked by mainViewModel.isUnlocked.collectAsStateWithLifecycle()

            // Update app language if user configured language
            val locale = when (profile?.languageCode) {
                "gu" -> Locale("gu")
                "hi" -> Locale("hi")
                else -> Locale("en")
            }
            val config = resources.configuration
            config.setLocale(locale)
            val localizedContext = createConfigurationContext(config)

            CompositionLocalProvider(LocalContext provides localizedContext) {
                GovWorkTheme(themeMode = profile?.themeMode ?: "system") {
                    when {
                        // 1. PIN Lock Gate
                        profile?.isAppLockEnabled == true && !profile?.pinCode.isNullOrEmpty() && !isUnlocked -> {
                            AppLockScreen(
                                correctPin = profile!!.pinCode!!,
                                onUnlockSuccess = { mainViewModel.unlockApp() }
                            )
                        }

                        // 2. Onboarding Profile Setup Gate (first launch)
                        profile != null && !profile!!.isProfileConfigured -> {
                            ProfileSetupScreen(
                                currentProfile = profile,
                                onSaveProfile = { name, dept, desig, off, empId ->
                                    mainViewModel.saveProfile(name, dept, desig, off, empId)
                                },
                                onSkip = {
                                    mainViewModel.saveProfile("Officer", "General Office", "Staff", "Office", "")
                                }
                            )
                        }

                        // 3. Main Application Flow
                        else -> {
                            MainAppContent(
                                mainViewModel = mainViewModel,
                                container = container
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    mainViewModel: MainViewModel,
    container: com.example.di.AppContainer
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val profile by mainViewModel.profile.collectAsStateWithLifecycle()

    // ViewModels with container dependencies
    val dashboardViewModel: DashboardViewModel = viewModel {
        DashboardViewModel(container.taskRepository, container.fileRepository, container.meetingRepository)
    }
    val taskViewModel: TaskViewModel = viewModel {
        TaskViewModel(container.taskRepository, context.applicationContext)
    }
    val fileViewModel: FileViewModel = viewModel {
        FileViewModel(container.fileRepository, context.applicationContext)
    }
    val meetingViewModel: MeetingViewModel = viewModel {
        MeetingViewModel(container.meetingRepository, context.applicationContext)
    }
    val noteViewModel: NoteViewModel = viewModel {
        NoteViewModel(container.noteRepository)
    }
    val voiceNoteViewModel: VoiceNoteViewModel = viewModel {
        VoiceNoteViewModel(
            container.voiceNoteRepository,
            container.taskRepository,
            container.noteRepository,
            container.meetingRepository,
            context.applicationContext
        )
    }
    val reportViewModel: ReportViewModel = viewModel {
        ReportViewModel(
            container.userProfileRepository,
            container.taskRepository,
            container.fileRepository,
            container.meetingRepository,
            container.noteRepository,
            context.applicationContext
        )
    }
    val searchViewModel: SearchViewModel = viewModel {
        SearchViewModel(
            container.taskRepository,
            container.fileRepository,
            container.meetingRepository,
            container.noteRepository
        )
    }

    // Top Level Tabs
    val isBottomBarVisible = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Tasks.route,
        Screen.Files.route,
        Screen.Meetings.route,
        Screen.More.route
    )

    val isTopLevelScreen = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Tasks.route,
        Screen.Files.route,
        Screen.Meetings.route,
        Screen.More.route
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (currentRoute) {
                        Screen.Dashboard.route -> stringResource(R.string.app_name)
                        Screen.Tasks.route -> stringResource(R.string.nav_tasks)
                        Screen.Files.route -> stringResource(R.string.nav_files)
                        Screen.Meetings.route -> stringResource(R.string.nav_meetings)
                        Screen.More.route -> stringResource(R.string.nav_more)
                        Screen.Notes.route -> stringResource(R.string.notes_title)
                        Screen.VoiceNotes.route -> stringResource(R.string.voice_notes_title)
                        Screen.Reports.route -> stringResource(R.string.reports_title)
                        Screen.Settings.route -> stringResource(R.string.settings_title)
                        Screen.Search.route -> stringResource(R.string.search_title)
                        Screen.ProfileSetup.route -> stringResource(R.string.profile_title)
                        else -> stringResource(R.string.app_short_name)
                    }
                    Text(text = title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (!isTopLevelScreen) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute != Screen.Search.route) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Search.route) },
                            modifier = Modifier.testTag("topbar_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isBottomBarVisible) {
                androidx.compose.foundation.layout.Column {
                    AdBannerView()
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == Screen.Dashboard.route,
                        onClick = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_dashboard)) },
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Tasks.route,
                        onClick = {
                            navController.navigate(Screen.Tasks.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_tasks)) },
                        modifier = Modifier.testTag("nav_item_tasks")
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Files.route,
                        onClick = {
                            navController.navigate(Screen.Files.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_files)) },
                        modifier = Modifier.testTag("nav_item_files")
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Meetings.route,
                        onClick = {
                            navController.navigate(Screen.Meetings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Event, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_meetings)) },
                        modifier = Modifier.testTag("nav_item_meetings")
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.More.route,
                        onClick = {
                            navController.navigate(Screen.More.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_more)) },
                        modifier = Modifier.testTag("nav_item_more")
                    )
                }
            }
        }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    profile = profile,
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToFiles = { navController.navigate(Screen.Files.route) },
                    onNavigateToMeetings = { navController.navigate(Screen.Meetings.route) },
                    onNavigateToVoiceNotes = { navController.navigate(Screen.VoiceNotes.route) },
                    onFileClick = { fileId -> navController.navigate(Screen.FileDetail.createRoute(fileId)) },
                    onAddTaskQuick = { navController.navigate(Screen.Tasks.route) },
                    onAddFileQuick = { navController.navigate(Screen.Files.route) },
                    onAddMeetingQuick = { navController.navigate(Screen.Meetings.route) }
                )
            }

            composable(Screen.Tasks.route) {
                TaskScreen(viewModel = taskViewModel)
            }

            composable(Screen.Files.route) {
                FileScreen(
                    viewModel = fileViewModel,
                    onFileSelected = { fileId -> navController.navigate(Screen.FileDetail.createRoute(fileId)) }
                )
            }

            composable(
                route = Screen.FileDetail.route,
                arguments = listOf(navArgument("fileId") { type = NavType.LongType })
            ) { backStackEntry ->
                val fileId = backStackEntry.arguments?.getLong("fileId") ?: 0L
                FileDetailScreen(
                    fileId = fileId,
                    viewModel = fileViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            composable(Screen.Meetings.route) {
                MeetingScreen(viewModel = meetingViewModel)
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                    onNavigateToVoiceNotes = { navController.navigate(Screen.VoiceNotes.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Notes.route) {
                NoteScreen(viewModel = noteViewModel)
            }

            composable(Screen.VoiceNotes.route) {
                VoiceNoteScreen(viewModel = voiceNoteViewModel)
            }

            composable(Screen.Reports.route) {
                ReportScreen(viewModel = reportViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    mainViewModel = mainViewModel,
                    profile = profile,
                    onEditProfile = { navController.navigate(Screen.ProfileSetup.route) }
                )
            }

            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(
                    currentProfile = profile,
                    onSaveProfile = { name, dept, desig, off, empId ->
                        mainViewModel.saveProfile(name, dept, desig, off, empId)
                        navController.navigateUp()
                    },
                    onSkip = { navController.navigateUp() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onFileClick = { fileId -> navController.navigate(Screen.FileDetail.createRoute(fileId)) },
                    onTaskClick = { navController.navigate(Screen.Tasks.route) },
                    onMeetingClick = { navController.navigate(Screen.Meetings.route) },
                    onNoteClick = { navController.navigate(Screen.Notes.route) }
                )
            }
        }
    }
}
