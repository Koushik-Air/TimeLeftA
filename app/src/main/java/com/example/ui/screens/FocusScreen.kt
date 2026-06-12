package com.example.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kotlinx.coroutines.launch

@Composable
fun FocusScreen(
    viewModel: TimeLeftViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaysSessions.collectAsStateWithLifecycle()

    // Timer state
    var isWorkMode by remember { mutableStateOf(true) } // true = Work, false = Break
    var isRunning by remember { mutableStateOf(false) }

    val defaultWorkDurationSeconds = remember(settings) { settings.workDuration * 60 }
    val defaultBreakDurationSeconds = remember(settings) { settings.breakDuration * 60 }

    val totalDurationSeconds = if (isWorkMode) defaultWorkDurationSeconds else defaultBreakDurationSeconds
    var secondsLeft by remember(isWorkMode, settings) { mutableStateOf(totalDurationSeconds) }

    // Count today's Pomodoro (Work type) completions
    val pomodoroCount = remember(todaySessions) {
        todaySessions.count { it.type == "Work" }
    }

    // Timer Ticker Effect
    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        } else if (isRunning && secondsLeft == 0) {
            // Trigger haptic and audio tone
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            try {
                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).startTone(ToneGenerator.TONE_PROP_BEEP, 250)
            } catch (e: Exception) {
                // Ignore audio failures
            }

            // Save completed focus session
            val sessionType = if (isWorkMode) "Work" else "Break"
            viewModel.addFocusSession(sessionType, totalDurationSeconds)

            // Auto cycle transition after 1s
            delay(1000)
            isWorkMode = !isWorkMode
            secondsLeft = if (isWorkMode) defaultWorkDurationSeconds else defaultBreakDurationSeconds
            isRunning = true // Auto-run next cycle
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
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
                text = "Focus",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION B — WORK/BREAK TOGGLE
        Row(
            modifier = Modifier
                .width(260.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(22.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // WORK Toggle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isWorkMode) Accent else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = false
                        isWorkMode = true
                        secondsLeft = defaultWorkDurationSeconds
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WORK",
                    color = if (isWorkMode) AccentText else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // BREAK Toggle
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (!isWorkMode) Accent else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = false
                        isWorkMode = false
                        secondsLeft = defaultBreakDurationSeconds
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BREAK",
                    color = if (!isWorkMode) AccentText else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // SECTION C — CIRCULAR TIMER
        val progressFraction = if (totalDurationSeconds > 0) secondsLeft.toFloat() / totalDurationSeconds.toFloat() else 0f
        CircularProgress(
            size = 260.dp,
            progress = progressFraction,
            strokeWidth = 10.dp,
            color = Accent,
            trackColor = Border
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = TimeLeftUtils.formatTime(secondsLeft),
                    color = TextPrimary,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isWorkMode) "FOCUSING" else "BREAKING",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // SECTION D — CONTROL BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .border(1.dp, Border, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = false
                        secondsLeft = totalDurationSeconds
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Reset timer",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Start/Pause Button (Takes rest of space)
            val activeLabel = if (isRunning) "PAUSE" else "START"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Accent)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRunning = !isRunning
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = activeLabel,
                        tint = AccentText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeLabel,
                        color = AccentText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // SECTION E — SESSIONS CARD
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
                    text = "Sessions Today",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Sessions Count badge
                Box(
                    modifier = Modifier
                        .background(Accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$pomodoroCount completed",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 dots cycle indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Calculation: dots filled = pomodoroCount % 4 (or 4 if count > 0 and count % 4 == 0)
                val activeDots = when {
                    pomodoroCount == 0 -> 0
                    pomodoroCount % 4 == 0 -> 4
                    else -> pomodoroCount % 4
                }

                for (i in 1..4) {
                    val isFilled = i <= activeDots
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFilled) Accent else SurfaceAlt)
                            .border(1.dp, if (isFilled) Accent else Border, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFilled) "★" else "☆",
                            color = if (isFilled) AccentText else TextDim,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
