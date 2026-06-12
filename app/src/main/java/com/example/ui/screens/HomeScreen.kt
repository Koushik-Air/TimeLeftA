package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TimeLeftViewModel
import com.example.ui.components.CircularProgress
import com.example.ui.theme.*
import com.example.utils.TimeLeftUtils
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: TimeLeftViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val completedCount by viewModel.tasksCompletedCount.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalTasksCount.collectAsStateWithLifecycle()

    // Real-time ticking state variables
    var timeOfDayInfo by remember { mutableStateOf(TimeLeftUtils.getTimeOfDay()) }
    var dayProgressInfo by remember { mutableStateOf(TimeLeftUtils.getDayProgress(settings.dailyStartHour, settings.dailyEndHour)) }
    var currentTimeLabel by remember { mutableStateOf(TimeLeftUtils.getCurrentTimeLabel()) }
    var dayName by remember { mutableStateOf(TimeLeftUtils.getDayName()) }
    val quoteValue = remember { TimeLeftUtils.getMotivationalQuote() }

    LaunchedEffect(settings) {
        while (true) {
            timeOfDayInfo = TimeLeftUtils.getTimeOfDay()
            dayProgressInfo = TimeLeftUtils.getDayProgress(settings.dailyStartHour, settings.dailyEndHour)
            currentTimeLabel = TimeLeftUtils.getCurrentTimeLabel()
            dayName = TimeLeftUtils.getDayName()
            delay(1000)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // SECTION A — GREETING HEADER
        Text(
            text = timeOfDayInfo.greeting.uppercase(),
            color = Accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .background(Surface, RoundedCornerShape(20.dp))
                .border(1.dp, Border, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${timeOfDayInfo.emoji} ${timeOfDayInfo.period}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION B — CIRCULAR HOURS RING
        CircularProgress(
            size = 240.dp,
            progress = dayProgressInfo.percentage,
            strokeWidth = 12.dp,
            color = Accent,
            trackColor = Border
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${dayProgressInfo.hoursLeft}",
                    color = TextPrimary,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 58.sp
                )
                Text(
                    text = "HOURS LEFT",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%02dh:%02dm", dayProgressInfo.hoursLeft, dayProgressInfo.minutesLeftInHour),
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // SECTION C — DAY PROGRESS CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day Progress",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(dayProgressInfo.percentage * 100).toInt()}%",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Progress Bar Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(dayProgressInfo.percentage)
                        .height(6.dp)
                        .background(Accent)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            // Time markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val markers = listOf("6A", "9A", "12P", "3P", "6P", "9P")
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                
                markers.forEachIndexed { index, name ->
                    val markerHour = 6 + (index * 3)
                    val color = when {
                        currentHour == markerHour -> Accent
                        currentHour > markerHour -> Accent.copy(alpha = 0.45f)
                        else -> TextDim
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION D — WIDGET ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tasks Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "TASKS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$completedCount",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "/$totalCount done",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp)
                    )
                }
            }

            // Now Card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Surface, RoundedCornerShape(16.dp))
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "NOW",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentTimeLabel,
                    color = Accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = dayName,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION E — QUOTE CARD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold Left Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Accent)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "\"${quoteValue.text}\"",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— ${quoteValue.author}",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
