package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TimeLeftViewModel
import com.example.ui.components.StatCard
import com.example.ui.theme.*
import com.example.utils.TimeLeftUtils
import kotlinx.coroutines.delay

@Composable
fun StatsScreen(
    viewModel: TimeLeftViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val todaysTasks by viewModel.todaysTasks.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaysSessions.collectAsStateWithLifecycle()
    
    val completedCount by viewModel.tasksCompletedCount.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalTasksCount.collectAsStateWithLifecycle()
    val focusMinutesVal by viewModel.focusMinutes.collectAsStateWithLifecycle()
    val targetScore by viewModel.productivityScore.collectAsStateWithLifecycle()

    // Count today's focus session count
    val totalPomodoroCount = todaySessions.count { it.type == "Work" }

    // Day Progress calculations
    var dayProgressInfo by remember { mutableStateOf(TimeLeftUtils.getDayProgress(settings.dailyStartHour, settings.dailyEndHour)) }
    LaunchedEffect(settings) {
        while (true) {
            dayProgressInfo = TimeLeftUtils.getDayProgress(settings.dailyStartHour, settings.dailyEndHour)
            delay(5000) // Ticks slower because it is the stats screen
        }
    }

    // SECTION B: Animated count-up score
    var animatedScore by remember { mutableStateOf(0) }
    LaunchedEffect(targetScore) {
        // Animate count-up safely
        animatedScore = 0
        if (targetScore > 0) {
            val step = (targetScore / 20).coerceAtLeast(1)
            while (animatedScore < targetScore) {
                delay(15)
                animatedScore = (animatedScore + step).coerceAtMost(targetScore)
            }
        }
    }

    // Process tasks by category for Section E chart
    val categoryCounts = remember(todaysTasks) {
        todaysTasks.groupBy { it.category }.mapValues { it.value.size }
    }
    val hasChartData = categoryCounts.isNotEmpty()

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

        // SECTION A: TITLE TITLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Stats",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION B: PRODUCTIVITY SCORE CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRODUCTIVITY SCORE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$animatedScore",
                color = Accent,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 80.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "out of 100",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION C: DAY BREAKDOWN CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "DAY BREAKDOWN",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Elapsed hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Accent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Elapsed Focus Window",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = String.format("%.1fh", dayProgressInfo.elapsedHours),
                    color = Accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Remaining hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(TextDim)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Remaining Focus Window",
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = String.format("%.1fh", dayProgressInfo.remainingHours),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION D — 2X2 STAT CARDS GRID
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Tasks Done",
                    value = "$completedCount/$totalCount",
                    subtext = "completed tasks",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Pomodoros",
                    value = "$totalPomodoroCount",
                    subtext = "completed sessions",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Focus Time",
                    value = TimeLeftUtils.formatHoursMinutes(focusMinutesVal),
                    subtext = "total active minutes",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Time Left",
                    value = String.format("%02dh:%02dm", dayProgressInfo.hoursLeft, dayProgressInfo.minutesLeftInHour),
                    subtext = "focus hours left",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION E — CATEGORY BAR CHART
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "TASKS BY CATEGORY",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (!hasChartData) {
                Text(
                    text = "Add and complete tasks to display category chart metrics.",
                    color = TextDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            } else {
                val maxCount = categoryCounts.values.maxOrNull() ?: 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val categoriesList = listOf("Work", "Personal", "Health", "Learning")
                    categoriesList.forEach { cat ->
                        val count = categoryCounts[cat] ?: 0
                        val barColor = getCategoryColor(cat)
                        
                        // Calculating relative height (max 110dp)
                        val heightDp = if (maxCount > 0) {
                            (count.toFloat() / maxCount.toFloat() * 110.dp.value).dp.coerceAtLeast(4.dp)
                        } else {
                            4.dp
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            if (count > 0) {
                                Text(
                                    text = "$count",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            // Color bar
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(heightDp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(if (count > 0) barColor else Border)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.take(4),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SECTION F — SETTINGS PANEL CARD
        var isSettingsExpanded by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSettingsExpanded = !isSettingsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APP CONFIGURATION",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isSettingsExpanded) "COLLAPSE" else "EXPAND",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            if (isSettingsExpanded) {
                Spacer(modifier = Modifier.height(18.dp))

                // Work duration chooser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Focus Duration", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Length of each focusing interval", color = TextMuted, fontSize = 11.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Minus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.workDuration > 5) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration - 5,
                                            breakDuration = settings.breakDuration,
                                            startHour = settings.dailyStartHour,
                                            endHour = settings.dailyEndHour
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "—", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${settings.workDuration}m",
                            color = Accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                        // Plus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.workDuration < 120) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration + 5,
                                            breakDuration = settings.breakDuration,
                                            startHour = settings.dailyStartHour,
                                            endHour = settings.dailyEndHour
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Break duration chooser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Break Duration", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Length of rest breaks", color = TextMuted, fontSize = 11.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Minus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.breakDuration > 1) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration,
                                            breakDuration = settings.breakDuration - 1,
                                            startHour = settings.dailyStartHour,
                                            endHour = settings.dailyEndHour
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "—", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${settings.breakDuration}m",
                            color = Accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                        // Plus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.breakDuration < 30) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration,
                                            breakDuration = settings.breakDuration + 1,
                                            startHour = settings.dailyStartHour,
                                            endHour = settings.dailyEndHour
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Daily Start Hour chooser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Day Start Window", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Hour when focus window starts", color = TextMuted, fontSize = 11.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Minus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.dailyStartHour > 0) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration,
                                            breakDuration = settings.breakDuration,
                                            startHour = settings.dailyStartHour - 1,
                                            endHour = settings.dailyEndHour
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "—", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        val meridiemLabel = if (settings.dailyStartHour < 12) {
                            "${if (settings.dailyStartHour == 0) 12 else settings.dailyStartHour} AM"
                        } else {
                            "${if (settings.dailyStartHour == 12) 12 else settings.dailyStartHour - 12} PM"
                        }
                        Text(
                            text = meridiemLabel,
                            color = Accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(54.dp),
                            textAlign = TextAlign.Center
                        )
                        // Plus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.dailyStartHour < settings.dailyEndHour - 2) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration,
                                            breakDuration = settings.breakDuration,
                                            startHour = settings.dailyStartHour + 1,
                                            endHour = settings.dailyEndHour
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Daily End Hour chooser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Day End Window", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Hour when focus window closes", color = TextMuted, fontSize = 11.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Minus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.dailyEndHour > settings.dailyStartHour + 2) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration,
                                            breakDuration = settings.breakDuration,
                                            startHour = settings.dailyStartHour,
                                            endHour = settings.dailyEndHour - 1
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "—", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        val endMeridiemLabel = when {
                            settings.dailyEndHour == 24 -> "12 AM"
                            settings.dailyEndHour < 12 -> "${if (settings.dailyEndHour == 0) 12 else settings.dailyEndHour} AM"
                            else -> "${if (settings.dailyEndHour == 12) 12 else settings.dailyEndHour - 12} PM"
                        }
                        Text(
                            text = endMeridiemLabel,
                            color = Accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(54.dp),
                            textAlign = TextAlign.Center
                        )
                        // Plus Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceAlt)
                                .clickable {
                                    if (settings.dailyEndHour < 24) {
                                        viewModel.updateSettings(
                                            workDuration = settings.workDuration,
                                            breakDuration = settings.breakDuration,
                                            startHour = settings.dailyStartHour,
                                            endHour = settings.dailyEndHour + 1
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
