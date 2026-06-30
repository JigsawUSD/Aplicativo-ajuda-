package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_history")
data class AlertHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val appName: String, // WhatsApp, SMS, Telegram, etc.
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
