package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ExamSelectorDialog
import com.example.ui.components.TimerDialog
import com.example.ui.screens.*
import com.example.ui.theme.VidyaSetuTheme
import com.example.viewmodel.AppNavScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VidyaSetuTheme {
                VidyaSetuApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VidyaSetuApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedExams by viewModel.selectedExams.collectAsState()

    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val initialSeconds by viewModel.initialTimerSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsState()

    var showExamSelectorDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    // Android back handling: Return to Home if on secondary screen
    BackHandler(enabled = currentScreen != AppNavScreen.HOME && currentScreen != AppNavScreen.WEB_VIEWER) {
        viewModel.navigateTo(AppNavScreen.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Hide bottom bar on web viewer for full immersive reading
            if (currentScreen != AppNavScreen.WEB_VIEWER) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.HOME,
                        onClick = { viewModel.navigateTo(AppNavScreen.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.COACHING_HUB,
                        onClick = { viewModel.navigateTo(AppNavScreen.COACHING_HUB) },
                        icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Coaching") },
                        label = { Text("Coaching Apps", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.AI_DOUBT_SOLVER,
                        onClick = { viewModel.navigateTo(AppNavScreen.AI_DOUBT_SOLVER) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("AI", fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Guru")
                            }
                        },
                        label = { Text("AI Guru", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.SYLLABUS_TRACKER,
                        onClick = { viewModel.navigateTo(AppNavScreen.SYLLABUS_TRACKER) },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = "Syllabus") },
                        label = { Text("Tracker", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppNavScreen.FORMULAS_DIARY,
                        onClick = { viewModel.navigateTo(AppNavScreen.FORMULAS_DIARY) },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Formulas") },
                        label = { Text("Formulas", fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    AppNavScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenExamSelector = { showExamSelectorDialog = true },
                        onOpenTimer = { showTimerDialog = true }
                    )
                    AppNavScreen.COACHING_HUB -> CoachingHubScreen(
                        viewModel = viewModel
                    )
                    AppNavScreen.AI_DOUBT_SOLVER -> AiDoubtScreen(
                        viewModel = viewModel
                    )
                    AppNavScreen.SYLLABUS_TRACKER -> SyllabusTrackerScreen(
                        viewModel = viewModel,
                        onOpenExamSelector = { showExamSelectorDialog = true }
                    )
                    AppNavScreen.FORMULAS_DIARY -> FormulasScreen(
                        viewModel = viewModel
                    )
                    AppNavScreen.WEB_VIEWER -> WebViewScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Multi-Class / Exam Selector Modal
    if (showExamSelectorDialog) {
        ExamSelectorDialog(
            selectedExams = selectedExams,
            onToggleExam = { viewModel.toggleExam(it) },
            onSelectSchoolAndJeeNeet = { viewModel.selectSchoolAndJeeNeet() },
            onSelectGovtExams = { viewModel.selectGovtExams() },
            onSelectAll = { viewModel.selectAllExams() },
            onDismiss = { showExamSelectorDialog = false }
        )
    }

    // Pomodoro Timer Modal
    if (showTimerDialog) {
        TimerDialog(
            secondsRemaining = timerSeconds,
            initialSeconds = initialSeconds,
            isRunning = isTimerRunning,
            totalMinutesToday = totalStudyMinutes,
            onToggle = { viewModel.toggleTimer() },
            onReset = { viewModel.resetTimer() },
            onSetDuration = { mins -> viewModel.setTimerDuration(mins) },
            onDismiss = { showTimerDialog = false }
        )
    }
}
