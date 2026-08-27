package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "development_activity_logs")
data class DevelopmentActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,
    val title: String,
    val description: String,
    val category: String, // CICD, ADB_HARDWARE, CORE_ENGINE, TESTING, FIXES
    val agentTag: String,
    val affectedFiles: String, // Comma-separated or JSON list
    val rollbackInstruction: String,
    val executionDetails: String = ""
)
