package com.calendar.importantdates.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.calendar.importantdates.R
import com.calendar.importantdates.data.db.AppDatabase
import com.calendar.importantdates.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { widgetId ->
                    updateWidget(context, appWidgetManager, widgetId)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {

        suspend fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dao = AppDatabase.getInstance(context).importantDateDao()
            val now = Calendar.getInstance()
            val todayDay = now.get(Calendar.DAY_OF_MONTH)
            val todayMonth = now.get(Calendar.MONTH) + 1

            val todayEvents = dao.getDatesByDayAndMonthSync(todayDay, todayMonth)
            val upcomingEvents = dao.getUpcomingDatesSync(todayMonth, todayDay, 5)

            val views = RemoteViews(context.packageName, R.layout.widget_calendar)

            // Клик по всему виджету → открыть приложение
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            when {
                todayEvents.isNotEmpty() -> {
                    val event = todayEvents.first()
                    views.setTextViewText(R.id.widget_today_label, "Сегодня")
                    views.setTextViewText(R.id.widget_event_emoji, event.category.emoji)
                    views.setTextViewText(R.id.widget_event_title, event.title)
                    val extra = if (todayEvents.size > 1) " +ещё ${todayEvents.size - 1}" else ""
                    views.setTextViewText(R.id.widget_countdown, "Сегодня!$extra")
                }
                upcomingEvents.isNotEmpty() -> {
                    val next = upcomingEvents.first()
                    val daysUntil = calcDaysUntil(next.day, next.month)
                    views.setTextViewText(R.id.widget_today_label, "Ближайшее")
                    views.setTextViewText(R.id.widget_event_emoji, next.category.emoji)
                    views.setTextViewText(R.id.widget_event_title, next.title)
                    views.setTextViewText(
                        R.id.widget_countdown,
                        if (daysUntil == 1) "Завтра!" else "через $daysUntil дн."
                    )
                }
                else -> {
                    views.setTextViewText(R.id.widget_today_label, "Сегодня")
                    views.setTextViewText(R.id.widget_event_emoji, "📅")
                    views.setTextViewText(R.id.widget_event_title, "Сегодня нет важных дат")
                    views.setTextViewText(R.id.widget_countdown, "")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun calcDaysUntil(day: Int, month: Int): Int {
            val now = Calendar.getInstance()
            val event = Calendar.getInstance().apply {
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) add(Calendar.YEAR, 1)
            }
            val diff = event.timeInMillis - now.timeInMillis
            return (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        }
    }
}
