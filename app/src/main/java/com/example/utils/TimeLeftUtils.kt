package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

data class TimeOfDayInfo(
    val greeting: String,
    val period: String,
    val emoji: String
)

data class DayProgressInfo(
    val percentage: Float, // 0.0 to 1.0
    val elapsedHours: Float,
    val remainingHours: Float,
    val hoursLeft: Int,
    val minutesLeftInHour: Int
)

data class Quote(
    val text: String,
    val author: String
)

object TimeLeftUtils {

    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun formatHoursMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h ${minutes}m"
    }

    fun getTimeOfDay(): TimeOfDayInfo {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..5 -> TimeOfDayInfo("Good Night", "Night", "🌙")
            in 6..11 -> TimeOfDayInfo("Good Morning", "Morning", "🌅")
            in 12..16 -> TimeOfDayInfo("Good Afternoon", "Afternoon", "☀️")
            in 17..20 -> TimeOfDayInfo("Good Evening", "Evening", "🌆")
            else -> TimeOfDayInfo("Good Night", "Night", "🌙")
        }
    }

    fun getDayProgress(startHour: Int = 6, endHour: Int = 24): DayProgressInfo {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)

        val totalMinutesInWindow = (endHour - startHour) * 60
        val currentMinutesInDay = (currentHour * 60) + currentMinute + (currentSecond / 60.0f)

        val startMinutes = startHour * 60
        val endMinutes = endHour * 60

        val elapsedMinutes = when {
            currentMinutesInDay < startMinutes -> 0.0f
            currentMinutesInDay > endMinutes -> totalMinutesInWindow.toFloat()
            else -> currentMinutesInDay - startMinutes
        }

        val percentage = if (totalMinutesInWindow > 0) {
            (elapsedMinutes / totalMinutesInWindow).coerceIn(0.0f, 1.0f)
        } else {
            0.0f
        }

        val elapsedHours = elapsedMinutes / 60.0f
        val remainingHours = (totalMinutesInWindow.toFloat() - elapsedMinutes) / 60.0f

        val totalWindowSeconds = (endHour * 3600) - (startHour * 3600)
        val currentSecondsInDay = (currentHour * 3600) + (currentMinute * 60) + currentSecond
        val startSeconds = startHour * 3600
        val endSeconds = endHour * 3600

        val remainingSeconds = when {
            currentSecondsInDay < startSeconds -> totalWindowSeconds
            currentSecondsInDay >= endSeconds -> 0
            else -> endSeconds - currentSecondsInDay
        }

        val hoursLeft = remainingSeconds / 3600
        val minutesLeftInHour = (remainingSeconds % 3600) / 60

        return DayProgressInfo(
            percentage = percentage,
            elapsedHours = elapsedHours,
            remainingHours = remainingHours,
            hoursLeft = hoursLeft,
            minutesLeftInHour = minutesLeftInHour
        )
    }

    fun getCurrentTimeLabel(): String {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getDayName(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val checkDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
    }

    private val quotes = listOf(
        Quote("Lost time is never found again.", "Benjamin Franklin"),
        Quote("Focus on being productive instead of busy.", "Tim Ferriss"),
        Quote("Your time is limited, don't waste it.", "Steve Jobs"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Either you run the day or the day runs you.", "Jim Rohn")
    )

    fun getMotivationalQuote(): Quote {
        val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[dayIndex % quotes.size]
    }

    fun calculateProductivityScore(tasksCompleted: Int, totalTasks: Int, focusMinutes: Int): Int {
        val taskScore = if (totalTasks > 0) {
            (tasksCompleted.toFloat() / totalTasks.toFloat()) * 50.0f
        } else {
            0.0f
        }
        val focusScore = (min(focusMinutes, 120).toFloat() / 120.0f) * 50.0f
        return (taskScore + focusScore).toInt().coerceIn(0, 100)
    }
}
