package com.calendar.importantdates

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.calendar.importantdates.data.db.AppDatabase
import com.calendar.importantdates.data.db.DateCategory
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { DateRepository(database) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        seedHolidaysIfNeeded()
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
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun seedHolidaysIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_HOLIDAYS_SEEDED, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            val holidays = listOf(
                ImportantDate(title = "Новый год", day = 1, month = 1, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#FFD93D",
                    description = "С Новым годом! 🎄"),
                ImportantDate(title = "Рождество", day = 7, month = 1, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#FFD93D",
                    description = "Православное Рождество"),
                ImportantDate(title = "День защитника Отечества", day = 23, month = 2, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#4D96FF",
                    description = "23 февраля 🎖️"),
                ImportantDate(title = "Международный женский день", day = 8, month = 3, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#EC4899",
                    description = "8 марта 🌹"),
                ImportantDate(title = "День труда", day = 1, month = 5, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#6BCB77",
                    description = "Праздник весны и труда"),
                ImportantDate(title = "День Победы", day = 9, month = 5, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#FF6B6B",
                    description = "9 мая 🎖️"),
                ImportantDate(title = "День России", day = 12, month = 6, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#4D96FF",
                    description = "С Днём России! 🇷🇺"),
                ImportantDate(title = "День народного единства", day = 4, month = 11, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#FF8E53",
                    description = "4 ноября"),
                ImportantDate(title = "День Конституции", day = 12, month = 12, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#4D96FF",
                    description = "12 декабря"),
                ImportantDate(title = "Международный день детей", day = 1, month = 6, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#FFD93D",
                    description = "1 июня 🎈"),
                ImportantDate(title = "День Святого Валентина", day = 14, month = 2, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#EC4899",
                    description = "14 февраля ❤️"),
                ImportantDate(title = "Хэллоуин", day = 31, month = 10, year = null,
                    category = DateCategory.HOLIDAY, colorHex = "#FF8E53",
                    description = "31 октября 🎃")
            )
            holidays.forEach { repository.insertDate(it) }
            prefs.edit().putBoolean(KEY_HOLIDAYS_SEEDED, true).apply()
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "important_dates_channel"
        private const val KEY_HOLIDAYS_SEEDED = "holidays_seeded"
    }
}
