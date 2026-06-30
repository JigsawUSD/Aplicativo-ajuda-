package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.models.PriorityContact
import com.example.services.NotificationMonitorService
import com.example.services.WakeAlertState
import com.example.ui.ActiveAlertOverlay
import com.example.ui.AddEditContactScreen
import com.example.ui.HomeScreen
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodels.MainViewModel

sealed class Screen {
    object Home : Screen()
    data class AddEditContact(val contact: PriorityContact? = null) : Screen()
}

class MainActivity : ComponentActivity() {

    private var mainViewModel: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                mainViewModel = viewModel

                val contacts by viewModel.contacts.collectAsStateWithLifecycle()
                val history by viewModel.alertHistory.collectAsStateWithLifecycle()

                val isAlerting by WakeAlertState.isAlerting.collectAsStateWithLifecycle()
                val activeContact by WakeAlertState.activeAlertContact.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Main Application Flow
                        if (!viewModel.isOnboardingCompleted) {
                            OnboardingScreen(
                                onComplete = {
                                    viewModel.completeOnboarding()
                                    currentScreen = Screen.Home
                                }
                            )
                        } else {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "screenTransition"
                            ) { screen ->
                                when (screen) {
                                    is Screen.Home -> {
                                        HomeScreen(
                                            contacts = contacts,
                                            history = history,
                                            onAddContact = {
                                                currentScreen = Screen.AddEditContact(null)
                                            },
                                            onEditContact = { contact ->
                                                currentScreen = Screen.AddEditContact(contact)
                                            },
                                            onToggleContactActive = { contact, isActive ->
                                                viewModel.updateContact(contact.copy(isActive = isActive))
                                            },
                                            onClearHistory = {
                                                viewModel.clearHistory()
                                            },
                                            onResetOnboarding = {
                                                viewModel.resetOnboarding()
                                            }
                                        )
                                    }
                                    is Screen.AddEditContact -> {
                                        AddEditContactScreen(
                                            contact = screen.contact,
                                            onBack = {
                                                currentScreen = Screen.Home
                                            },
                                            onSave = { savedContact ->
                                                if (screen.contact == null) {
                                                    viewModel.addContact(savedContact)
                                                } else {
                                                    viewModel.updateContact(savedContact)
                                                }
                                                currentScreen = Screen.Home
                                            },
                                            onDelete = { contactToDelete ->
                                                viewModel.deleteContact(contactToDelete)
                                                currentScreen = Screen.Home
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Full-screen overlay for active alerts, bypassing other UI layers
                        AnimatedVisibility(
                            visible = isAlerting,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ActiveAlertOverlay(
                                contact = activeContact,
                                onStopAlert = {
                                    viewModel.stopActiveAlert()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra("stop_alert_action", false)) {
            NotificationMonitorService.stopActiveAlert(this)
            mainViewModel?.stopActiveAlert()
        }
    }
}
