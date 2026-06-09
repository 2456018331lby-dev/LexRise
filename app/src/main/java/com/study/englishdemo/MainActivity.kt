package com.study.englishdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PsychologyAlt
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.study.englishdemo.data.ClozeContextGuide
import com.study.englishdemo.data.ClozeContextGuideKind
import com.study.englishdemo.data.DailyReviewCount
import com.study.englishdemo.data.DailyLoadBrief
import com.study.englishdemo.data.DailyLoadBriefKind
import com.study.englishdemo.data.DailyLoadLane
import com.study.englishdemo.data.DailyStudyRoute
import com.study.englishdemo.data.DailyStudyRouteStep
import com.study.englishdemo.data.DailyStudyRouteTarget
import com.study.englishdemo.data.LearningLoopBrief
import com.study.englishdemo.data.LearningLoopBriefKind
import com.study.englishdemo.data.LearningLoopStep
import com.study.englishdemo.data.Morpheme
import com.study.englishdemo.data.MnemonicBatchBrief
import com.study.englishdemo.data.MnemonicBatchBriefKind
import com.study.englishdemo.data.PracticeMode
import com.study.englishdemo.data.PracticeModeBrief
import com.study.englishdemo.data.PracticeModeBriefKind
import com.study.englishdemo.data.PracticeModeLadderStep
import com.study.englishdemo.data.PracticeSessionCoach
import com.study.englishdemo.data.PracticeSessionCoachKind
import com.study.englishdemo.data.PracticeSessionStats
import com.study.englishdemo.data.ReviewRating
import com.study.englishdemo.data.ReviewExitBrief
import com.study.englishdemo.data.ReviewExitBriefKind
import com.study.englishdemo.data.ReviewQueueBrief
import com.study.englishdemo.data.ReviewQueueBriefKind
import com.study.englishdemo.data.RootAtlasBrief
import com.study.englishdemo.data.RootAtlasBriefKind
import com.study.englishdemo.data.RootGroup
import com.study.englishdemo.data.RootGroupStage
import com.study.englishdemo.data.RootMnemonicBrief
import com.study.englishdemo.data.RootMnemonicBriefKind
import com.study.englishdemo.data.RootWordPracticePlan
import com.study.englishdemo.data.RootWordPracticePlanKind
import com.study.englishdemo.data.RootWordGuide
import com.study.englishdemo.data.RootWordGuideKind
import com.study.englishdemo.data.StudyFocusCue
import com.study.englishdemo.data.StudyFocusKind
import com.study.englishdemo.data.StudyPhase
import com.study.englishdemo.data.StudyRhythmBrief
import com.study.englishdemo.data.StudyRhythmBriefKind
import com.study.englishdemo.data.ToughWordPrescription
import com.study.englishdemo.data.ToughWordPrescriptionKind
import com.study.englishdemo.data.ToughWordsBrief
import com.study.englishdemo.data.VocabularyResultLane
import com.study.englishdemo.data.VocabularyResultTriage
import com.study.englishdemo.data.VocabularyResultTriageKind
import com.study.englishdemo.data.VocabularySearchInsight
import com.study.englishdemo.data.VocabularySearchInsightKind
import com.study.englishdemo.data.VocabularySearchRescuePlan
import com.study.englishdemo.data.WordBatchBrief
import com.study.englishdemo.data.WordBatchBriefKind
import com.study.englishdemo.data.WordBook
import com.study.englishdemo.data.WordEntry
import com.study.englishdemo.data.WordMemoryAnchor
import com.study.englishdemo.data.WordMemoryAnchorKind
import com.study.englishdemo.data.buildLearningLoopBrief
import com.study.englishdemo.data.buildMnemonicBatchBrief
import com.study.englishdemo.data.buildClozeContextGuide
import com.study.englishdemo.data.buildDailyLoadBrief
import com.study.englishdemo.data.buildDailyStudyRoute
import com.study.englishdemo.data.buildPracticeModeBrief
import com.study.englishdemo.data.buildPracticeSessionCoach
import com.study.englishdemo.data.buildReviewExitBrief
import com.study.englishdemo.data.buildReviewQueueBrief
import com.study.englishdemo.data.buildRootAtlasBrief
import com.study.englishdemo.data.buildRootGroupInsight
import com.study.englishdemo.data.buildRootMnemonicBrief
import com.study.englishdemo.data.buildRootWordPracticePlan
import com.study.englishdemo.data.buildRootWordGuide
import com.study.englishdemo.data.buildStudyFocusCue
import com.study.englishdemo.data.buildStudyRhythmBrief
import com.study.englishdemo.data.buildToughWordPrescription
import com.study.englishdemo.data.buildToughWordsBrief
import com.study.englishdemo.data.buildVocabularyResultTriage
import com.study.englishdemo.data.buildVocabularySearchInsight
import com.study.englishdemo.data.buildVocabularySearchRescuePlan
import com.study.englishdemo.data.buildWordBatchBrief
import com.study.englishdemo.data.buildWordMemoryAnchor
import com.study.englishdemo.data.decomposeWord
import com.study.englishdemo.data.matchingWordForms
import com.study.englishdemo.ui.AppUiState
import com.study.englishdemo.ui.AppViewModel
import com.study.englishdemo.ui.AppViewModelFactory
import com.study.englishdemo.ui.LexRiseTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel> {
        AppViewModelFactory((application as EnglishDemoApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexRiseTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                LexRiseApp(
                    uiState = uiState,
                    onRate = viewModel::reviewWord,
                    onPracticeRate = viewModel::reviewPracticeWord,
                    onSpeak = viewModel::speakWord,
                    onDailyTargetChange = viewModel::updateDailyTarget,
                    onReminderSettingsSave = viewModel::updateReminderSettings,
                    onExamPlanSave = viewModel::updateExamPlan,
                    onSelectBook = viewModel::selectBook,
                    onImportBook = viewModel::importBook,
                    onDeleteBook = viewModel::deleteBook,
                    onRenameBook = viewModel::renameBook,
                    onVocabularyQuery = viewModel::updateVocabularyQuery,
                    onVocabularyPhase = viewModel::updateVocabularyPhase,
                    onRootsQuery = viewModel::updateRootsQuery,
                    onMnemonic = viewModel::updateMnemonic,
                    onModeChange = viewModel::setPracticeMode,
                    onResetPracticeStats = viewModel::resetPracticeSessionStats,
                    buildQuiz = viewModel::buildQuiz,
                    buildCloze = viewModel::buildCloze,
                    gradeSpelling = viewModel::gradeSpelling,
                    onRefreshTough = viewModel::loadToughWords,
                )
            }
        }
    }
}

