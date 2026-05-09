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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PsychologyAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.study.englishdemo.data.ReviewRating
import com.study.englishdemo.data.StudyPhase
import com.study.englishdemo.data.WordBook
import com.study.englishdemo.data.WordEntry
import com.study.englishdemo.ui.AppUiState
import com.study.englishdemo.ui.AppViewModel
import com.study.englishdemo.ui.AppViewModelFactory
import com.study.englishdemo.ui.LexRiseTheme
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

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
                    onSelectBook = viewModel::selectBook,
                    onImportBook = viewModel::importBook,
                    onDeleteBook = viewModel::deleteBook,
                    onRenameBook = viewModel::renameBook,
                    onVocabularyQuery = viewModel::updateVocabularyQuery,
                    onVocabularyPhase = viewModel::updateVocabularyPhase,
                    onMnemonic = viewModel::updateMnemonic,
                )
            }
        }
    }
}

private enum class HomeTab { DASHBOARD, LEARN, REVIEW, VOCAB, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LexRiseApp(
    uiState: AppUiState,
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onDailyTargetChange: (Int) -> Unit,
    onReminderSettingsSave: (Boolean, Int, Int) -> Unit,
    onSelectBook: (Long) -> Unit,
    onImportBook: (android.content.Context, android.net.Uri) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onRenameBook: (Long, String) -> Unit,
    onVocabularyQuery: (String) -> Unit,
    onVocabularyPhase: (StudyPhase?) -> Unit,
    onMnemonic: (Long, String) -> Unit,
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
                    selected = currentTab == HomeTab.VOCAB,
                    onClick = { currentTab = HomeTab.VOCAB },
                    icon = { Icon(Icons.Rounded.Translate, contentDescription = null) },
                    label = { Text("词汇") },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.SETTINGS,
                    onClick = { currentTab = HomeTab.SETTINGS },
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                    label = { Text("设置") },
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
                    HomeTab.REVIEW -> ReviewScreen(uiState, onRate, onSpeak, onMnemonic)
                    HomeTab.VOCAB -> VocabularyScreen(uiState, onSpeak, onVocabularyQuery, onVocabularyPhase)
                    HomeTab.SETTINGS -> SettingsScreen(
                        uiState = uiState,
                        onDailyTargetChange = onDailyTargetChange,
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
) {
    val words = uiState.session?.dueReviewWords.orEmpty()
    WordQueueScreen(
        title = "复习训练",
        subtitle = "当前词书：${uiState.selectedBookTitle}。先自测回忆，再翻卡核对。",
        words = words,
        uiState = uiState,
        onRate = onRate,
        onSpeak = onSpeak,
        onMnemonic = onMnemonic,
    )
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
            OutlinedTextField(
                value = uiState.vocabularyQuery,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                label = { Text("搜索单词、释义或英文解释") },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhaseChip("全部", uiState.vocabularyPhaseFilter == null) { onPhase(null) }
                PhaseChip("新词", uiState.vocabularyPhaseFilter == StudyPhase.NEW) { onPhase(StudyPhase.NEW) }
                PhaseChip("学习中", uiState.vocabularyPhaseFilter == StudyPhase.LEARNING) { onPhase(StudyPhase.LEARNING) }
                PhaseChip("复习", uiState.vocabularyPhaseFilter == StudyPhase.REVIEW) { onPhase(StudyPhase.REVIEW) }
                PhaseChip("掌握", uiState.vocabularyPhaseFilter == StudyPhase.MASTERED) { onPhase(StudyPhase.MASTERED) }
            }
        }
        if (uiState.vocabularyLoading && uiState.vocabularyResults.isEmpty()) {
            item { EmptyStateCard("正在加载词汇…") }
        } else if (uiState.vocabularyResults.isEmpty()) {
            item { EmptyStateCard("没有匹配的单词，换个关键词试试。") }
        } else {
            items(uiState.vocabularyResults) { word ->
                VocabularyRow(word = word, onSpeak = onSpeak)
            }
        }
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

@Composable
private fun SettingsScreen(
    uiState: AppUiState,
    onDailyTargetChange: (Int) -> Unit,
    onReminderSettingsSave: (Boolean, Int, Int) -> Unit,
) {
    var sliderValue by remember(uiState.settings.dailyNewWordTarget) {
        mutableStateOf(uiState.settings.dailyNewWordTarget.toFloat())
    }
    var reminderEnabled by remember(uiState.settings.reviewReminderEnabled) {
        mutableStateOf(uiState.settings.reviewReminderEnabled)
    }
    var reminderHour by remember(uiState.settings.reminderHour) {
        mutableStateOf(uiState.settings.reminderHour.toFloat())
    }
    var reminderMinute by remember(uiState.settings.reminderMinute) {
        mutableStateOf(uiState.settings.reminderMinute.toFloat())
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
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("每日新词目标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${sliderValue.toInt()} 个", style = MaterialTheme.typography.headlineMedium)
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 5f..40f,
                        steps = 6,
                    )
                    Button(onClick = { onDailyTargetChange(sliderValue.toInt()) }) {
                        Text("保存计划")
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
    onRate: (Long, ReviewRating) -> Unit,
    onSpeak: (String) -> Unit,
    onMnemonic: (Long, String) -> Unit,
) {
    var revealed by remember(word.id) { mutableStateOf(false) }
    var mnemonicEditorOpen by remember(word.id) { mutableStateOf(false) }
    var mnemonicDraft by remember(word.id, word.mnemonic) { mutableStateOf(word.mnemonic) }
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
                    PhaseTag(word.progress?.phase)
                    IconButton(onClick = { onSpeak(word.term) }) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = "播放发音")
                    }
                }
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    RoundedCornerShape(16.dp),
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                "词根 [${r.key}]",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (r.meanings.isNotEmpty()) {
                                Text(r.meanings.joinToString("；"), style = MaterialTheme.typography.bodySmall)
                            }
                            if (r.examples.isNotEmpty()) {
                                Text(
                                    "例词：${r.examples.take(5).joinToString("、")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                )
                            }
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
                                AssistChip(
                                    onClick = { onSpeak(sib.term) },
                                    label = { Text("${sib.term} · ${sib.translation.take(6)}") },
                                )
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
