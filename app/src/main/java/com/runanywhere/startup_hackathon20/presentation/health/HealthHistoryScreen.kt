package com.runanywhere.startup_hackathon20.presentation.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.presentation.health.HealthTrend
import kotlinx.coroutines.launch
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthHistoryScreen(
    viewModel: HealthHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF81D4FA).copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
    ) {
        // Top App Bar
        LargeTopAppBar(
            title = {
                Text(
                    text = "📊 Health History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = Color.Transparent
            ),
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Selector
            item {
                DateRangeSelector(
                    selectedRange = uiState.selectedTimeRange,
                    onRangeSelected = viewModel::selectTimeRange
                )
            }

            // Health Overview Cards
            item {
                HealthOverviewCards(uiState = uiState)
            }

            // Interactive Charts Section
            item {
                Text(
                    text = "📈 Health Trends",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Activity Chart
            item {
                ChartCard(
                    title = "🏃 Activity Levels",
                    subtitle = "Steps and active minutes over time",
                    color = Color(0xFF00BCD4)
                ) {
                    ActivityChart(
                        data = uiState.activityData,
                        modifier = Modifier.height(200.dp)
                    )
                }
            }

            // Heart Rate Chart
            item {
                ChartCard(
                    title = "❤️ Heart Rate Variability",
                    subtitle = "BPM trends with normal range indicators",
                    color = Color(0xFFFF7043)
                ) {
                    HeartRateChart(
                        data = uiState.heartRateData,
                        modifier = Modifier.height(200.dp)
                    )
                }
            }

            // Mood & Sleep Chart
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChartCard(
                        title = "😊 Mood Score",
                        subtitle = "Emotional well-being",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    ) {
                        MoodChart(
                            data = uiState.moodData,
                            modifier = Modifier.height(150.dp)
                        )
                    }

                    ChartCard(
                        title = "😴 Sleep Quality",
                        subtitle = "Hours & quality",
                        color = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f)
                    ) {
                        SleepChart(
                            data = uiState.sleepData,
                            modifier = Modifier.height(150.dp)
                        )
                    }
                }
            }

            // Health Events Timeline
            item {
                Text(
                    text = "📅 Health Timeline",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Timeline Items
            items(uiState.healthEvents) { event ->
                HealthEventCard(
                    event = event,
                    onEventClick = { /* Navigate to event details */ }
                )
            }

            // Export Options
            item {
                ExportOptionsCard(
                    onExportPdf = {
                        scope.launch {
                            viewModel.exportHealthReport()
                        }
                    },
                    onShareReport = {
                        scope.launch {
                            viewModel.shareHealthReport()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📅 Time Period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TimeRange.values()) { range ->
                    FilterChip(
                        onClick = { onRangeSelected(range) },
                        label = { Text(range.displayName) },
                        selected = selectedRange == range,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00BCD4),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HealthOverviewCards(uiState: HealthHistoryUiState) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            OverviewCard(
                title = "Avg Heart Rate",
                value = "${uiState.averageHeartRate} BPM",
                trend = uiState.heartRateTrend,
                color = Color(0xFFFF7043),
                icon = "❤️"
            )
        }

        item {
            OverviewCard(
                title = "Daily Steps",
                value = "${uiState.averageSteps}",
                trend = uiState.activityTrend,
                color = Color(0xFF00BCD4),
                icon = "🏃"
            )
        }

        item {
            OverviewCard(
                title = "Mood Score",
                value = "${uiState.averageMood}/10",
                trend = uiState.moodTrend,
                color = Color(0xFF4CAF50),
                icon = "😊"
            )
        }

        item {
            OverviewCard(
                title = "Sleep Quality",
                value = "${uiState.averageSleep}h",
                trend = uiState.sleepTrend,
                color = Color(0xFF9C27B0),
                icon = "😴"
            )
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String,
    trend: HealthTrend,
    color: Color,
    icon: String
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp
                )

                Icon(
                    imageVector = when (trend) {
                        HealthTrend.IMPROVING -> Icons.Default.TrendingUp
                        HealthTrend.DECLINING -> Icons.Default.TrendingDown
                        HealthTrend.STABLE -> Icons.Default.TrendingFlat
                    },
                    contentDescription = trend.name,
                    tint = when (trend) {
                        HealthTrend.IMPROVING -> Color(0xFF4CAF50)
                        HealthTrend.DECLINING -> Color(0xFFFF5722)
                        HealthTrend.STABLE -> Color(0xFF757575)
                    },
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
fun ActivityChart(
    data: List<ActivityDataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height
        val maxSteps = data.maxOfOrNull { it.steps }?.toFloat() ?: 1f

        if (data.isNotEmpty()) {
            val path = Path()
            val stepSize = width / (data.size - 1).coerceAtLeast(1)

            data.forEachIndexed { index, point ->
                val x = index * stepSize
                val y = height - (point.steps / maxSteps * height)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw data points
                drawCircle(
                    color = Color(0xFF00BCD4),
                    radius = 6f,
                    center = Offset(x, y)
                )
            }

            // Draw the line
            drawPath(
                path = path,
                color = Color(0xFF00BCD4),
                style = Stroke(width = 4f)
            )
        }
    }
}

@Composable
fun HeartRateChart(
    data: List<HeartRateDataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height

        // Draw normal range background
        val normalRangeTop = height * 0.2f
        val normalRangeBottom = height * 0.7f

        drawRect(
            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
            topLeft = Offset(0f, normalRangeTop),
            size = androidx.compose.ui.geometry.Size(width, normalRangeBottom - normalRangeTop)
        )

        if (data.isNotEmpty()) {
            val maxHeartRate = data.maxOfOrNull { it.heartRate }?.toFloat() ?: 140f
            val minHeartRate = data.minOfOrNull { it.heartRate }?.toFloat() ?: 60f
            val range = maxHeartRate - minHeartRate

            val path = Path()
            val stepSize = width / (data.size - 1).coerceAtLeast(1)

            data.forEachIndexed { index, point ->
                val x = index * stepSize
                val normalizedValue = (point.heartRate - minHeartRate) / range
                val y = height - (normalizedValue * height)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Color based on heart rate range
                val pointColor = when {
                    point.heartRate < 60 -> Color(0xFF2196F3) // Low - Blue
                    point.heartRate <= 100 -> Color(0xFF4CAF50) // Normal - Green
                    point.heartRate <= 120 -> Color(0xFFFF9800) // Elevated - Orange
                    else -> Color(0xFFFF5722) // High - Red
                }

                drawCircle(
                    color = pointColor,
                    radius = 5f,
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = Color(0xFFFF7043),
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun MoodChart(
    data: List<MoodDataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height

        if (data.isNotEmpty()) {
            val stepSize = width / data.size

            data.forEachIndexed { index, point ->
                val x = index * stepSize + stepSize / 2
                val barHeight = (point.moodScore / 10f) * height
                val y = height - barHeight

                val color = when {
                    point.moodScore >= 8 -> Color(0xFF4CAF50) // Excellent - Green
                    point.moodScore >= 6 -> Color(0xFF8BC34A) // Good - Light Green
                    point.moodScore >= 4 -> Color(0xFFFF9800) // Fair - Orange
                    else -> Color(0xFFFF5722) // Poor - Red
                }

                drawRect(
                    color = color,
                    topLeft = Offset(x - stepSize / 4, y),
                    size = androidx.compose.ui.geometry.Size(stepSize / 2, barHeight)
                )
            }
        }
    }
}

@Composable
fun SleepChart(
    data: List<SleepDataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height

        if (data.isNotEmpty()) {
            val maxHours = 12f
            val stepSize = width / data.size

            data.forEachIndexed { index, point ->
                val x = index * stepSize + stepSize / 2
                val barHeight = (point.sleepHours / maxHours) * height
                val y = height - barHeight

                val color = when (point.quality) {
                    "EXCELLENT" -> Color(0xFF4CAF50)
                    "GOOD" -> Color(0xFF8BC34A)
                    "FAIR" -> Color(0xFFFF9800)
                    else -> Color(0xFFFF5722)
                }

                drawRect(
                    color = color,
                    topLeft = Offset(x - stepSize / 4, y),
                    size = androidx.compose.ui.geometry.Size(stepSize / 2, barHeight)
                )
            }
        }
    }
}

@Composable
fun HealthEventCard(
    event: HealthEvent,
    onEventClick: () -> Unit
) {
    Card(
        onClick = onEventClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Icon
            Card(
                modifier = Modifier.size(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = event.color.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.icon,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Event Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = event.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Severity Indicator
            if (event.severity != EventSeverity.NORMAL) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (event.severity) {
                            EventSeverity.HIGH -> Color(0xFFFF5722)
                            EventSeverity.MEDIUM -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = event.severity.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun ExportOptionsCard(
    onExportPdf: () -> Unit,
    onShareReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📄 Export & Share",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00BCD4)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Export PDF",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export PDF")
                }

                Button(
                    onClick = onShareReport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Report",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Report")
                }
            }
        }
    }
}