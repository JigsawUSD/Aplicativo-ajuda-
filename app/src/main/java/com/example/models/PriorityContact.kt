package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "priority_contacts")
data class PriorityContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String? = null,
    val startTime: String = "00:00", // "HH:mm" format
    val endTime: String = "23:59",   // "HH:mm" format
    val daysOfWeek: String = "1,2,3,4,5,6,7", // comma separated day indices: 1 = Monday, ..., 7 = Sunday
    val alertType: String = "SOUND_VIB",    // "SOUND_VIB", "SOUND_ONLY", "VIBRATE_ONLY"
    val intensity: String = "HIGH",          // "HIGH", "MEDIUM", "LOW"
    val isActive: Boolean = true
)
