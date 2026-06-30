package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.database.AppDatabase
import com.example.models.AlertHistory
import com.example.models.PriorityContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationMonitorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var alertEngine: WakeAlertEngine? = null

    companion object {
        private const val TAG = "NotificationMonitor"
        private const val CHANNEL_ID = "wake_alert_urgent_channel"
        private const val NOTIFICATION_ID = 9999
        
        @Volatile
        private var isServiceRunning = false

        // Holds instance to allow stopping alerts from UI
        @Volatile
        private var instance: NotificationMonitorService? = null

        fun isRunning(): Boolean = isServiceRunning

        fun stopActiveAlert(context: Context) {
            instance?.stopAlert() ?: run {
                // Fallback if service isn't active/bound: stop using a temporary engine
                Log.w(TAG, "Instance is null, stopping alert via temporary engine")
                val tempEngine = WakeAlertEngine(context)
                tempEngine.stopAlert()
            }
            // Reset global state
            WakeAlertState.isAlerting.value = false
            WakeAlertState.activeAlertContact.value = null

            // Dismiss the alert notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(NOTIFICATION_ID)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        isServiceRunning = true
        alertEngine = WakeAlertEngine(applicationContext)
        createNotificationChannel()
        Log.d(TAG, "Service Created")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        if (instance == this) {
            instance = null
        }
        alertEngine?.stopAlert()
        Log.d(TAG, "Service Destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val appPackage = sbn.packageName

        // Ignore notifications from our own application
        if (appPackage == packageName) return

        serviceScope.launch {
            // Get app display name
            val pm = packageManager
            val appName = try {
                pm.getApplicationLabel(pm.getApplicationInfo(appPackage, 0)).toString()
            } catch (e: Exception) {
                appPackage.substringAfterLast('.')
            }

            Log.d(TAG, "Received notification: App=$appName, Package=$appPackage, Title=$title, Content=$text")

            val db = AppDatabase.getDatabase(applicationContext)
            val activeContacts = db.contactDao().getActiveContacts()

            for (contact in activeContacts) {
                // Matches if contact name is present in the notification title/sender
                val matchByName = title.contains(contact.name, ignoreCase = true) ||
                        text.contains(contact.name, ignoreCase = true)

                if (matchByName) {
                    if (isRuleSatisfied(contact)) {
                        triggerAlert(contact, appName, title, text, db)
                        break
                    }
                }
            }
        }
    }

    /**
     * Checks if current time and day matches the prioritized contact rule
     */
    private fun isRuleSatisfied(contact: PriorityContact): Boolean {
        val calendar = Calendar.getInstance()
        
        // 1. Day of the week check
        val dayOfWeekCalendar = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekIndex = when (dayOfWeekCalendar) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val daysList = contact.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (daysList.isNotEmpty() && !daysList.contains(dayOfWeekIndex)) {
            Log.d(TAG, "Contact match found but day $dayOfWeekIndex is not prioritized ($daysList)")
            return false
        }

        // 2. Time check
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTimeMinutes = currentHour * 60 + currentMinute

        val startParts = contact.startTime.split(":")
        val endParts = contact.endTime.split(":")
        
        if (startParts.size != 2 || endParts.size != 2) return true // default to active if parse fails

        val startMinutes = startParts[0].toIntOrNull()?.let { it * 60 } ?: 0 + (startParts[1].toIntOrNull() ?: 0)
        val endMinutes = endParts[0].toIntOrNull()?.let { it * 60 } ?: 0 + (endParts[1].toIntOrNull() ?: 0)

        val isTimeValid = if (startMinutes <= endMinutes) {
            currentTimeMinutes in startMinutes..endMinutes
        } else {
            // Overnight schedule, e.g. 22:00 to 06:00
            currentTimeMinutes >= startMinutes || currentTimeMinutes <= endMinutes
        }

        if (!isTimeValid) {
            Log.d(TAG, "Contact match found but current time $currentHour:$currentMinute is outside of schedule (${contact.startTime} - ${contact.endTime})")
            return false
        }

        return true
    }

    private suspend fun triggerAlert(
        contact: PriorityContact,
        appName: String,
        title: String,
        text: String,
        db: AppDatabase
    ) {
        Log.i(TAG, "🔥 ALERTA DISPARADO PARA O CONTATO ${contact.name} 🔥")

        // Set global alert state
        WakeAlertState.activeAlertContact.value = contact
        WakeAlertState.isAlerting.value = true

        // Insert into history log
        val history = AlertHistory(
            contactName = contact.name,
            appName = appName,
            title = title,
            content = text,
            timestamp = System.currentTimeMillis()
        )
        db.alertHistoryDao().insertHistory(history)

        // Start hardware triggers
        alertEngine?.startAlert(contact.intensity, contact.alertType)

        // Post a loud priority Heads-up notification
        showNotification(contact)

        // Launch / bring application UI to the foreground
        try {
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("active_alert", true)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not launch MainActivity directly from background", e)
        }
    }

    private fun stopAlert() {
        alertEngine?.stopAlert()
    }

    private fun showNotification(contact: PriorityContact) {
        val stopIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("stop_alert_action", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val stopPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("active_alert", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val openPendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("WAKEALERT URGENTE")
            .setContentText("Notificação prioritária recebida de: ${contact.name}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(openPendingIntent, true) // Heads-up banner
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "PARAR ALERTA", stopPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Mensagem importante de ${contact.name}. Toque para abrir ou desligar o alarme.")
            )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas Urgentes WakeAlert"
            val descriptionText = "Canal utilizado para reproduzir os alarmes de contatos críticos."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setBypassDnd(true) // Attempt DND bypass for alarms
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
