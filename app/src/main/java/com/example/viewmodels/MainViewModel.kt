package com.example.viewmodels

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.ContactRepository
import com.example.database.AlertHistoryRepository
import com.example.models.AlertHistory
import com.example.models.PriorityContact
import com.example.services.NotificationMonitorService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val contactRepo = ContactRepository(db.contactDao())
    private val historyRepo = AlertHistoryRepository(db.alertHistoryDao())

    // Shared Preferences for Onboarding Completed tracking
    private val prefs = application.getSharedPreferences("wake_alert_preferences", Context.MODE_PRIVATE)
    
    var isOnboardingCompleted by mutableStateOf(prefs.getBoolean("onboarding_done", false))
        private set

    // Flow for prioritizing contacts
    val contacts: StateFlow<List<PriorityContact>> = contactRepo.allContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flow for alerted history
    val alertHistory: StateFlow<List<AlertHistory>> = historyRepo.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_done", true).apply()
        isOnboardingCompleted = true
    }

    // Reset onboarding for settings or development testing
    fun resetOnboarding() {
        prefs.edit().putBoolean("onboarding_done", false).apply()
        isOnboardingCompleted = false
    }

    fun addContact(contact: PriorityContact) {
        viewModelScope.launch {
            contactRepo.insertContact(contact)
        }
    }

    fun updateContact(contact: PriorityContact) {
        viewModelScope.launch {
            contactRepo.updateContact(contact)
        }
    }

    fun deleteContact(contact: PriorityContact) {
        viewModelScope.launch {
            contactRepo.deleteContact(contact)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
        }
    }

    fun stopActiveAlert() {
        NotificationMonitorService.stopActiveAlert(getApplication())
    }
}
