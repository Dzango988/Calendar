package com.calendar.importantdates

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.calendar.importantdates.data.db.AppDatabase
import com.calendar.importantdates.data.repository.DateRepository

class App : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { DateRepository(database) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Напоминания о важных датах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о предстоящих важных событиях"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "important_dates_channel"
    }
}
