package com.calendar.importantdates.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val dateId = intent.getLongExtra(EXTRA_DATE_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val daysBefore = intent.getIntExtra(EXTRA_DAYS_BEFORE, 1)

        if (dateId == -1L) return

        NotificationHelper.showNotification(context, dateId, title, daysBefore)
    }

    companion object {
        const val EXTRA_DATE_ID = "extra_date_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DAYS_BEFORE = "extra_days_before"
    }
}
