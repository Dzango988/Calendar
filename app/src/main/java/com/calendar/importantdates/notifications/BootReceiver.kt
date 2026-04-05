package com.calendar.importantdates.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calendar.importantdates.data.db.AppDatabase

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Thread {
            val dao = AppDatabase.getInstance(context).importantDateDao()
            val dates = dao.getAllDatesDirect()
            dates.forEach { date ->
                if (date.reminderDaysBefore >= 0) {
                    NotificationHelper.scheduleReminder(context, date)
                }
            }
        }.start()
    }
}
