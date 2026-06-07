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
import com.study.englishdemo.data.DailyReviewCount
import com.study.englishdemo.data.Morpheme
import com.study.englishdemo.data.ReviewRating
import com.study.englishdemo.data.RootGroup
import com.study.englishdemo.data.StudyPhase
import com.study.englishdemo.data.WordBook
import com.study.englishdemo.data.WordEntry
import com.study.englishdemo.data.decomposeWord
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
                    HomeTab.DASHBOARD -> DashboardScreen(uiState, onSelectBook, onDeleteBook, onRenameBook)
                    HomeTab.LEARN -> LearnScreen(uiState, onRate, onSpeak, onMnemonic)
                    HomeTab.REVIEW -> ReviewScreen(
                        uiState = uiState,
                        onRate = onRate,
                        onSpeak = onSpeak,
                        onMnemonic = onMnemonic,
                        onModeChange = onModeChange,
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
) {
    val session = requireNotNull(uiState.session)
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
    WordQueueScreen(
        title = "新词学习",
        subtitle = "当前词书：${uiState.selectedBookTitle}。按词根聚簇出词——同根词连着记，比字母序高效。",
        words = words,
        uiState = uiState,
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
    buildQuiz: suspend (Long) -> com.study.englishdemo.data.QuizQuestion?,
    buildCloze: suspend (Long) -> com.study.englishdemo.data.ClozeQuestion?,
    gradeSpelling: (String, String) -> ReviewRating,
) {
    val words = uiState.session?.dueReviewWords.orEmpty()
    Column(modifier = Modifier.fillMaxSize()) {
        PracticeModeBar(
            current = uiState.practiceMode,
            onChange = onModeChange,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
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
private fun VocabularyScreen(
    uiState: AppUiState,
    onSpeak: (String) -> Unit,
    onQuery: (String) -> Unit,
    onPhase: (StudyPhase?) -> Unit,
) {
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
                    label = { Text("搜索单词、释义或英文解释") },
                    supportingText = { Text("支持拼写容错：clarfy 也会尝试找 clarify") },
                )
                VocabularySearchStatus(
                    query = uiState.vocabularyQuery,
                    resultCount = uiState.vocabularyResults.size,
                    loading = uiState.vocabularyLoading,
                )
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
        if (uiState.vocabularyLoading && uiState.vocabularyResults.isEmpty()) {
            item { EmptyStateCard("正在加载词汇…") }
        } else if (uiState.vocabularyResults.isEmpty()) {
            item { EmptyStateCard("没有匹配的单词，换个关键词试试。") }
        } else {
            items(uiState.vocabularyResults, key = { it.id }) { word ->
                VocabularyRow(word = word, onSpeak = onSpeak)
            }
        }
    }
}

@Composable
private fun VocabularySearchStatus(query: String, resultCount: Int, loading: Boolean) {
    val trimmed = query.trim()
    val text = when {
        loading -> "正在检索词书与学习阶段…"
        trimmed.isEmpty() -> "可输入英文、中文释义或解释；英文词形支持轻微拼写错误。"
        resultCount == 0 -> "未找到结果。试试更完整的单词，或改用中文释义搜索。"
        else -> "找到 $resultCount 个结果；若没有精确命中，已按词形相似度补充候选。"
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
private fun PhaseChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun VocabularyRow(word: WordEntry, onSpeak: (String) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chips.forEach { label ->
                        AssistChip(onClick = {}, label = { Text(label) })
                    }
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
    selectedSibling?.let { selected ->
        RootWordPreviewSheet(
            word = selected,
            rootMeanings = rootRef?.meanings.orEmpty(),
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
                        Text(
                            "巧记：${word.mnemonic}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
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
    onSpeak: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(word.term, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        word.phonetic.ifBlank { "同根词速览" },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PhaseTag(word.progress?.phase)
                    IconButton(onClick = { onSpeak(word.term) }) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = "播放发音")
                    }
                }
            }
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    word.derivatives.take(6).forEach { d ->
                        AssistChip(onClick = {}, label = { Text(d) })
                    }
                }
            }
        }
    }
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
private fun RootGroupCard(group: RootGroup, onSpeak: (String) -> Unit) {
    var expanded by remember(group.rootKey) { mutableStateOf(false) }
    var selectedWord by remember(group.rootKey) { mutableStateOf<WordEntry?>(null) }
    selectedWord?.let { selected ->
        RootWordPreviewSheet(
            word = selected,
            rootMeanings = group.meanings,
            onSpeak = onSpeak,
            onDismiss = { selectedWord = null },
        )
    }
    val ratio = if (group.totalWords == 0) 0f else group.learnedWords.toFloat() / group.totalWords
    val animatedRatio by animateFloatAsState(ratio, tween(360), label = "rootGroupRatio")
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        group.rootKey,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (group.meanings.isNotEmpty()) {
                        Text(
                            group.meanings.joinToString("；"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                    }
                }
                Text(
                    "${group.learnedWords}/${group.totalWords}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LinearProgressIndicator(
                progress = { animatedRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            )
            val preview = if (expanded) group.members else group.members.take(6)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    preview.chunked(2).forEach { rowWords ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowWords.forEach { w ->
                                SiblingChip(sib = w, onClick = { selectedWord = it })
                            }
                        }
                    }
                }
            }
            if (group.members.size > 6) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "收起" else "展开全部 ${group.members.size} 个词")
                }
            }
        }
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
                EmptyStateCard("这张卡没有命中单词原形的例句，先跳过。")
            }
            item {
                Button(
                    onClick = { if (index < words.size - 1) index++ else index = 0 },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (index < words.size - 1) "跳到下一题" else "回到开头") }
            }
        } else {
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle(
                "难词专攻",
                "按『重来』次数排序的错题本。先把这些啃下，整本掌握度会显著前进。",
            )
        }
        if (uiState.toughLoading && uiState.toughWords.isEmpty()) {
            item { EmptyStateCard("正在统计错题…") }
        } else if (uiState.toughWords.isEmpty()) {
            item { EmptyStateCard("还没有翻车过的词，保持住") }
        } else {
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
    Card(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(word.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "重来",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
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
                        Text(
                            "巧记：${word.mnemonic}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
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
