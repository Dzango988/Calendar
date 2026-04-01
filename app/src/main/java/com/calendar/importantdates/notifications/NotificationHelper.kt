package com.calendar.importantdates.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.calendar.importantdates.App
import com.calendar.importantdates.R
import com.calendar.importantdates.data.db.ImportantDate
import java.util.Calendar

object NotificationHelper {

    fun scheduleReminder(context: Context, date: ImportantDate) {
        if (date.reminderDaysBefore < 0) return  // -1 = без напоминания

        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val now = Calendar.getInstance()
        val targetYear = date.year ?: now.get(Calendar.YEAR)

        val reminderCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, targetYear)
            set(Calendar.MONTH, date.month - 1)
            set(Calendar.DAY_OF_MONTH, date.day)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_MONTH, -date.reminderDaysBefore)
        }

        // Если уже прошло — планируем на следующий год (для ежегодных)
        if (reminderCal.timeInMillis <= now.timeInMillis && date.isRecurringYearly) {
            reminderCal.add(Calendar.YEAR, 1)
        }

        if (reminderCal.timeInMillis <= now.timeInMillis) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_DATE_ID, date.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, date.title)
            putExtra(AlarmReceiver.EXTRA_DAYS_BEFORE, date.reminderDaysBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            date.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderCal.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Нет разрешения на точные будильники — используем неточный
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminderCal.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, dateId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dateId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun showNotification(context: Context, dateId: Long, title: String, daysBefore: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val message = when (daysBefore) {
            0 -> "Сегодня: $title"
            1 -> "Завтра: $title"
            else -> "Через $daysBefore дней: $title"
        }

        val notification = NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Важная дата")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(dateId.toInt(), notification)
    }
}
