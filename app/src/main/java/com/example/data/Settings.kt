package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val workDuration: Int = 25, // in minutes
    val breakDuration: Int = 5,  // in minutes
    val dailyStartHour: Int = 6,
    val dailyEndHour: Int = 24
)