private enum class HomeTab { DASHBOARD, LEARN, REVIEW, ROOTS, TOUGH, VOCAB, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LexRiseApp(
    uiState: AppUiState,
    onRate: (Long, ReviewRating) -> Unit,
    onPracticeRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onDailyTargetChange: (Int) -> Unit,
    onReminderSettingsSave: (Boolean, Int, Int) -> Unit,
    onExamPlanSave: (LocalDate?, Boolean) -> Unit,
    onSelectBook: (Long) -> Unit,
    onImportBook: (android.content.Context, android.net.Uri) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onRenameBook: (Long, String) -> Unit,
    onVocabularyQuery: (String) -> Unit,
    onVocabularyPhase: (StudyPhase?) -> Unit,
    onRootsQuery: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
    onModeChange: (com.study.englishdemo.data.PracticeMode) -> Unit,
    onResetPracticeStats: () -> Unit,
    buildQuiz: suspend (Long) -> com.study.englishdemo.data.QuizQuestion?,
    buildCloze: suspend (Long) -> com.study.englishdemo.data.ClozeQuestion?,
    gradeSpelling: (String, String) -> ReviewRating,
    onRefreshTough: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(HomeTab.DASHBOARD) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            onImportBook(context, uri)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LexRise", fontWeight = FontWeight.SemiBold)
                        Text(
                            "离线优先的考试背词助手",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = !uiState.isImporting,
                        onClick = { importLauncher.launch(arrayOf("text/*", "text/csv")) },
                    ) {
                        Icon(Icons.Rounded.UploadFile, contentDescription = "导入词书")
                    }
                    IconButton(onClick = { currentTab = HomeTab.SETTINGS }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == HomeTab.DASHBOARD,
                    onClick = { currentTab = HomeTab.DASHBOARD },
                    icon = { Icon(Icons.Rounded.Insights, contentDescription = null) },
                    label = { Text("总览") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.LEARN,
                    onClick = { currentTab = HomeTab.LEARN },
                    icon = { Icon(Icons.Rounded.AutoStories, contentDescription = null) },
                    label = { Text("新词") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.REVIEW,
                    onClick = { currentTab = HomeTab.REVIEW },
                    icon = { Icon(Icons.Rounded.PsychologyAlt, contentDescription = null) },
                    label = { Text("复习") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.ROOTS,
                    onClick = { currentTab = HomeTab.ROOTS },
                    icon = { Icon(Icons.Rounded.AccountTree, contentDescription = null) },
                    label = { Text("词根") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.TOUGH,
                    onClick = { currentTab = HomeTab.TOUGH },
                    icon = { Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null) },
                    label = { Text("难词") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.VOCAB,
                    onClick = { currentTab = HomeTab.VOCAB },
                    icon = { Icon(Icons.Rounded.Translate, contentDescription = null) },
                    label = { Text("词汇") },
                )
            }
        },
    ) { padding ->
        if (uiState.loading || uiState.session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("正在整理词书与学习数据…")
            }
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background,
            ) {
                when (currentTab) {
                    HomeTab.DASHBOARD -> DashboardScreen(
                        uiState = uiState,
                        onSelectBook = onSelectBook,
                        onDeleteBook = onDeleteBook,
                        onRenameBook = onRenameBook,
                        onFocusAction = { cue ->
                            currentTab = when (cue.kind) {
                                StudyFocusKind.REVIEW -> HomeTab.REVIEW
                                StudyFocusKind.PACE -> HomeTab.LEARN
                                StudyFocusKind.ROOTS -> HomeTab.ROOTS
                                StudyFocusKind.NEW_WORDS -> HomeTab.LEARN
                                StudyFocusKind.MOMENTUM -> HomeTab.REVIEW
                            }
                        },
                        onRouteAction = { step ->
                            currentTab = when (step.target) {
                                DailyStudyRouteTarget.REVIEW -> HomeTab.REVIEW
                                DailyStudyRouteTarget.TOUGH -> HomeTab.TOUGH
                                DailyStudyRouteTarget.ROOTS -> HomeTab.ROOTS
                                DailyStudyRouteTarget.LEARN -> HomeTab.LEARN
                            }
                        },
                    )
                    HomeTab.LEARN -> LearnScreen(uiState, onRate, onSpeak, onMnemonic)
                    HomeTab.REVIEW -> ReviewScreen(
                        uiState = uiState,
                        onRate = onPracticeRate,
                        onSpeak = onSpeak,
                        onMnemonic = onMnemonic,
                        onModeChange = onModeChange,
                        onResetPracticeStats = onResetPracticeStats,
                        buildQuiz = buildQuiz,
                        buildCloze = buildCloze,
                        gradeSpelling = gradeSpelling,
                    )
                    HomeTab.ROOTS -> RootsScreen(uiState, onSpeak, onRootsQuery)
                    HomeTab.TOUGH -> ToughWordsScreen(uiState, onSpeak, onMnemonic, onRate, onRefreshTough)
                    HomeTab.VOCAB -> VocabularyScreen(uiState, onSpeak, onVocabularyQuery, onVocabularyPhase)
                    HomeTab.SETTINGS -> SettingsScreen(
                        uiState = uiState,
                        onDailyTargetChange = onDailyTargetChange,
                        onExamPlanSave = onExamPlanSave,
                        onReminderSettingsSave = { enabled, hour, minute ->
                            onReminderSettingsSave(enabled, hour, minute)
                            if (
                                enabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    uiState: AppUiState,
    onSelectBook: (Long) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onRenameBook: (Long, String) -> Unit,
    onFocusAction: (StudyFocusCue) -> Unit,
    onRouteAction: (DailyStudyRouteStep) -> Unit,
) {
    val session = requireNotNull(uiState.session)
    val focusCue = remember(session, uiState.rootSnapshot, uiState.pace) {
        buildStudyFocusCue(session, uiState.rootSnapshot, uiState.pace)
    }
    val loadBrief = remember(session, uiState.rootSnapshot, uiState.pace, uiState.toughWords.size) {
        buildDailyLoadBrief(
            session = session,
            rootSnapshot = uiState.rootSnapshot,
            pace = uiState.pace,
            toughWordCount = uiState.toughWords.size,
        )
    }
    val dailyRoute = remember(session, uiState.rootSnapshot, uiState.pace, uiState.toughWords.size) {
        buildDailyStudyRoute(
            session = session,
            rootSnapshot = uiState.rootSnapshot,
            pace = uiState.pace,
            toughWordCount = uiState.toughWords.size,
        )
    }
    val rhythmBrief = remember(uiState.recentReviewCounts, session.overview) {
        buildStudyRhythmBrief(
            counts = uiState.recentReviewCounts,
            overview = session.overview,
        )
    }
    var pendingDelete by remember { mutableStateOf<WordBook?>(null) }
    var pendingRename by remember { mutableStateOf<WordBook?>(null) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(20.dp),
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF24473F), Color(0xFF5F8B77), Color(0xFFD6B179)),
                            ),
                        )
                        .padding(24.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "今天继续把词汇记进长期记忆",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFFFDF9F3),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${uiState.selectedBookTitle} 还剩新词 ${session.overview.newWordsRemaining} 个，待复习 ${session.overview.reviewDueCount} 个，连续学习 ${session.overview.streakDays} 天。",
                            color = Color(0xFFF3EBDD),
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("今日完成", "${session.overview.studiedToday}", Modifier.weight(1f))
                MetricCard("复习债务", "${session.overview.reviewDueCount}", Modifier.weight(1f))
                MetricCard("完成率", "${(session.overview.completionRatio * 100).toInt()}%", Modifier.weight(1f))
            }
        }
        item { DailyLoadBriefCard(loadBrief) }
        item { StudyFocusCueCard(focusCue, onAction = { onFocusAction(focusCue) }) }
        item { DailyStudyRouteCard(dailyRoute, onStepAction = onRouteAction) }
        if (uiState.settings.examDate != null || uiState.pace.isAuto) {
            item { ExamCountdownCard(uiState) }
        }
        if (uiState.rootSnapshot.totalRoots > 0) {
            item { RootCoverageCard(uiState) }
        }
        item {
            SectionTitle("最近 30 日复习")
        }
        item {
            StudyRhythmBriefCard(rhythmBrief)
        }
        item {
            ReviewHeatmap(uiState.recentReviewCounts)
        }
        item {
            StreakBadge(session.overview.streakDays)
        }
        item {
            SectionTitle("词书")
        }
        if (uiState.isImporting) {
            item {
                EmptyStateCard("正在解析词书并建立学习进度…")
            }
        }
        items(uiState.books) { book ->
            val selected = book.id == uiState.selectedBookId
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (book.source != "builtin") {
                            Row {
                                IconButton(onClick = { pendingRename = book }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "重命名词书")
                                }
                                IconButton(onClick = { pendingDelete = book }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "删除词书")
                                }
                            }
                        }
                    }
                    Text(book.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text(book.examTag.uppercase()) })
                        AssistChip(onClick = {}, label = { Text("${book.totalWords} 词") })
                        AssistChip(onClick = {}, label = { Text(book.source) })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (selected) "当前学习词书" else "可切换到这本词书开始新词和复习",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                        Button(
                            enabled = !selected,
                            onClick = { onSelectBook(book.id) },
                        ) {
                            Text(if (selected) "使用中" else "切换")
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("导入格式提示", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("支持 CSV 和 TXT。CSV 建议使用 term, phonetic, definition, translation, example, tags 六列。")
                    Text("TXT 支持 `单词 :: 释义 :: 例句`、Tab 或 ` - ` 分隔；空词条和重复单词会自动过滤。")
                }
            }
        }
    }

    pendingDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除词书") },
            text = { Text("确认删除「${book.title}」吗？已学进度和复习记录会一起清空。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteBook(book.id)
                    pendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }

    pendingRename?.let { book ->
        var draft by remember(book.id) { mutableStateOf(book.title) }
        AlertDialog(
            onDismissRequest = { pendingRename = null },
            title = { Text("重命名词书") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    label = { Text("词书名称") },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRenameBook(book.id, draft)
                    pendingRename = null
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRename = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun DailyLoadBriefCard(brief: DailyLoadBrief) {
    val accent = dailyLoadBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        DailyLoadBriefKind.REVIEW_DEBT -> Icons.Rounded.PsychologyAlt
        DailyLoadBriefKind.TOUGH_REPAIR -> Icons.Rounded.LocalFireDepartment
        DailyLoadBriefKind.ROOT_GAP -> Icons.Rounded.AccountTree
        DailyLoadBriefKind.PACE_PUSH -> Icons.Rounded.EventAvailable
        DailyLoadBriefKind.BALANCED -> Icons.Rounded.Insights
        DailyLoadBriefKind.CLEAR -> Icons.Rounded.Check
    }
    val intensity by animateFloatAsState(
        targetValue = brief.intensity.coerceIn(0f, 1f),
        animationSpec = tween(520),
        label = "dailyLoadIntensity",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.20f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(850f, 360f),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "今日负载简报",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text(
                    brief.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
                LinearProgressIndicator(
                    progress = { intensity },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FocusMetricPill(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    FocusMetricPill(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    brief.lanes.forEach { lane ->
                        DailyLoadLaneRow(lane = lane, accent = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyLoadLaneRow(lane: DailyLoadLane, accent: Color) {
    val weight by animateFloatAsState(
        targetValue = lane.weight.coerceIn(0f, 1f),
        animationSpec = tween(420),
        label = "dailyLoadLane${lane.label}",
    )
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                lane.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
            Text(
                lane.value,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(accent.copy(alpha = 0.10f), RoundedCornerShape(999.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(weight)
                    .height(8.dp)
                    .background(accent, RoundedCornerShape(999.dp)),
            )
        }
    }
}

@Composable
private fun dailyLoadBriefAccent(kind: DailyLoadBriefKind): Color = when (kind) {
    DailyLoadBriefKind.REVIEW_DEBT -> MaterialTheme.colorScheme.primary
    DailyLoadBriefKind.TOUGH_REPAIR -> Color(0xFFB65245)
    DailyLoadBriefKind.ROOT_GAP -> Color(0xFF2D5B52)
    DailyLoadBriefKind.PACE_PUSH -> Color(0xFFC98A3D)
    DailyLoadBriefKind.BALANCED -> MaterialTheme.colorScheme.secondary
    DailyLoadBriefKind.CLEAR -> Color(0xFF6E8B3D)
}

@Composable
private fun ReviewHeatmap(counts: List<DailyReviewCount>) {
    if (counts.isEmpty()) {
        EmptyStateCard("暂无复习记录，今天学几个词开启曲线吧。")
        return
    }
    val maxCount = (counts.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val baseColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val monthFormatter = remember { DateTimeFormatter.ofPattern("M/d") }
    val padded = counts.takeLast(30)
    val rows = 5
    val cols = ((padded.size + rows - 1) / rows)
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("30 日热力图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "共 ${counts.sumOf { it.count }} 词 · 峰值 $maxCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in 0 until cols) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (r in 0 until rows) {
                            val idx = c * rows + r
                            val value = padded.getOrNull(idx)?.count ?: 0
                            val alpha = if (value == 0) 0f else 0.25f + 0.75f * (value.toFloat() / maxCount)
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        if (alpha == 0f) gridColor else baseColor.copy(alpha = alpha),
                                        RoundedCornerShape(4.dp),
                                    ),
                            )
                        }
                    }
                }
            }
            if (padded.size >= 2) {
                Text(
                    "${padded.first().date.format(monthFormatter)}  —  ${padded.last().date.format(monthFormatter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                )
            }
        }
    }
}

@Composable
private fun StudyRhythmBriefCard(brief: StudyRhythmBrief) {
    val accent = studyRhythmAccent(brief.kind)
    val icon = when (brief.kind) {
        StudyRhythmBriefKind.QUIET -> Icons.Rounded.AutoStories
        StudyRhythmBriefKind.RECOVERY -> Icons.Rounded.PsychologyAlt
        StudyRhythmBriefKind.BALANCE -> Icons.Rounded.Insights
        StudyRhythmBriefKind.STEADY -> Icons.Rounded.Check
        StudyRhythmBriefKind.SURGE -> Icons.Rounded.LocalFireDepartment
    }
    val maxCount = (brief.recentCounts.maxOrNull() ?: 0).coerceAtLeast(1)
    val momentum by animateFloatAsState(
        targetValue = brief.momentum.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "studyRhythmMomentum",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.20f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "七日节奏简报",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text(
                    brief.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    brief.recentCounts.forEachIndexed { index, count ->
                        val ratio = count.toFloat() / maxCount.toFloat()
                        val animated by animateFloatAsState(
                            targetValue = ratio.coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 420 + index * 35),
                            label = "studyRhythmBar$index",
                        )
                        val barHeight = (46.dp.value * animated.coerceAtLeast(if (count == 0) 0.08f else 0.16f)).dp
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Text(
                                count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(barHeight)
                                    .background(
                                        if (count == 0) {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        } else {
                                            accent.copy(alpha = 0.35f + 0.65f * animated)
                                        },
                                        RoundedCornerShape(8.dp),
                                    ),
                            )
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { momentum },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StudyRhythmMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    StudyRhythmMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StudyRhythmMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun studyRhythmAccent(kind: StudyRhythmBriefKind): Color = when (kind) {
    StudyRhythmBriefKind.QUIET -> Color(0xFFC98A3D)
    StudyRhythmBriefKind.RECOVERY -> Color(0xFFB65245)
    StudyRhythmBriefKind.BALANCE -> MaterialTheme.colorScheme.secondary
    StudyRhythmBriefKind.STEADY -> Color(0xFF2D5B52)
    StudyRhythmBriefKind.SURGE -> Color(0xFF6E8B3D)
}

@Composable
private fun StreakBadge(streakDays: Int) {
    val (label, color, goal) = when {
        streakDays >= 100 -> Triple("百日坚持 · 超神", Color(0xFFB9860B), 100)
        streakDays >= 30 -> Triple("30 日达人", Color(0xFF2D5B52), 30)
        streakDays >= 7 -> Triple("一周入门", Color(0xFF5F8B77), 7)
        streakDays >= 3 -> Triple("起步 3 天", Color(0xFFC98A3D), 7)
        else -> Triple("从今天开启第 1 天", MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), 3)
    }
    val progress = (streakDays.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animated by animateFloatAsState(progress, tween(400), label = "streak")
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("连续打卡", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "已坚持 $streakDays 天",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animated)
                        .height(12.dp)
                        .background(color, RoundedCornerShape(8.dp)),
                )
            }
            Text(label, color = color, style = MaterialTheme.typography.bodyMedium)
            Text(
                "下一档：${if (streakDays < 7) "7 天" else if (streakDays < 30) "30 天" else "100 天"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun ReviewBarChart(counts: List<DailyReviewCount>) {
    if (counts.isEmpty()) {
        EmptyStateCard("暂无复习记录，今天学几个词开启曲线吧。")
        return
    }
    val maxCount = (counts.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val formatter = remember { DateTimeFormatter.ofPattern("M/d") }
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("每日复习词数", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                counts.takeLast(7).forEach { entry ->
                    val ratio = entry.count.toFloat() / maxCount.toFloat()
                    val animatedRatio by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 450),
                        label = "bar",
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text(
                            text = entry.count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 6.dp)
                                .height((100.dp.value * animatedRatio.coerceAtLeast(0.04f)).dp),
                        ) {
                            drawRoundRectBar(barColor)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = entry.date.format(formatter),
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoundRectBar(color: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset.Zero,
        size = Size(size.width, size.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
    )
}

@Composable
private fun LearnScreen(
    uiState: AppUiState,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
) {
    val words = uiState.session?.recommendedNewWords.orEmpty()
    val batchBrief = remember(words) { buildWordBatchBrief(words) }
    val mnemonicBrief = remember(words) { buildMnemonicBatchBrief(words) }
    val learningLoopBrief = remember(words) { buildLearningLoopBrief(words) }
    WordQueueScreen(
        title = "新词学习",
        subtitle = "当前词书：${uiState.selectedBookTitle}。按词根聚簇出词——同根词连着记，比字母序高效。",
        words = words,
        uiState = uiState,
        batchBrief = batchBrief,
        mnemonicBrief = mnemonicBrief,
        learningLoopBrief = learningLoopBrief,
        onRate = onRate,
        onSpeak = onSpeak,
        onMnemonic = onMnemonic,
    )
}

@Composable
private fun ReviewScreen(
    uiState: AppUiState,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
    onModeChange: (com.study.englishdemo.data.PracticeMode) -> Unit,
    onResetPracticeStats: () -> Unit,
    buildQuiz: suspend (Long) -> com.study.englishdemo.data.QuizQuestion?,
    buildCloze: suspend (Long) -> com.study.englishdemo.data.ClozeQuestion?,
    gradeSpelling: (String, String) -> ReviewRating,
) {
    val words = uiState.session?.dueReviewWords.orEmpty()
    val queueBrief = remember(words, uiState.practiceMode) {
        buildReviewQueueBrief(words = words, mode = uiState.practiceMode)
    }
    val modeBrief = remember(uiState.practiceMode, uiState.practiceStats, words.size) {
        buildPracticeModeBrief(
            mode = uiState.practiceMode,
            stats = uiState.practiceStats,
            remainingDueCount = words.size,
        )
    }
    val exitBrief = remember(uiState.practiceStats, uiState.practiceMode, words.size) {
        buildReviewExitBrief(
            stats = uiState.practiceStats,
            mode = uiState.practiceMode,
            remainingDueCount = words.size,
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        PracticeModeBar(
            current = uiState.practiceMode,
            onChange = onModeChange,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
        PracticeModeBriefCard(
            brief = modeBrief,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
        )
        ReviewQueueBriefCard(
            brief = queueBrief,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
        )
        ReviewExitBriefCard(
            brief = exitBrief,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
        )
        PracticeSessionStatsCard(
            stats = uiState.practiceStats,
            mode = uiState.practiceMode,
            onReset = onResetPracticeStats,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
        )
        if (words.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateCard("当前没有待复习的单词，去『新词』Tab 学几张再回来。")
            }
            return
        }
        when (uiState.practiceMode) {
            com.study.englishdemo.data.PracticeMode.FLIP -> WordQueueScreen(
                title = "复习训练",
                subtitle = "当前词书：${uiState.selectedBookTitle}。先自测回忆，再翻卡核对。",
                words = words,
                uiState = uiState,
                batchBrief = null,
                onRate = onRate,
                onSpeak = onSpeak,
                onMnemonic = onMnemonic,
            )
            com.study.englishdemo.data.PracticeMode.CHOICE -> QuizPracticePager(
                words = words,
                buildQuiz = buildQuiz,
                onRate = onRate,
                onSpeak = onSpeak,
            )
            com.study.englishdemo.data.PracticeMode.CLOZE -> ClozePracticePager(
                words = words,
                buildCloze = buildCloze,
                onRate = onRate,
                onSpeak = onSpeak,
            )
            com.study.englishdemo.data.PracticeMode.SPELL -> SpellPracticePager(
                words = words,
                onRate = onRate,
                onSpeak = onSpeak,
                gradeSpelling = gradeSpelling,
                withAudio = false,
            )
            com.study.englishdemo.data.PracticeMode.DICTATION -> SpellPracticePager(
                words = words,
                onRate = onRate,
                onSpeak = onSpeak,
                gradeSpelling = gradeSpelling,
                withAudio = true,
            )
        }
    }
}

@Composable
private fun PracticeModeBriefCard(brief: PracticeModeBrief, modifier: Modifier = Modifier) {
    val accent = practiceModeBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        PracticeModeBriefKind.WARMUP -> Icons.Rounded.AutoStories
        PracticeModeBriefKind.RECOGNITION -> Icons.Rounded.Quiz
        PracticeModeBriefKind.CONTEXT -> Icons.Rounded.Insights
        PracticeModeBriefKind.ACTIVE_RECALL -> Icons.Rounded.Keyboard
        PracticeModeBriefKind.LISTENING -> Icons.Rounded.Hearing
    }
    val progress by animateFloatAsState(
        targetValue = brief.progress.coerceIn(0f, 1f),
        animationSpec = tween(520),
        label = "practiceModeBriefProgress",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.14f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "练习模式阶梯",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                brief.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PracticeModeBriefMetric(
                        label = brief.primaryLabel,
                        value = brief.primaryValue,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                    )
                    PracticeModeBriefMetric(
                        label = brief.secondaryLabel,
                        value = brief.secondaryValue,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                    )
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(brief.ladder, key = { it.mode.name }) { step ->
                        PracticeModeLadderChip(step = step, accent = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeModeBriefMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun PracticeModeLadderChip(step: PracticeModeLadderStep, accent: Color) {
    val chipColor = if (step.isCurrent) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.70f)
    val textColor = if (step.isCurrent) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
    Surface(
        modifier = Modifier.width(76.dp),
        shape = RoundedCornerShape(18.dp),
        color = chipColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                step.label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Medium,
            )
            LinearProgressIndicator(
                progress = { step.weight.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = accent,
                trackColor = accent.copy(alpha = 0.10f),
            )
            Text(
                step.cue,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (step.isCurrent) 0.72f else 0.48f),
            )
        }
    }
}

@Composable
private fun practiceModeBriefAccent(kind: PracticeModeBriefKind): Color = when (kind) {
    PracticeModeBriefKind.WARMUP -> Color(0xFF7A6742)
    PracticeModeBriefKind.RECOGNITION -> Color(0xFF3F6F8F)
    PracticeModeBriefKind.CONTEXT -> Color(0xFF6E8B3D)
    PracticeModeBriefKind.ACTIVE_RECALL -> Color(0xFFB65245)
    PracticeModeBriefKind.LISTENING -> Color(0xFF2D5B52)
}

@Composable
private fun PracticeSessionStatsCard(
    stats: PracticeSessionStats,
    mode: PracticeMode,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coach = remember(stats, mode) { buildPracticeSessionCoach(stats, mode) }
    val accent = practiceCoachAccent(coach.kind)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                        Text(
                            "本轮练习仪表",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                        Text(
                            if (stats.hasAttempts) "稳定率 ${stats.stabilityPercent}% · 教练会随作答自动改策略"
                            else "还没作答，先完成一题再看本轮策略。",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    TextButton(onClick = onReset, enabled = stats.hasAttempts) {
                        Text("重置")
                    }
                }
                LinearProgressIndicator(
                    progress = { coach.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.14f),
                )
                PracticeCoachPanel(coach = coach, accent = accent)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PracticeStatsMetric("完成", stats.answered.toString(), Modifier.weight(1f))
                    PracticeStatsMetric("稳答", stats.stable.toString(), Modifier.weight(1f))
                    PracticeStatsMetric("再练", stats.needsPractice.toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun practiceCoachAccent(kind: PracticeSessionCoachKind): Color = when (kind) {
    PracticeSessionCoachKind.WARMUP -> Color(0xFFC98A3D)
    PracticeSessionCoachKind.RECOVER -> Color(0xFFB65245)
    PracticeSessionCoachKind.STABILIZE -> MaterialTheme.colorScheme.primary
    PracticeSessionCoachKind.ADVANCE -> Color(0xFF6E8B3D)
}

@Composable
private fun ReviewExitBriefCard(brief: ReviewExitBrief, modifier: Modifier = Modifier) {
    val accent = reviewExitBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        ReviewExitBriefKind.START -> Icons.Rounded.AutoStories
        ReviewExitBriefKind.CONTINUE -> Icons.Rounded.Insights
        ReviewExitBriefKind.REPAIR -> Icons.Rounded.PsychologyAlt
        ReviewExitBriefKind.LEVEL_UP -> Icons.Rounded.LocalFireDepartment
        ReviewExitBriefKind.WRAP_UP -> Icons.Rounded.EventAvailable
        ReviewExitBriefKind.CLEAR -> Icons.Rounded.Check
    }
    val animated by animateFloatAsState(
        targetValue = brief.progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "reviewExitBriefProgress",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .padding(15.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(15.dp), color = accent.copy(alpha = 0.14f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "本轮收口建议",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                brief.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReviewQueueMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    ReviewQueueMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun reviewExitBriefAccent(kind: ReviewExitBriefKind): Color = when (kind) {
    ReviewExitBriefKind.START -> Color(0xFFC98A3D)
    ReviewExitBriefKind.CONTINUE -> MaterialTheme.colorScheme.secondary
    ReviewExitBriefKind.REPAIR -> Color(0xFFB65245)
    ReviewExitBriefKind.LEVEL_UP -> Color(0xFF6E8B3D)
    ReviewExitBriefKind.WRAP_UP -> Color(0xFF3F6F8F)
    ReviewExitBriefKind.CLEAR -> Color(0xFF2D5B52)
}

@Composable
private fun ReviewQueueBriefCard(brief: ReviewQueueBrief, modifier: Modifier = Modifier) {
    val accent = reviewQueueBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        ReviewQueueBriefKind.EMPTY -> Icons.Rounded.Check
        ReviewQueueBriefKind.WARMUP -> Icons.Rounded.AutoStories
        ReviewQueueBriefKind.ROOT_TRACE -> Icons.Rounded.AccountTree
        ReviewQueueBriefKind.CONTEXT -> Icons.Rounded.PsychologyAlt
        ReviewQueueBriefKind.ACTIVE_RECALL -> Icons.Rounded.Keyboard
        ReviewQueueBriefKind.MIXED -> Icons.Rounded.Insights
    }
    val animated by animateFloatAsState(
        targetValue = brief.intensity.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "reviewQueueBriefIntensity",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(15.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "复习队列预案",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                brief.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ReviewQueueMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    ReviewQueueMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                if (brief.focusTerms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items(brief.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewQueueMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun reviewQueueBriefAccent(kind: ReviewQueueBriefKind): Color = when (kind) {
    ReviewQueueBriefKind.EMPTY -> Color(0xFF6E8B3D)
    ReviewQueueBriefKind.WARMUP -> Color(0xFFC98A3D)
    ReviewQueueBriefKind.ROOT_TRACE -> Color(0xFF2D5B52)
    ReviewQueueBriefKind.CONTEXT -> MaterialTheme.colorScheme.primary
    ReviewQueueBriefKind.ACTIVE_RECALL -> Color(0xFF3F6F8F)
    ReviewQueueBriefKind.MIXED -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun PracticeCoachPanel(coach: PracticeSessionCoach, accent: Color) {
    val icon = when (coach.kind) {
        PracticeSessionCoachKind.WARMUP -> Icons.Rounded.AutoStories
        PracticeSessionCoachKind.RECOVER -> Icons.Rounded.PsychologyAlt
        PracticeSessionCoachKind.STABILIZE -> Icons.Rounded.Insights
        PracticeSessionCoachKind.ADVANCE -> Icons.Rounded.LocalFireDepartment
    }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = accent.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.16f)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            "本轮教练",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(coach.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(accent.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(coach.actionLabel, style = MaterialTheme.typography.labelSmall, color = accent)
                }
            }
            Text(
                coach.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PracticeCoachMetric(coach.primaryLabel, coach.primaryValue, accent, Modifier.weight(1f))
                PracticeCoachMetric(coach.secondaryLabel, coach.secondaryValue, accent, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PracticeCoachMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun PracticeStatsMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun VocabularyScreen(
    uiState: AppUiState,
    onSpeak: (String) -> Unit,
    onQuery: (String) -> Unit,
    onPhase: (StudyPhase?) -> Unit,
) {
    val searchInsight = remember(
        uiState.vocabularyQuery,
        uiState.vocabularyResults,
        uiState.vocabularyLoading,
    ) {
        buildVocabularySearchInsight(
            query = uiState.vocabularyQuery,
            results = uiState.vocabularyResults,
        )
    }
    val rescuePlan = remember(
        uiState.vocabularyQuery,
        uiState.vocabularyResults.size,
        uiState.vocabularyPhaseFilter,
        uiState.vocabularyLoading,
    ) {
        buildVocabularySearchRescuePlan(
            query = uiState.vocabularyQuery,
            resultCount = uiState.vocabularyResults.size,
            phaseFilter = uiState.vocabularyPhaseFilter,
        )
    }
    val resultTriage = remember(
        uiState.vocabularyQuery,
        uiState.vocabularyResults,
        uiState.vocabularyPhaseFilter,
    ) {
        buildVocabularyResultTriage(
            query = uiState.vocabularyQuery,
            results = uiState.vocabularyResults,
            phaseFilter = uiState.vocabularyPhaseFilter,
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle("词汇浏览", "搜索「${uiState.selectedBookTitle}」中的单词，按学习阶段筛选。")
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.vocabularyQuery,
                    onValueChange = onQuery,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    label = { Text("搜索单词、派生词、释义或英文解释") },
                    supportingText = { Text("支持词形和拼写容错：clarified / clarifed 都会尝试找 clarify") },
                )
                VocabularySearchStatus(
                    query = uiState.vocabularyQuery,
                    resultCount = uiState.vocabularyResults.size,
                    loading = uiState.vocabularyLoading,
                )
            }
        }
        item {
            VocabularySearchInsightCard(
                insight = searchInsight,
                loading = uiState.vocabularyLoading,
            )
        }
        if (!uiState.vocabularyLoading && rescuePlan.steps.isNotEmpty()) {
            item {
                VocabularySearchRescueCard(plan = rescuePlan)
            }
        }
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { PhaseChip("全部", uiState.vocabularyPhaseFilter == null) { onPhase(null) } }
                item { PhaseChip("新词", uiState.vocabularyPhaseFilter == StudyPhase.NEW) { onPhase(StudyPhase.NEW) } }
                item { PhaseChip("学习中", uiState.vocabularyPhaseFilter == StudyPhase.LEARNING) { onPhase(StudyPhase.LEARNING) } }
                item { PhaseChip("复习", uiState.vocabularyPhaseFilter == StudyPhase.REVIEW) { onPhase(StudyPhase.REVIEW) } }
                item { PhaseChip("掌握", uiState.vocabularyPhaseFilter == StudyPhase.MASTERED) { onPhase(StudyPhase.MASTERED) } }
            }
        }
        if (!uiState.vocabularyLoading && resultTriage.kind != VocabularyResultTriageKind.IDLE) {
            item {
                VocabularyResultTriageCard(triage = resultTriage)
            }
        }
        if (uiState.vocabularyLoading && uiState.vocabularyResults.isEmpty()) {
            item { EmptyStateCard("正在加载词汇…") }
        } else if (uiState.vocabularyResults.isEmpty()) {
            item { EmptyStateCard("没有匹配的单词，换个关键词试试。") }
        } else {
            items(uiState.vocabularyResults, key = { it.id }) { word ->
                VocabularyRow(
                    word = word,
                    query = uiState.vocabularyQuery,
                    onSpeak = onSpeak,
                )
            }
        }
    }
}

@Composable
private fun VocabularySearchStatus(query: String, resultCount: Int, loading: Boolean) {
    val trimmed = query.trim()
    val text = when {
        loading -> "正在检索词书与学习阶段…"
        trimmed.isEmpty() -> "可输入原词、派生词、中文释义或解释；英文词形支持轻微拼写错误。"
        resultCount == 0 -> "未找到结果。试试原词、派生词或中文释义搜索。"
        else -> "找到 $resultCount 个结果；派生词命中和词形相似候选已合并展示。"
    }
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun VocabularySearchInsightCard(insight: VocabularySearchInsight, loading: Boolean) {
    val accent = vocabularySearchInsightAccent(insight.kind)
    val icon = when (insight.kind) {
        VocabularySearchInsightKind.READY -> Icons.Rounded.Search
        VocabularySearchInsightKind.EMPTY_RESULTS -> Icons.Rounded.Close
        VocabularySearchInsightKind.TERM_MATCH -> Icons.Rounded.Check
        VocabularySearchInsightKind.WORD_FORM_MATCH -> Icons.Rounded.Translate
        VocabularySearchInsightKind.ROOT_CLUSTER -> Icons.Rounded.AccountTree
        VocabularySearchInsightKind.MEANING_MATCH -> Icons.Rounded.AutoStories
    }
    val animated by animateFloatAsState(
        targetValue = if (loading) 0.12f else insight.confidence.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "vocabularySearchInsightConfidence",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.14f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "检索洞察",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (loading) "正在刷新结果…" else insight.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (loading) "搜索和阶段筛选正在更新，稍后会重新判断命中类型。" else insight.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            if (loading) "检索中" else insight.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VocabularyInsightMetric(
                        label = insight.primaryLabel,
                        value = if (loading) "…" else insight.primaryValue,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                    )
                    VocabularyInsightMetric(
                        label = insight.secondaryLabel,
                        value = if (loading) "…" else insight.secondaryValue,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!loading && insight.focusTerms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items(insight.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularySearchRescueCard(plan: VocabularySearchRescuePlan, modifier: Modifier = Modifier) {
    val accent = Color(0xFFB65245)
    val animated by animateFloatAsState(
        targetValue = plan.intensity.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "vocabularySearchRescueIntensity",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.14f)) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "空结果救援",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                plan.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            plan.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan.steps.forEachIndexed { index, step ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.14f)) {
                                    Text(
                                        (index + 1).toString(),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = accent,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            step.label,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            step.example,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = accent,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Text(
                                        step.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyResultTriageCard(triage: VocabularyResultTriage, modifier: Modifier = Modifier) {
    val accent = vocabularyResultTriageAccent(triage.kind)
    val icon = when (triage.kind) {
        VocabularyResultTriageKind.IDLE -> Icons.Rounded.Search
        VocabularyResultTriageKind.TERM_SWEEP -> Icons.Rounded.Check
        VocabularyResultTriageKind.WORD_FORM -> Icons.Rounded.Translate
        VocabularyResultTriageKind.ROOT_LANE -> Icons.Rounded.AccountTree
        VocabularyResultTriageKind.PHASE_FOCUS -> Icons.Rounded.EventAvailable
        VocabularyResultTriageKind.MEANING_SCAN -> Icons.Rounded.AutoStories
        VocabularyResultTriageKind.MIXED -> Icons.Rounded.Insights
    }
    val animated by animateFloatAsState(
        targetValue = triage.intensity.coerceIn(0f, 1f),
        animationSpec = tween(520),
        label = "vocabularyResultTriageIntensity",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
                            MaterialTheme.colorScheme.surface,
                            accent.copy(alpha = 0.16f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 420f),
                    ),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.14f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "结果分诊",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(triage.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                triage.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            triage.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VocabularyInsightMetric(
                        label = triage.primaryLabel,
                        value = triage.primaryValue,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                    )
                    VocabularyInsightMetric(
                        label = triage.secondaryLabel,
                        value = triage.secondaryValue,
                        accent = accent,
                        modifier = Modifier.weight(1f),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    triage.lanes.forEach { lane ->
                        VocabularyResultLaneRow(lane = lane, accent = accent)
                    }
                }
                if (triage.focusTerms.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            "优先查看",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            items(triage.focusTerms, key = { it }) { term ->
                                Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                    Text(
                                        term,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyResultLaneRow(lane: VocabularyResultLane, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                lane.label,
                modifier = Modifier.width(42.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                fontWeight = FontWeight.Medium,
            )
            LinearProgressIndicator(
                progress = { lane.weight.coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp),
                color = accent,
                trackColor = accent.copy(alpha = 0.10f),
            )
            Text(
                lane.value,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun VocabularyInsightMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun vocabularySearchInsightAccent(kind: VocabularySearchInsightKind): Color = when (kind) {
    VocabularySearchInsightKind.READY -> MaterialTheme.colorScheme.primary
    VocabularySearchInsightKind.EMPTY_RESULTS -> MaterialTheme.colorScheme.error
    VocabularySearchInsightKind.TERM_MATCH -> Color(0xFF2D5B52)
    VocabularySearchInsightKind.WORD_FORM_MATCH -> Color(0xFF3F6F8F)
    VocabularySearchInsightKind.ROOT_CLUSTER -> Color(0xFF6E8B3D)
    VocabularySearchInsightKind.MEANING_MATCH -> Color(0xFFC98A3D)
}

@Composable
private fun vocabularyResultTriageAccent(kind: VocabularyResultTriageKind): Color = when (kind) {
    VocabularyResultTriageKind.IDLE -> MaterialTheme.colorScheme.primary
    VocabularyResultTriageKind.TERM_SWEEP -> Color(0xFF2D5B52)
    VocabularyResultTriageKind.WORD_FORM -> Color(0xFF3F6F8F)
    VocabularyResultTriageKind.ROOT_LANE -> Color(0xFF6E8B3D)
    VocabularyResultTriageKind.PHASE_FOCUS -> Color(0xFF7A6742)
    VocabularyResultTriageKind.MEANING_SCAN -> Color(0xFFC98A3D)
    VocabularyResultTriageKind.MIXED -> Color(0xFF54616D)
}

@Composable
private fun PhaseChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun VocabularyRow(word: WordEntry, query: String, onSpeak: (String) -> Unit) {
    val matchedForms = matchingWordForms(
        query = query,
        term = word.term,
        variants = word.derivatives,
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(word.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (word.phonetic.isNotBlank()) {
                        Text(word.phonetic, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PhaseTag(word.progress?.phase)
                    IconButton(onClick = { onSpeak(word.term) }) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = "播放发音")
                    }
                }
            }
            Text(word.translation, style = MaterialTheme.typography.bodyLarge)
            if (matchedForms.isNotEmpty()) {
                VocabularyMatchRail(matchedForms)
            }
            if (word.definition.isNotBlank()) {
                Text(
                    word.definition,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            val chips = buildList {
                if (word.rootKey.isNotBlank()) add("词根 ${word.rootKey}")
                if (word.pos.isNotBlank()) add(word.pos)
                addAll(word.tags.take(2))
            }
            if (chips.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(chips, key = { it }) { label ->
                        AssistChip(onClick = {}, label = { Text(label) })
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyMatchRail(forms: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "词形雷达",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(forms, key = { it }) { form ->
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        "命中 $form",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseTag(phase: StudyPhase?) {
    val (label, color) = when (phase) {
        StudyPhase.NEW -> "新词" to Color(0xFFC98A3D)
        StudyPhase.LEARNING -> "学习中" to Color(0xFF4A7DA8)
        StudyPhase.REVIEW -> "复习" to Color(0xFF2D5B52)
        StudyPhase.MASTERED -> "掌握" to Color(0xFF6E8B3D)
        null -> "未建档" to Color(0xFF777777)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: AppUiState,
    onDailyTargetChange: (Int) -> Unit,
    onExamPlanSave: (LocalDate?, Boolean) -> Unit,
    onReminderSettingsSave: (Boolean, Int, Int) -> Unit,
) {
    var sliderValue by remember(uiState.settings.dailyNewWordTarget) {
        mutableFloatStateOf(uiState.settings.dailyNewWordTarget.toFloat())
    }
    var reminderEnabled by remember(uiState.settings.reviewReminderEnabled) {
        mutableStateOf(uiState.settings.reviewReminderEnabled)
    }
    var reminderHour by remember(uiState.settings.reminderHour) {
        mutableFloatStateOf(uiState.settings.reminderHour.toFloat())
    }
    var reminderMinute by remember(uiState.settings.reminderMinute) {
        mutableFloatStateOf(uiState.settings.reminderMinute.toFloat())
    }
    val session = requireNotNull(uiState.session)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionTitle("计划与提醒")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("当前词书", uiState.selectedBookTitle, Modifier.weight(1f))
                MetricCard("连续天数", "${session.overview.streakDays}", Modifier.weight(1f))
            }
        }
        item {
            ExamPlanCard(
                settings = uiState.settings,
                pace = uiState.pace,
                onSave = onExamPlanSave,
            )
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("手动基线（每日新词）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${sliderValue.toInt()} 个", style = MaterialTheme.typography.headlineMedium)
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 5f..40f,
                        steps = 6,
                    )
                    if (uiState.settings.autoPaceEnabled && uiState.pace.isAuto) {
                        Text(
                            "自动配速开启，实际使用 ${uiState.pace.target} 个/天。关闭后回到此基线。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                    Button(onClick = { onDailyTargetChange(sliderValue.toInt()) }) {
                        Text("保存基线")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("复习提醒", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }
                    Text(
                        if (reminderEnabled) {
                            "每天 ${reminderHour.toInt().toString().padStart(2, '0')}:${reminderMinute.toInt().toString().padStart(2, '0')} 检查待复习单词。"
                        } else {
                            "已关闭提醒。"
                        },
                    )
                    Text("提醒小时", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                    Slider(
                        value = reminderHour,
                        onValueChange = { reminderHour = it },
                        valueRange = 0f..23f,
                        steps = 22,
                        enabled = reminderEnabled,
                    )
                    Text("提醒分钟", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
                    Slider(
                        value = reminderMinute,
                        onValueChange = { reminderMinute = ((it / 5f).toInt() * 5).toFloat() },
                        valueRange = 0f..55f,
                        steps = 10,
                        enabled = reminderEnabled,
                    )
                    Button(
                        onClick = {
                            onReminderSettingsSave(
                                reminderEnabled,
                                reminderHour.toInt(),
                                reminderMinute.toInt(),
                            )
                        },
                    ) {
                        Text("保存提醒")
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("产品方向", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("1. 离线优先：数据、词书、复习记录都保存在本地。")
                    Text("2. 双模式学习：新词闯关 + 记忆曲线复习并重。")
                    Text("3. 音频架构已接上系统 TTS，后续可以切到更高质量发音资源。")
                    Text("4. 发布路径以 GitHub Releases 附件 APK 为主，先保证可下载、可安装、可复现。")
                }
            }
        }
    }
}

@Composable
private fun WordQueueScreen(
    title: String,
    subtitle: String,
    words: List<WordEntry>,
    uiState: AppUiState,
    batchBrief: WordBatchBrief? = null,
    mnemonicBrief: MnemonicBatchBrief? = null,
    learningLoopBrief: LearningLoopBrief? = null,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            SectionTitle(title, subtitle)
        }
        batchBrief?.let { brief ->
            item {
                WordBatchBriefCard(brief)
            }
        }
        mnemonicBrief?.let { brief ->
            item {
                MnemonicBatchBriefCard(brief)
            }
        }
        learningLoopBrief?.let { brief ->
            item {
                LearningLoopBriefCard(brief)
            }
        }
        if (words.isEmpty()) {
            item {
                EmptyStateCard("当前没有待处理单词，休息一下也很好。")
            }
        } else {
            items(words, key = { it.id }) { word ->
                FlipWordCard(
                    word = word,
                    rootRef = uiState.rootRefByKey[word.rootKey],
                    siblings = uiState.siblingsByWordId[word.id].orEmpty(),
                    isAnchor = word.id in uiState.anchorWordIds,
                    onRate = onRate,
                    onSpeak = onSpeak,
                    onMnemonic = onMnemonic,
                )
            }
        }
    }
}

@Composable
private fun WordBatchBriefCard(brief: WordBatchBrief) {
    val accent = wordBatchBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        WordBatchBriefKind.ROOTS -> Icons.Rounded.AccountTree
        WordBatchBriefKind.WORD_FORMS -> Icons.Rounded.Translate
        WordBatchBriefKind.CONTEXT -> Icons.Rounded.AutoStories
        WordBatchBriefKind.MIXED -> Icons.Rounded.Insights
        WordBatchBriefKind.EMPTY -> Icons.Rounded.Check
    }
    val animated by animateFloatAsState(
        targetValue = brief.intensity.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "wordBatchBriefIntensity",
    )
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.20f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(21.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "本批新词策略",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                brief.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BatchBriefMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    BatchBriefMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                if (brief.focusTerms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items(brief.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MnemonicBatchBriefCard(brief: MnemonicBatchBrief) {
    val accent = mnemonicBatchBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        MnemonicBatchBriefKind.READY -> Icons.Rounded.Star
        MnemonicBatchBriefKind.SEED_GAP -> Icons.Rounded.Edit
        MnemonicBatchBriefKind.ROOT_BRIDGE -> Icons.Rounded.AccountTree
        MnemonicBatchBriefKind.QUICK_START -> Icons.Rounded.PsychologyAlt
        MnemonicBatchBriefKind.EMPTY -> Icons.Rounded.Check
    }
    val animated by animateFloatAsState(
        targetValue = brief.coverage.coerceIn(0f, 1f),
        animationSpec = tween(520),
        label = "mnemonicBatchBriefCoverage",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                            accent.copy(alpha = 0.07f),
                        ),
                    ),
                )
                .padding(17.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.15f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .size(21.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "巧记覆盖简报",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                brief.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BatchBriefMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    BatchBriefMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                if (brief.focusTerms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items(brief.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningLoopBriefCard(brief: LearningLoopBrief) {
    val accent = learningLoopBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        LearningLoopBriefKind.EMPTY -> Icons.Rounded.Check
        LearningLoopBriefKind.ROOT_LOOP -> Icons.Rounded.AccountTree
        LearningLoopBriefKind.MEMORY_LOOP -> Icons.Rounded.Star
        LearningLoopBriefKind.CONTEXT_LOOP -> Icons.Rounded.AutoStories
        LearningLoopBriefKind.BATCH_LOOP -> Icons.Rounded.Insights
        LearningLoopBriefKind.QUICK_LOOP -> Icons.Rounded.PsychologyAlt
    }
    val animated by animateFloatAsState(
        targetValue = brief.progress.coerceIn(0f, 1f),
        animationSpec = tween(560),
        label = "learningLoopBriefProgress",
    )
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(780f, 320f),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(17.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "新词闭环计划",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                brief.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BatchBriefMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    BatchBriefMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    brief.steps.forEachIndexed { index, step ->
                        LearningLoopStepRow(
                            step = step,
                            accent = accent,
                            isLast = index == brief.steps.lastIndex,
                        )
                    }
                }
                if (brief.focusTerms.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        items(brief.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningLoopStepRow(step: LearningLoopStep, accent: Color, isLast: Boolean) {
    val weight by animateFloatAsState(
        targetValue = step.weight.coerceIn(0f, 1f),
        animationSpec = tween(430),
        label = "learningLoopStep${step.label}",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.16f)) {
                Text(
                    step.label,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(22.dp)
                        .background(accent.copy(alpha = 0.18f), RoundedCornerShape(999.dp)),
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(step.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(6.dp)
                        .background(accent.copy(alpha = 0.10f), RoundedCornerShape(999.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(weight)
                            .height(6.dp)
                            .background(accent, RoundedCornerShape(999.dp)),
                    )
                }
            }
            Text(
                step.cue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
            )
        }
    }
}

@Composable
private fun BatchBriefMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun mnemonicBatchBriefAccent(kind: MnemonicBatchBriefKind): Color = when (kind) {
    MnemonicBatchBriefKind.READY -> Color(0xFFC98A3D)
    MnemonicBatchBriefKind.SEED_GAP -> Color(0xFF9A6B3A)
    MnemonicBatchBriefKind.ROOT_BRIDGE -> Color(0xFF2D5B52)
    MnemonicBatchBriefKind.QUICK_START -> Color(0xFF3F6F8F)
    MnemonicBatchBriefKind.EMPTY -> Color(0xFF6E8B3D)
}

@Composable
private fun learningLoopBriefAccent(kind: LearningLoopBriefKind): Color = when (kind) {
    LearningLoopBriefKind.EMPTY -> Color(0xFF6E8B3D)
    LearningLoopBriefKind.ROOT_LOOP -> Color(0xFF2D5B52)
    LearningLoopBriefKind.MEMORY_LOOP -> Color(0xFFC98A3D)
    LearningLoopBriefKind.CONTEXT_LOOP -> MaterialTheme.colorScheme.primary
    LearningLoopBriefKind.BATCH_LOOP -> Color(0xFF9A6B3A)
    LearningLoopBriefKind.QUICK_LOOP -> Color(0xFF3F6F8F)
}

@Composable
private fun wordBatchBriefAccent(kind: WordBatchBriefKind): Color = when (kind) {
    WordBatchBriefKind.ROOTS -> Color(0xFF2D5B52)
    WordBatchBriefKind.WORD_FORMS -> Color(0xFF3F6F8F)
    WordBatchBriefKind.CONTEXT -> MaterialTheme.colorScheme.primary
    WordBatchBriefKind.MIXED -> Color(0xFFC98A3D)
    WordBatchBriefKind.EMPTY -> Color(0xFF6E8B3D)
}

@Composable
private fun FlipWordCard(
    word: WordEntry,
    rootRef: com.study.englishdemo.data.RootReference?,
    siblings: List<WordEntry>,
    isAnchor: Boolean,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
) {
    var revealed by remember(word.id) { mutableStateOf(false) }
    var mnemonicEditorOpen by remember(word.id) { mutableStateOf(false) }
    var mnemonicDraft by remember(word.id, word.mnemonic) { mutableStateOf(word.mnemonic) }
    var selectedSibling by remember(word.id) { mutableStateOf<WordEntry?>(null) }
    val memoryAnchor = remember(word, rootRef, siblings.size) {
        buildWordMemoryAnchor(word = word, rootRef = rootRef, siblingCount = siblings.size)
    }
    selectedSibling?.let { selected ->
        RootWordPreviewSheet(
            word = selected,
            rootMeanings = rootRef?.meanings.orEmpty(),
            familyWords = siblings,
            onSpeak = onSpeak,
            onDismiss = { selectedSibling = null },
        )
    }
    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { revealed = !revealed },
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(word.term, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        word.phonetic.ifBlank { "点击卡片查看释义" },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isAnchor) AnchorBadge()
                    PhaseTag(word.progress?.phase)
                    IconButton(onClick = { onSpeak(word.term) }) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = "播放发音")
                    }
                }
            }
            WordMemoryAnchorPanel(memoryAnchor)
            if (word.rootKey.isNotBlank()) {
                MorphemeRow(term = word.term, rootKey = word.rootKey, rootRef = rootRef)
            }
            AnimatedVisibility(visible = revealed) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(word.translation, style = MaterialTheme.typography.titleMedium)
                    if (word.pos.isNotBlank()) {
                        Text(
                            "词性：${word.pos}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        )
                    }
                    if (word.definition.isNotBlank()) {
                        Text(word.definition, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    if (word.example.isNotBlank()) {
                        Text(
                            "例句：${word.example}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    rootRef?.let { r ->
                        if (r.examples.isNotEmpty()) {
                            Text(
                                "同根例词：${r.examples.take(5).joinToString("、")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                    if (siblings.isNotEmpty()) {
                        Text(
                            "同根词",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            siblings.take(4).forEach { sib ->
                                SiblingChip(sib = sib, onClick = { selectedSibling = it })
                            }
                        }
                    }
                    if (word.derivatives.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            word.derivatives.take(6).forEach { d ->
                                AssistChip(onClick = {}, label = { Text(d) })
                            }
                        }
                    }
                    if (word.mnemonic.isNotBlank() && !mnemonicEditorOpen) {
                        MnemonicSignalCard(word.mnemonic)
                    }
                    if (mnemonicEditorOpen) {
                        OutlinedTextField(
                            value = mnemonicDraft,
                            onValueChange = { mnemonicDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("写一句帮自己记住的话") },
                            singleLine = false,
                            minLines = 2,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                onMnemonic(word.id, mnemonicDraft)
                                mnemonicEditorOpen = false
                            }) { Text("保存") }
                            TextButton(onClick = { mnemonicEditorOpen = false }) { Text("取消") }
                        }
                    } else {
                        TextButton(onClick = { mnemonicEditorOpen = true }) {
                            Text(if (word.mnemonic.isBlank()) "+ 添加巧记" else "编辑巧记")
                        }
                    }
                    if (word.tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            word.tags.take(3).forEach { tag ->
                                AssistChip(onClick = {}, label = { Text(tag) })
                            }
                        }
                    }
                }
            }
            if (!revealed) {
                Text(
                    "点击卡片翻出释义，然后选择下方评分。",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RatingButton("重来", ReviewRating.AGAIN, word.id, enabled = revealed, onRate = onRate)
                RatingButton("困难", ReviewRating.HARD, word.id, enabled = revealed, onRate = onRate)
                RatingButton("掌握", ReviewRating.GOOD, word.id, enabled = revealed, onRate = onRate)
                RatingButton("秒会", ReviewRating.EASY, word.id, enabled = revealed, onRate = onRate)
            }
        }
    }
}

@Composable
private fun MnemonicSignalCard(mnemonic: String, modifier: Modifier = Modifier) {
    val accent = Color(0xFFC98A3D)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.14f)) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .padding(7.dp)
                        .size(18.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "巧记线索",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    mnemonic,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun WordMemoryAnchorPanel(anchor: WordMemoryAnchor) {
    val accent = wordMemoryAnchorAccent(anchor.kind)
    val icon = wordMemoryAnchorIcon(anchor.kind)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                    RoundedCornerShape(24.dp),
                )
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Surface(shape = RoundedCornerShape(15.dp), color = accent.copy(alpha = 0.18f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(anchor.badgeLabel, style = MaterialTheme.typography.labelMedium, color = accent)
                            Text(anchor.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            anchor.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text(
                    anchor.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MemoryAnchorMetric(anchor.primaryLabel, anchor.primaryValue, accent, Modifier.weight(1f))
                    MemoryAnchorMetric(anchor.secondaryLabel, anchor.secondaryValue, accent, Modifier.weight(1f))
                }
                if (anchor.focusTerms.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(anchor.focusTerms, key = { it }) { term ->
                            AssistChip(onClick = {}, label = { Text(term) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryAnchorMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.09f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun wordMemoryAnchorAccent(kind: WordMemoryAnchorKind): Color = when (kind) {
    WordMemoryAnchorKind.ROOT_FAMILY -> Color(0xFF2D5B52)
    WordMemoryAnchorKind.WORD_FORMS -> MaterialTheme.colorScheme.primary
    WordMemoryAnchorKind.CONTEXT -> Color(0xFFC98A3D)
    WordMemoryAnchorKind.SOLO -> Color(0xFF6E8B3D)
}

private fun wordMemoryAnchorIcon(kind: WordMemoryAnchorKind) = when (kind) {
    WordMemoryAnchorKind.ROOT_FAMILY -> Icons.Rounded.AccountTree
    WordMemoryAnchorKind.WORD_FORMS -> Icons.Rounded.Translate
    WordMemoryAnchorKind.CONTEXT -> Icons.Rounded.AutoStories
    WordMemoryAnchorKind.SOLO -> Icons.Rounded.PsychologyAlt
}

@Composable
private fun RatingButton(
    text: String,
    rating: ReviewRating,
    wordId: Long,
    enabled: Boolean,
    onRate: (Long, ReviewRating) -> Unit,
) {
    Button(onClick = { onRate(wordId, rating) }, enabled = enabled) {
        Text(text)
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StudyFocusCueCard(cue: StudyFocusCue, onAction: () -> Unit) {
    val accent = when (cue.kind) {
        StudyFocusKind.REVIEW -> MaterialTheme.colorScheme.primary
        StudyFocusKind.PACE -> MaterialTheme.colorScheme.tertiary
        StudyFocusKind.ROOTS -> Color(0xFF2D5B52)
        StudyFocusKind.NEW_WORDS -> Color(0xFFC98A3D)
        StudyFocusKind.MOMENTUM -> Color(0xFF6E8B3D)
    }
    val icon = when (cue.kind) {
        StudyFocusKind.REVIEW -> Icons.Rounded.PsychologyAlt
        StudyFocusKind.PACE -> Icons.Rounded.EventAvailable
        StudyFocusKind.ROOTS -> Icons.Rounded.AccountTree
        StudyFocusKind.NEW_WORDS -> Icons.Rounded.AutoStories
        StudyFocusKind.MOMENTUM -> Icons.Rounded.LocalFireDepartment
    }
    val animated by animateFloatAsState(
        targetValue = cue.progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "studyFocusProgress",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = accent.copy(alpha = 0.16f),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("今日学习焦点", style = MaterialTheme.typography.labelMedium, color = accent)
                            Text(cue.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "${(cue.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        color = accent,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    cue.message,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FocusMetricPill(cue.primaryLabel, cue.primaryValue, accent, Modifier.weight(1f))
                    FocusMetricPill(cue.secondaryLabel, cue.secondaryValue, accent, Modifier.weight(1f))
                }
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(cue.actionLabel)
                }
            }
        }
    }
}

@Composable
private fun DailyStudyRouteCard(route: DailyStudyRoute, onStepAction: (DailyStudyRouteStep) -> Unit) {
    val firstAccent = dailyRouteAccent(route.steps.first().target)
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            firstAccent.copy(alpha = 0.14f),
                            Color(0xFFD6B179).copy(alpha = 0.10f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text("今日训练路线", style = MaterialTheme.typography.labelMedium, color = firstAccent)
                        Text(
                            route.headline,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            route.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = firstAccent.copy(alpha = 0.14f),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = firstAccent,
                            modifier = Modifier
                                .padding(11.dp)
                                .size(25.dp),
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(route.steps, key = { "${it.target}-${it.title}" }) { step ->
                        DailyStudyRouteStepCard(step = step, onClick = { onStepAction(step) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyStudyRouteStepCard(step: DailyStudyRouteStep, onClick: () -> Unit) {
    val accent = dailyRouteAccent(step.target)
    val icon = dailyRouteIcon(step.target)
    val animated by animateFloatAsState(
        targetValue = step.weight.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "dailyRouteStepWeight",
    )
    Surface(
        modifier = Modifier.width(198.dp),
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.16f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                    )
                }
                Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(step.metricValue, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
                Text(
                    step.metricLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = accent,
                trackColor = accent.copy(alpha = 0.12f),
            )
            Text(
                step.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                modifier = Modifier.heightIn(min = 48.dp),
            )
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(step.actionLabel)
            }
        }
    }
}

@Composable
private fun dailyRouteAccent(target: DailyStudyRouteTarget): Color = when (target) {
    DailyStudyRouteTarget.REVIEW -> MaterialTheme.colorScheme.primary
    DailyStudyRouteTarget.TOUGH -> Color(0xFFB65245)
    DailyStudyRouteTarget.ROOTS -> Color(0xFF2D5B52)
    DailyStudyRouteTarget.LEARN -> Color(0xFFC98A3D)
}

private fun dailyRouteIcon(target: DailyStudyRouteTarget) = when (target) {
    DailyStudyRouteTarget.REVIEW -> Icons.Rounded.PsychologyAlt
    DailyStudyRouteTarget.TOUGH -> Icons.Rounded.LocalFireDepartment
    DailyStudyRouteTarget.ROOTS -> Icons.Rounded.AccountTree
    DailyStudyRouteTarget.LEARN -> Icons.Rounded.AutoStories
}

@Composable
private fun FocusMetricPill(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.09f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text)
        }
    }
}

@Composable
private fun AnchorBadge() {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            Icons.Rounded.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(12.dp),
        )
        Text(
            "地基词",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MorphemeRow(
    term: String,
    rootKey: String,
    rootRef: com.study.englishdemo.data.RootReference?,
) {
    val segments = remember(term, rootKey) { decomposeWord(term, rootKey) }
    if (segments.size <= 1) return
    val prefixBg = MaterialTheme.colorScheme.secondaryContainer
    val prefixFg = MaterialTheme.colorScheme.onSecondaryContainer
    val rootBg = MaterialTheme.colorScheme.primaryContainer
    val rootFg = MaterialTheme.colorScheme.onPrimaryContainer
    val suffixBg = MaterialTheme.colorScheme.tertiaryContainer
    val suffixFg = MaterialTheme.colorScheme.onTertiaryContainer
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            segments.forEach { seg ->
                val (bg, fg, label) = when (seg.kind) {
                    Morpheme.PREFIX -> Triple(prefixBg, prefixFg, "前缀")
                    Morpheme.ROOT -> Triple(rootBg, rootFg, "词根")
                    Morpheme.SUFFIX -> Triple(suffixBg, suffixFg, "后缀")
                    Morpheme.PLAIN -> return@forEach
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(bg, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            seg.text,
                            color = fg,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    Text(
                        label,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        val meaningText = rootRef?.meanings?.firstOrNull()?.takeIf { it.isNotBlank() }
        if (meaningText != null) {
            Text(
                "$rootKey = $meaningText",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SiblingChip(sib: WordEntry, onClick: (WordEntry) -> Unit) {
    val phase = sib.progress?.phase
    val (dotColor, _) = when (phase) {
        StudyPhase.MASTERED -> Color(0xFF6E8B3D) to "掌握"
        StudyPhase.REVIEW -> MaterialTheme.colorScheme.primary to "复习"
        StudyPhase.LEARNING -> MaterialTheme.colorScheme.secondary to "学习中"
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) to "新词"
    }
    AssistChip(
        onClick = { onClick(sib) },
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, RoundedCornerShape(4.dp)),
                )
                Text("${sib.term} · ${sib.translation.take(6)}")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootWordPreviewSheet(
    word: WordEntry,
    rootMeanings: List<String>,
    familyWords: List<WordEntry>,
    onSpeak: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val guide = remember(word, rootMeanings, familyWords) {
        buildRootWordGuide(
            word = word,
            rootMeanings = rootMeanings,
            familyTerms = familyWords.map { it.term },
        )
    }
    val practicePlan = remember(word, rootMeanings, familyWords) {
        buildRootWordPracticePlan(
            word = word,
            rootMeanings = rootMeanings,
            familyTerms = familyWords.map { it.term },
        )
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RootWordPreviewHero(
                word = word,
                guide = guide,
                practicePlan = practicePlan,
                onSpeak = onSpeak,
            )
            RootWordGuidePanel(guide)
            RootWordPracticePlanCard(practicePlan)
            if (word.rootKey.isNotBlank()) {
                val rootRef = com.study.englishdemo.data.RootReference(
                    key = word.rootKey,
                    meanings = rootMeanings,
                    examples = emptyList(),
                )
                MorphemeRow(term = word.term, rootKey = word.rootKey, rootRef = rootRef)
            }
            Text(word.translation.ifBlank { "暂无中文释义" }, style = MaterialTheme.typography.titleMedium)
            if (word.definition.isNotBlank()) {
                Text(
                    word.definition,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                )
            }
            if (word.example.isNotBlank()) {
                Text(
                    "例句：${word.example}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            RoundedCornerShape(18.dp),
                        )
                        .padding(14.dp),
                )
            }
            if (word.derivatives.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(
                        word.derivatives
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                            .distinctBy { it.lowercase() }
                            .take(8),
                        key = { it },
                    ) { derivative ->
                        AssistChip(onClick = {}, label = { Text(derivative) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RootWordPreviewHero(
    word: WordEntry,
    guide: RootWordGuide,
    practicePlan: RootWordPracticePlan,
    onSpeak: (String) -> Unit,
) {
    val accent = rootWordPracticePlanAccent(practicePlan.kind)
    Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        ),
                    ),
                    RoundedCornerShape(28.dp),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            "词根详情 · ${guide.badgeLabel}",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PhaseTag(word.progress?.phase)
                        IconButton(onClick = { onSpeak(word.term) }) {
                            Icon(Icons.Rounded.GraphicEq, contentDescription = "播放发音", tint = accent)
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(word.term, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        word.phonetic.ifBlank { practicePlan.actionLabel },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f),
                    )
                    Text(
                        word.translation.ifBlank { "暂无中文释义" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun RootWordGuidePanel(guide: RootWordGuide) {
    val accent = rootWordGuideAccent(guide.kind)
    val icon = rootWordGuideIcon(guide.kind)
    val intensity by animateFloatAsState(
        targetValue = guide.intensity.coerceIn(0f, 1f),
        animationSpec = tween(420),
        label = "rootWordGuideIntensity",
    )
    Surface(shape = RoundedCornerShape(24.dp), color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                    RoundedCornerShape(24.dp),
                )
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(15.dp), color = accent.copy(alpha = 0.18f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(guide.badgeLabel, style = MaterialTheme.typography.labelMedium, color = accent)
                            Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            guide.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text(
                    guide.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
                LinearProgressIndicator(
                    progress = { intensity },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    RootWordGuideMetric(guide.primaryLabel, guide.primaryValue, accent, Modifier.weight(1f))
                    RootWordGuideMetric(guide.secondaryLabel, guide.secondaryValue, accent, Modifier.weight(1f))
                }
                if (guide.focusTerms.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(guide.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RootWordPracticePlanCard(plan: RootWordPracticePlan) {
    val accent = rootWordPracticePlanAccent(plan.kind)
    val icon = rootWordPracticePlanIcon(plan.kind)
    val progress by animateFloatAsState(
        targetValue = plan.progress.coerceIn(0f, 1f),
        animationSpec = tween(460),
        label = "rootWordPracticePlan",
    )
    Surface(shape = RoundedCornerShape(26.dp), color = Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            accent.copy(alpha = 0.13f),
                        ),
                    ),
                    RoundedCornerShape(26.dp),
                )
                .padding(15.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.15f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("复盘计划", style = MaterialTheme.typography.labelMedium, color = accent)
                            Text(plan.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            plan.actionLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(
                    plan.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.11f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    RootWordGuideMetric(plan.primaryLabel, plan.primaryValue, accent, Modifier.weight(1f))
                    RootWordGuideMetric(plan.secondaryLabel, plan.secondaryValue, accent, Modifier.weight(1f))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    plan.steps.forEachIndexed { index, step ->
                        LearningLoopStepRow(
                            step = step,
                            accent = accent,
                            isLast = index == plan.steps.lastIndex,
                        )
                    }
                }
                if (plan.focusTerms.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(plan.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RootWordGuideMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.09f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun rootWordGuideAccent(kind: RootWordGuideKind): Color = when (kind) {
    RootWordGuideKind.ROOT_TRACE -> Color(0xFF2D5B52)
    RootWordGuideKind.WORD_FORMS -> Color(0xFF3F6F8F)
    RootWordGuideKind.CONTEXT -> Color(0xFFC98A3D)
    RootWordGuideKind.QUICK_REVIEW -> Color(0xFF6E8B3D)
}

private fun rootWordGuideIcon(kind: RootWordGuideKind) = when (kind) {
    RootWordGuideKind.ROOT_TRACE -> Icons.Rounded.AccountTree
    RootWordGuideKind.WORD_FORMS -> Icons.Rounded.Translate
    RootWordGuideKind.CONTEXT -> Icons.Rounded.AutoStories
    RootWordGuideKind.QUICK_REVIEW -> Icons.Rounded.PsychologyAlt
}

@Composable
private fun rootWordPracticePlanAccent(kind: RootWordPracticePlanKind): Color = when (kind) {
    RootWordPracticePlanKind.ROOT_LOOP -> Color(0xFF2D5B52)
    RootWordPracticePlanKind.FORM_LOOP -> Color(0xFF3F6F8F)
    RootWordPracticePlanKind.MEMORY_LOOP -> Color(0xFFB46B2B)
    RootWordPracticePlanKind.CONTEXT_LOOP -> Color(0xFFC98A3D)
    RootWordPracticePlanKind.QUICK_LOOP -> Color(0xFF6E8B3D)
}

private fun rootWordPracticePlanIcon(kind: RootWordPracticePlanKind) = when (kind) {
    RootWordPracticePlanKind.ROOT_LOOP -> Icons.Rounded.AccountTree
    RootWordPracticePlanKind.FORM_LOOP -> Icons.Rounded.Translate
    RootWordPracticePlanKind.MEMORY_LOOP -> Icons.Rounded.Star
    RootWordPracticePlanKind.CONTEXT_LOOP -> Icons.Rounded.AutoStories
    RootWordPracticePlanKind.QUICK_LOOP -> Icons.Rounded.PsychologyAlt
}

@Composable
private fun ExamCountdownCard(uiState: AppUiState) {
    val examDate = uiState.settings.examDate ?: return
    val today = LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(today, examDate).toInt()
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-M-d") }
    val gradientStart = MaterialTheme.colorScheme.primary
    val gradientEnd = MaterialTheme.colorScheme.tertiary
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Rounded.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        "考试倒计时",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    when {
                        daysLeft < 0 -> "已过考试日期 ${-daysLeft} 天"
                        daysLeft == 0 -> "就是今天，冲刺一把"
                        else -> "还有 $daysLeft 天"
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "考试日：${examDate.format(formatter)}" +
                        if (uiState.pace.isAuto) "，自动配速 ${uiState.pace.target} 词/天" else "",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun RootCoverageCard(uiState: AppUiState) {
    val snap = uiState.rootSnapshot
    val rootRatio = if (snap.totalRoots == 0) 0f else snap.touchedRoots.toFloat() / snap.totalRoots
    val wordRatio = if (snap.totalClustered == 0) 0f else snap.learnedClustered.toFloat() / snap.totalClustered
    val animatedRoot by animateFloatAsState(rootRatio, tween(500), label = "rootRatio")
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Rounded.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("词根覆盖", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        trackColor = Color.Transparent,
                    )
                    CircularProgressIndicator(
                        progress = { animatedRoot },
                        modifier = Modifier.size(72.dp),
                        strokeWidth = 8.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Transparent,
                    )
                    Text(
                        "${(rootRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        "已接触 ${snap.touchedRoots} / ${snap.totalRoots} 个词根",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "聚簇词掌握 ${snap.learnedClustered} / ${snap.totalClustered}（${(wordRatio * 100).toInt()}%）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    )
                    Text(
                        "学词根就是学一大把同源词，打开『词根』Tab 看全景",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamPlanCard(
    settings: com.study.englishdemo.data.SettingsState,
    pace: com.study.englishdemo.data.PaceRecommendation,
    onSave: (LocalDate?, Boolean) -> Unit,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    var autoOn by remember(settings.autoPaceEnabled) { mutableStateOf(settings.autoPaceEnabled) }
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-M-d") }
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("考试计划", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = autoOn,
                    onCheckedChange = {
                        autoOn = it
                        onSave(settings.examDate, it)
                    },
                )
            }
            Text(
                if (settings.examDate != null) {
                    val left = ChronoUnit.DAYS.between(LocalDate.now(), settings.examDate).toInt()
                    "考试日：${settings.examDate.format(formatter)}（还有 $left 天）"
                } else {
                    "未设置考试日期"
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
            if (autoOn && pace.isAuto) {
                Text(
                    "自动配速：根据剩余 ${pace.remainingWords} 词 + 剩余 ${pace.remainingDays} 天，推荐 ${pace.target} 词/天（含 20% 缓冲）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    "开启后按考试日期自动计算每日新词量；关闭则使用下方手动基线。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pickerOpen = true }) {
                    Text(if (settings.examDate == null) "设置考试日期" else "修改日期")
                }
                if (settings.examDate != null) {
                    TextButton(onClick = { onSave(null, autoOn) }) { Text("清除日期") }
                }
            }
        }
    }
    if (pickerOpen) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = settings.examDate
                ?.atStartOfDay(java.time.ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        onSave(date, autoOn)
                    }
                    pickerOpen = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { pickerOpen = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun RootsScreen(
    uiState: AppUiState,
    onSpeak: (String) -> Unit,
    onQuery: (String) -> Unit,
) {
    val atlasBrief = remember(uiState.rootGroups, uiState.rootSnapshot) {
        buildRootAtlasBrief(uiState.rootGroups, uiState.rootSnapshot)
    }
    val mnemonicBrief = remember(uiState.rootGroups) {
        buildRootMnemonicBrief(uiState.rootGroups)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle(
                "词根图谱",
                "「${uiState.selectedBookTitle}」的 ${uiState.rootSnapshot.totalRoots} 个词根。按词根成员数倒序，搜词根、含义或例词都能命中。",
            )
        }
        item {
            OutlinedTextField(
                value = uiState.rootsQuery,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("搜索词根（如 spec / 看见）") },
            )
        }
        item {
            RootAtlasBriefCard(atlasBrief)
        }
        item {
            RootMnemonicBriefCard(mnemonicBrief)
        }
        if (uiState.rootsLoading && uiState.rootGroups.isEmpty()) {
            item { EmptyStateCard("正在整理词根…") }
        } else if (uiState.rootGroups.isEmpty()) {
            item { EmptyStateCard("没有匹配的词根，换个关键词试试。") }
        } else {
            items(uiState.rootGroups, key = { it.rootKey }) { group ->
                RootGroupCard(group = group, onSpeak = onSpeak)
            }
        }
    }
}

@Composable
private fun RootMnemonicBriefCard(brief: RootMnemonicBrief) {
    val accent = rootMnemonicBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        RootMnemonicBriefKind.EMPTY -> Icons.Rounded.AutoStories
        RootMnemonicBriefKind.ROOT_SEED -> Icons.Rounded.AccountTree
        RootMnemonicBriefKind.PATCH_GAPS -> Icons.Rounded.Insights
        RootMnemonicBriefKind.READY -> Icons.Rounded.PsychologyAlt
        RootMnemonicBriefKind.SATURATED -> Icons.Rounded.Check
    }
    val progress by animateFloatAsState(
        targetValue = brief.progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "rootMnemonicProgress",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                            MaterialTheme.colorScheme.surface,
                            accent.copy(alpha = 0.18f),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 420f),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "根族巧记补给",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text(
                    brief.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    RootAtlasMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    RootAtlasMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                if (brief.focusRoots.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            "补给根族",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(brief.focusRoots, key = { it }) { root ->
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accent.copy(alpha = 0.10f),
                                ) {
                                    Text(
                                        root,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rootMnemonicBriefAccent(kind: RootMnemonicBriefKind): Color = when (kind) {
    RootMnemonicBriefKind.EMPTY -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
    RootMnemonicBriefKind.ROOT_SEED -> Color(0xFFC98A3D)
    RootMnemonicBriefKind.PATCH_GAPS -> Color(0xFFD1783A)
    RootMnemonicBriefKind.READY -> Color(0xFF2D5B52)
    RootMnemonicBriefKind.SATURATED -> Color(0xFF6E8B3D)
}

@Composable
private fun RootAtlasBriefCard(brief: RootAtlasBrief) {
    val accent = rootAtlasBriefAccent(brief.kind)
    val icon = when (brief.kind) {
        RootAtlasBriefKind.EMPTY -> Icons.Rounded.AutoStories
        RootAtlasBriefKind.SEED -> Icons.Rounded.AccountTree
        RootAtlasBriefKind.EXPAND -> Icons.Rounded.Insights
        RootAtlasBriefKind.CONSOLIDATE -> Icons.Rounded.PsychologyAlt
        RootAtlasBriefKind.MASTERED -> Icons.Rounded.Check
    }
    val progress by animateFloatAsState(
        targetValue = brief.progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "rootAtlasProgress",
    )
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.20f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.16f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(22.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                "词根图谱简报",
                                style = MaterialTheme.typography.labelMedium,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(brief.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            brief.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                Text(
                    brief.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    RootAtlasMetric(brief.primaryLabel, brief.primaryValue, accent, Modifier.weight(1f))
                    RootAtlasMetric(brief.secondaryLabel, brief.secondaryValue, accent, Modifier.weight(1f))
                }
                if (brief.focusRoots.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            "优先根族",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(brief.focusRoots, key = { it }) { root ->
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = accent.copy(alpha = 0.10f),
                                ) {
                                    Text(
                                        root,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = accent,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RootAtlasMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}

@Composable
private fun rootAtlasBriefAccent(kind: RootAtlasBriefKind): Color = when (kind) {
    RootAtlasBriefKind.EMPTY -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
    RootAtlasBriefKind.SEED -> Color(0xFFC98A3D)
    RootAtlasBriefKind.EXPAND -> MaterialTheme.colorScheme.primary
    RootAtlasBriefKind.CONSOLIDATE -> Color(0xFF2D5B52)
    RootAtlasBriefKind.MASTERED -> Color(0xFF6E8B3D)
}

@Composable
private fun RootGroupCard(group: RootGroup, onSpeak: (String) -> Unit) {
    var expanded by remember(group.rootKey) { mutableStateOf(false) }
    var selectedWord by remember(group.rootKey) { mutableStateOf<WordEntry?>(null) }
    selectedWord?.let { selected ->
        RootWordPreviewSheet(
            word = selected,
            rootMeanings = group.meanings,
            familyWords = group.members,
            onSpeak = onSpeak,
            onDismiss = { selectedWord = null },
        )
    }
    val insight = remember(group) { buildRootGroupInsight(group) }
    val accent = rootGroupStageAccent(insight.stage)
    val focusWords = remember(group, insight.focusTerms) {
        val byTerm = group.members.associateBy { it.term.lowercase() }
        insight.focusTerms.mapNotNull { byTerm[it.lowercase()] }
    }
    val animatedRatio by animateFloatAsState(
        targetValue = insight.progress.coerceIn(0f, 1f),
        animationSpec = tween(420),
        label = "rootGroupRatio",
    )
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = accent.copy(alpha = 0.16f),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountTree,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    group.rootKey,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                RootStageBadge(insight.badgeLabel, accent)
                            }
                            if (group.meanings.isNotEmpty()) {
                                Text(
                                    group.meanings.joinToString("；"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                )
                            }
                        }
                    }
                    Text(
                        "${(insight.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }

                Text(
                    insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
                Text(
                    insight.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                )
                LinearProgressIndicator(
                    progress = { animatedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FocusMetricPill(insight.primaryLabel, insight.primaryValue, accent, Modifier.weight(1f))
                    FocusMetricPill(insight.secondaryLabel, insight.secondaryValue, accent, Modifier.weight(1f))
                }
                if (focusWords.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "推荐先看",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(focusWords, key = { it.id }) { word ->
                                RootFocusTermChip(
                                    term = word.term,
                                    accent = accent,
                                    onClick = { selectedWord = word },
                                )
                            }
                        }
                    }
                }
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起路线" else insight.actionLabel)
                }
                AnimatedVisibility(visible = expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            group.members.chunked(2).forEach { rowWords ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowWords.forEach { w ->
                                        SiblingChip(sib = w, onClick = { selectedWord = it })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rootGroupStageAccent(stage: RootGroupStage): Color = when (stage) {
    RootGroupStage.SEED -> Color(0xFFC98A3D)
    RootGroupStage.BUILDING -> MaterialTheme.colorScheme.primary
    RootGroupStage.CONSOLIDATING -> Color(0xFF2D5B52)
    RootGroupStage.MASTERED -> Color(0xFF6E8B3D)
}

@Composable
private fun RootStageBadge(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RootFocusTermChip(term: String, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.10f),
    ) {
        Text(
            term,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PracticeModeBar(
    current: com.study.englishdemo.data.PracticeMode,
    onChange: (com.study.englishdemo.data.PracticeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            PracticeChip(
                label = "翻卡",
                icon = Icons.Rounded.AutoStories,
                selected = current == com.study.englishdemo.data.PracticeMode.FLIP,
                onClick = { onChange(com.study.englishdemo.data.PracticeMode.FLIP) },
            )
        }
        item {
            PracticeChip(
                label = "选择",
                icon = Icons.Rounded.Quiz,
                selected = current == com.study.englishdemo.data.PracticeMode.CHOICE,
                onClick = { onChange(com.study.englishdemo.data.PracticeMode.CHOICE) },
            )
        }
        item {
            PracticeChip(
                label = "完形",
                icon = Icons.Rounded.Edit,
                selected = current == com.study.englishdemo.data.PracticeMode.CLOZE,
                onClick = { onChange(com.study.englishdemo.data.PracticeMode.CLOZE) },
            )
        }
        item {
            PracticeChip(
                label = "拼写",
                icon = Icons.Rounded.Keyboard,
                selected = current == com.study.englishdemo.data.PracticeMode.SPELL,
                onClick = { onChange(com.study.englishdemo.data.PracticeMode.SPELL) },
            )
        }
        item {
            PracticeChip(
                label = "听写",
                icon = Icons.Rounded.Hearing,
                selected = current == com.study.englishdemo.data.PracticeMode.DICTATION,
                onClick = { onChange(com.study.englishdemo.data.PracticeMode.DICTATION) },
            )
        }
    }
}

@Composable
private fun PracticeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = fg,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun QuizPracticePager(
    words: List<WordEntry>,
    buildQuiz: suspend (Long) -> com.study.englishdemo.data.QuizQuestion?,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
) {
    var index by remember(words.firstOrNull()?.id) { mutableIntStateOf(0) }
    val word = words.getOrNull(index) ?: words.firstOrNull() ?: return
    var question by remember(word.id) { mutableStateOf<com.study.englishdemo.data.QuizQuestion?>(null) }
    var loading by remember(word.id) { mutableStateOf(true) }
    var selected by remember(word.id) { mutableStateOf<String?>(null) }
    var resolved by remember(word.id) { mutableStateOf(false) }
    LaunchedEffect(word.id) {
        loading = true
        question = buildQuiz(word.id)
        loading = false
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "${index + 1} / ${words.size}  选择与中文匹配的单词",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        word.translation,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (word.pos.isNotBlank()) {
                        Text(
                            word.pos,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
        val q = question
        if (loading) {
            item { EmptyStateCard("正在生成选项…") }
        } else if (q == null) {
            item {
                EmptyStateCard("这张卡暂时凑不齐 3 个干扰项，先跳过。")
            }
            item {
                Button(
                    onClick = { if (index < words.size - 1) index++ else index = 0 },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (index < words.size - 1) "跳到下一题" else "回到开头") }
            }
        } else {
            items(q.options) { option ->
                QuizOptionButton(
                    text = option,
                    state = when {
                        !resolved -> QuizOptionState.IDLE
                        option == q.correct -> QuizOptionState.CORRECT
                        option == selected -> QuizOptionState.WRONG
                        else -> QuizOptionState.DIMMED
                    },
                    onClick = {
                        if (!resolved) {
                            selected = option
                            resolved = true
                            val rating = if (option == q.correct) ReviewRating.GOOD else ReviewRating.AGAIN
                            onRate(word.id, rating)
                        }
                    },
                )
            }
            if (resolved) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (selected == q.correct) "对了，继续保持" else "正确答案：${q.correct}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected == q.correct) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            word.definition.ifBlank { "（无英文解释）" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onSpeak(word.term) }) { Text("听发音") }
                            Button(
                                onClick = {
                                    selected = null
                                    resolved = false
                                    if (index < words.size - 1) index++ else index = 0
                                },
                            ) { Text(if (index < words.size - 1) "下一题" else "再来一轮") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClozePracticePager(
    words: List<WordEntry>,
    buildCloze: suspend (Long) -> com.study.englishdemo.data.ClozeQuestion?,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
) {
    var index by remember(words.firstOrNull()?.id) { mutableIntStateOf(0) }
    val word = words.getOrNull(index) ?: words.firstOrNull() ?: return
    var question by remember(word.id) { mutableStateOf<com.study.englishdemo.data.ClozeQuestion?>(null) }
    var loading by remember(word.id) { mutableStateOf(true) }
    var selected by remember(word.id) { mutableStateOf<String?>(null) }
    var resolved by remember(word.id) { mutableStateOf(false) }
    LaunchedEffect(word.id) {
        loading = true
        question = buildCloze(word.id)
        loading = false
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "${index + 1} / ${words.size}  读例句，补回被挖空的单词",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        val q = question
        if (loading) {
            item { EmptyStateCard("正在挑选可挖空例句…") }
        } else if (q == null) {
            item {
                EmptyStateCard("这张卡没有命中原词或派生词的例句，先跳过。")
            }
            item {
                Button(
                    onClick = { if (index < words.size - 1) index++ else index = 0 },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (index < words.size - 1) "跳到下一题" else "回到开头") }
            }
        } else {
            val guide = buildClozeContextGuide(q)
            item {
                Card(shape = RoundedCornerShape(26.dp)) {
                    Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            q.prompt,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            word.translation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                        if (word.rootKey.isNotBlank()) {
                            Text(
                                "词根线索：${word.rootKey}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
            item {
                ClozeContextGuidePanel(guide)
            }
            items(q.options) { option ->
                QuizOptionButton(
                    text = option,
                    state = when {
                        !resolved -> QuizOptionState.IDLE
                        option == q.correct -> QuizOptionState.CORRECT
                        option == selected -> QuizOptionState.WRONG
                        else -> QuizOptionState.DIMMED
                    },
                    onClick = {
                        if (!resolved) {
                            selected = option
                            resolved = true
                            val rating = if (option == q.correct) ReviewRating.GOOD else ReviewRating.AGAIN
                            onRate(word.id, rating)
                        }
                    },
                )
            }
            if (resolved) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (selected == q.correct) "上下文判断正确" else "正确答案：${q.correct}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selected == q.correct) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "原句：${word.example}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                        if (word.definition.isNotBlank()) {
                            Text(
                                word.definition,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onSpeak(word.term) }) { Text("听发音") }
                            Button(
                                onClick = {
                                    selected = null
                                    resolved = false
                                    if (index < words.size - 1) index++ else index = 0
                                },
                            ) { Text(if (index < words.size - 1) "下一题" else "再来一轮") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClozeContextGuidePanel(guide: ClozeContextGuide) {
    val accent = clozeContextGuideAccent(guide.kind)
    val icon = clozeContextGuideIcon(guide.kind)
    val confidence by animateFloatAsState(
        targetValue = guide.confidence.coerceIn(0f, 1f),
        animationSpec = tween(420),
        label = "clozeContextGuideConfidence",
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                        ),
                    ),
                )
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(shape = RoundedCornerShape(15.dp), color = accent.copy(alpha = 0.15f)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp),
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                guide.badgeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(guide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                guide.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.12f)) {
                        Text(
                            guide.actionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { confidence },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ClozeGuideMetric(guide.primaryLabel, guide.primaryValue, accent, Modifier.weight(1f))
                    ClozeGuideMetric(guide.secondaryLabel, guide.secondaryValue, accent, Modifier.weight(1f))
                }
                if (guide.focusTerms.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        items(guide.focusTerms, key = { it }) { term ->
                            Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.10f)) {
                                Text(
                                    term,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClozeGuideMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun clozeContextGuideAccent(kind: ClozeContextGuideKind): Color = when (kind) {
    ClozeContextGuideKind.ROOT_TRACE -> Color(0xFF2D5B52)
    ClozeContextGuideKind.WORD_FORM -> Color(0xFF3F6F8F)
    ClozeContextGuideKind.MEANING -> Color(0xFFC98A3D)
    ClozeContextGuideKind.QUICK_SCAN -> Color(0xFF6E8B3D)
}

private fun clozeContextGuideIcon(kind: ClozeContextGuideKind) = when (kind) {
    ClozeContextGuideKind.ROOT_TRACE -> Icons.Rounded.AccountTree
    ClozeContextGuideKind.WORD_FORM -> Icons.Rounded.Translate
    ClozeContextGuideKind.MEANING -> Icons.Rounded.AutoStories
    ClozeContextGuideKind.QUICK_SCAN -> Icons.Rounded.PsychologyAlt
}

private enum class QuizOptionState { IDLE, CORRECT, WRONG, DIMMED }

@Composable
private fun QuizOptionButton(text: String, state: QuizOptionState, onClick: () -> Unit) {
    val bg = when (state) {
        QuizOptionState.CORRECT -> MaterialTheme.colorScheme.primaryContainer
        QuizOptionState.WRONG -> MaterialTheme.colorScheme.errorContainer
        QuizOptionState.DIMMED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        QuizOptionState.IDLE -> MaterialTheme.colorScheme.surface
    }
    val fg = when (state) {
        QuizOptionState.CORRECT -> MaterialTheme.colorScheme.onPrimaryContainer
        QuizOptionState.WRONG -> MaterialTheme.colorScheme.onErrorContainer
        QuizOptionState.DIMMED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        QuizOptionState.IDLE -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(18.dp),
        border = if (state == QuizOptionState.IDLE)
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = fg, modifier = Modifier.weight(1f))
            when (state) {
                QuizOptionState.CORRECT -> Icon(
                    Icons.Rounded.Check, contentDescription = null, tint = fg,
                )
                QuizOptionState.WRONG -> Icon(
                    Icons.Rounded.Close, contentDescription = null, tint = fg,
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun SpellPracticePager(
    words: List<WordEntry>,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    gradeSpelling: (String, String) -> ReviewRating,
    withAudio: Boolean,
) {
    var index by remember(words.firstOrNull()?.id) { mutableIntStateOf(0) }
    val word = words.getOrNull(index) ?: words.firstOrNull() ?: return
    var input by remember(word.id) { mutableStateOf("") }
    var rating by remember(word.id) { mutableStateOf<ReviewRating?>(null) }
    LaunchedEffect(word.id) {
        if (withAudio) onSpeak(word.term)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                "${index + 1} / ${words.size}  " + if (withAudio) "听音写出英文" else "根据中文写出英文",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (withAudio) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            IconButton(onClick = { onSpeak(word.term) }) {
                                Icon(Icons.Rounded.GraphicEq, contentDescription = "重播")
                            }
                            Text(
                                if (rating == null) "点喇叭重播发音，在下方写出单词" else word.term,
                                style = if (rating == null) MaterialTheme.typography.bodyMedium
                                else MaterialTheme.typography.headlineSmall,
                                fontWeight = if (rating == null) FontWeight.Normal else FontWeight.Bold,
                            )
                        }
                    } else {
                        Text(
                            word.translation,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    if (rating != null && !withAudio) {
                        Text(
                            "正确：${word.term}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = input,
                onValueChange = { if (rating == null) input = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("输入英文") },
                enabled = rating == null,
                textStyle = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            if (rating == null) {
                Button(
                    onClick = {
                        val r = gradeSpelling(input, word.term)
                        rating = r
                        onRate(word.id, r)
                    },
                    enabled = input.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("提交") }
            } else {
                val r = rating!!
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val label = when (r) {
                        ReviewRating.EASY -> "完美" to MaterialTheme.colorScheme.primary
                        ReviewRating.GOOD -> "基本正确" to MaterialTheme.colorScheme.primary
                        ReviewRating.HARD -> "有拼写错误，加入复习" to MaterialTheme.colorScheme.secondary
                        ReviewRating.AGAIN -> "差得较多，重来" to MaterialTheme.colorScheme.error
                    }
                    Text(
                        label.first,
                        color = label.second,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (withAudio) {
                        Text(
                            "你写：$input",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                        Text(
                            "正确：${word.term}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        word.translation,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                    Button(
                        onClick = {
                            input = ""
                            rating = null
                            if (index < words.size - 1) index++ else index = 0
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (index < words.size - 1) "下一个" else "再来一轮") }
                }
            }
        }
    }
}

@Composable
private fun ToughWordsScreen(
    uiState: AppUiState,
    onSpeak: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
    onRate: (Long, ReviewRating) -> Unit,
    onRefresh: () -> Unit,
) {
    LaunchedEffect(uiState.selectedBookId) { onRefresh() }
    val toughBrief = remember(uiState.toughWords) {
        buildToughWordsBrief(uiState.toughWords)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle(
                "难词专攻",
                "按『重来』次数排序，并给每个错题生成重建、词根、语境或巩固处方。",
            )
        }
        if (uiState.toughLoading && uiState.toughWords.isEmpty()) {
            item { EmptyStateCard("正在统计错题…") }
        } else if (uiState.toughWords.isEmpty()) {
            item { EmptyStateCard("还没有翻车过的词，保持住") }
        } else {
            item { ToughWordsBriefCard(toughBrief) }
            items(uiState.toughWords, key = { it.word.id }) { tough ->
                ToughWordCard(
                    tough = tough,
                    rootRef = uiState.rootRefByKey[tough.word.rootKey],
                    isAnchor = tough.word.id in uiState.anchorWordIds,
                    onSpeak = onSpeak,
                    onRate = onRate,
                    onMnemonic = onMnemonic,
                )
            }
        }
    }
}

@Composable
private fun ToughWordsBriefCard(brief: ToughWordsBrief) {
    val accent = toughPrescriptionAccent(brief.dominantKind)
    val animated by animateFloatAsState(
        targetValue = brief.intensity.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "toughWordsBriefIntensity",
    )
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                        Text("错题战情台", style = MaterialTheme.typography.labelMedium, color = accent)
                        Text(brief.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            brief.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                    Surface(shape = RoundedCornerShape(999.dp), color = accent.copy(alpha = 0.14f)) {
                        Text(
                            brief.dominantLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { animated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.12f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ToughBriefMetric("错题", brief.totalCount.toString(), accent, Modifier.weight(1f))
                    ToughBriefMetric("高风险", brief.highRiskCount.toString(), accent, Modifier.weight(1f))
                    ToughBriefMetric("最高重来", brief.peakAgainCount.toString(), accent, Modifier.weight(1f))
                }
                Surface(shape = RoundedCornerShape(18.dp), color = accent.copy(alpha = 0.10f)) {
                    Text(
                        brief.actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ToughBriefMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.09f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            )
        }
    }
}

@Composable
private fun ToughWordCard(
    tough: com.study.englishdemo.data.ToughWord,
    rootRef: com.study.englishdemo.data.RootReference?,
    isAnchor: Boolean,
    onSpeak: (String) -> Unit,
    onRate: (Long, ReviewRating) -> Unit,
    onMnemonic: (Long, String) -> Unit,
) {
    var expanded by remember(tough.word.id) { mutableStateOf(false) }
    var editing by remember(tough.word.id) { mutableStateOf(false) }
    var draft by remember(tough.word.id, tough.word.mnemonic) { mutableStateOf(tough.word.mnemonic) }
    val word = tough.word
    val prescription = remember(tough) { buildToughWordPrescription(tough) }
    val accent = toughPrescriptionAccent(prescription.kind)
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f),
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(word.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            ToughPrescriptionBadge(prescription.badgeLabel, accent)
                            if (isAnchor) AnchorBadge()
                        }
                        Text(
                            word.translation,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${tough.againCount} 次",
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "重来",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                }
                ToughPrescriptionPanel(prescription = prescription, accent = accent)
                if (word.rootKey.isNotBlank()) {
                    MorphemeRow(term = word.term, rootKey = word.rootKey, rootRef = rootRef)
                }
                AnimatedVisibility(visible = expanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (word.example.isNotBlank()) {
                            Text(
                                "例句：${word.example}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                            )
                        }
                        if (!editing && word.mnemonic.isNotBlank()) {
                            MnemonicSignalCard(word.mnemonic)
                        }
                        if (editing) {
                            OutlinedTextField(
                                value = draft,
                                onValueChange = { draft = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("写一句让你记住的话") },
                                minLines = 2,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = {
                                    onMnemonic(word.id, draft)
                                    editing = false
                                }) { Text("保存") }
                                TextButton(onClick = { editing = false }) { Text("取消") }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onSpeak(word.term) }) { Text("听发音") }
                                TextButton(onClick = { editing = true }) {
                                    Text(if (word.mnemonic.isBlank()) "+ 巧记" else "编辑巧记")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onRate(word.id, ReviewRating.GOOD) },
                                    modifier = Modifier.weight(1f),
                                ) { Text("这次会了") }
                                TextButton(
                                    onClick = { onRate(word.id, ReviewRating.AGAIN) },
                                    modifier = Modifier.weight(1f),
                                ) { Text("还是不会") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun toughPrescriptionAccent(kind: ToughWordPrescriptionKind): Color = when (kind) {
    ToughWordPrescriptionKind.REBUILD -> Color(0xFFB65245)
    ToughWordPrescriptionKind.ROOT_TRACE -> Color(0xFF2D5B52)
    ToughWordPrescriptionKind.CONTEXT -> MaterialTheme.colorScheme.primary
    ToughWordPrescriptionKind.STABILIZE -> Color(0xFF6E8B3D)
}

@Composable
private fun ToughPrescriptionBadge(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ToughPrescriptionPanel(prescription: ToughWordPrescription, accent: Color) {
    val icon = when (prescription.kind) {
        ToughWordPrescriptionKind.REBUILD -> Icons.Rounded.PsychologyAlt
        ToughWordPrescriptionKind.ROOT_TRACE -> Icons.Rounded.AccountTree
        ToughWordPrescriptionKind.CONTEXT -> Icons.Rounded.AutoStories
        ToughWordPrescriptionKind.STABILIZE -> Icons.Rounded.Check
    }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = accent.copy(alpha = 0.10f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = 0.16f)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            "错题处方",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(prescription.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .background(accent.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(prescription.actionLabel, style = MaterialTheme.typography.labelSmall, color = accent)
                }
            }
            Text(
                prescription.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
            )
            LinearProgressIndicator(
                progress = { prescription.intensity },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = accent,
                trackColor = accent.copy(alpha = 0.12f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ToughPrescriptionMetric(
                    prescription.primaryLabel,
                    prescription.primaryValue,
                    accent,
                    Modifier.weight(1f),
                )
                ToughPrescriptionMetric(
                    prescription.secondaryLabel,
                    prescription.secondaryValue,
                    accent,
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ToughPrescriptionMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
            )
        }
    }
}
