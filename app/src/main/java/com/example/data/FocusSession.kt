package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // Work, Break
    val duration: Int, // in seconds
    val completedAt: Long = System.currentTimeMillis()
)
