package com.example.birthdaycalendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {
    fun scheduleAll(context: Context) {
        val repository = BirthdayRepository(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        repository.getAll().forEach { birthday ->
            val trigger = nextTriggerTime(birthday)
            val intent = Intent(context, BirthdayNotificationReceiver::class.java).apply {
                putExtra(BirthdayNotificationReceiver.EXTRA_NAME, birthday.name)
                putExtra(BirthdayNotificationReceiver.EXTRA_DATE, birthday.formattedDate())
            }
            val requestCode = requestCodeFor(birthday)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger,
                pendingIntent
            )
        }
    }

    private fun nextTriggerTime(birthday: Birthday): Long {
        val now = LocalDate.now()
        val nextDate = LocalDate.of(now.year, birthday.month, birthday.day).let {
            if (it.isBefore(now)) it.plusYears(1) else it
        }
        val dateTime = LocalDateTime.of(nextDate, LocalTime.of(9, 0))
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun requestCodeFor(birthday: Birthday): Int {
        return (birthday.name.hashCode() * 31) + (birthday.month * 100 + birthday.day)
    }
}
