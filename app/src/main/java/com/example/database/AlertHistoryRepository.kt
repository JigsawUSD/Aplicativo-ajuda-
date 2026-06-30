package com.example.database

import com.example.models.AlertHistory
import kotlinx.coroutines.flow.Flow

class AlertHistoryRepository(private val alertHistoryDao: AlertHistoryDao) {
    val allHistory: Flow<List<AlertHistory>> = alertHistoryDao.getAllHistory()

    suspend fun insertHistory(history: AlertHistory) = alertHistoryDao.insertHistory(history)

    suspend fun clearHistory() = alertHistoryDao.clearHistory()
}
