package com.example.services

import com.example.models.PriorityContact
import kotlinx.coroutines.flow.MutableStateFlow

object WakeAlertState {
    val activeAlertContact = MutableStateFlow<PriorityContact?>(null)
    val isAlerting = MutableStateFlow(false)
}
