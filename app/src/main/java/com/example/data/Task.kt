package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Work, Health, Learning, Personal
    val completed: Boolean = false,
    val pomodorosUsed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
