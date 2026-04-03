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
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val dao = db.importantDateDao()
                val now = Calendar.getInstance()
                val todayDay = now.get(Calendar.DAY_OF_MONTH)
                val todayMonth = now.get(Calendar.MONTH) + 1

                // Проверяем события сегодня
                val todayEvents = dao.getDatesByDayAndMonthSync(todayDay, todayMonth)

                // Ближайшее предстоящее событие
                val upcomingEvents = dao.getUpcomingDatesSync(todayMonth, todayDay, 5)

                withContext(Dispatchers.Main) {
                    val views = RemoteViews(context.packageName, R.layout.widget_calendar)

                    // Клик по виджету → открыть приложение
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_event_title, pendingIntent)

                    if (todayEvents.isNotEmpty()) {
                        // Сегодня есть события
                        val event = todayEvents.first()
                        views.setTextViewText(R.id.widget_today_label, "Сегодня")
                        views.setTextViewText(R.id.widget_event_emoji, event.category.emoji)
                        views.setTextViewText(R.id.widget_event_title, event.title)
                        val more = if (todayEvents.size > 1) " +ещё ${todayEvents.size - 1}" else ""
                        views.setTextViewText(R.id.widget_countdown, "Сегодня!$more")
                    } else if (upcomingEvents.isNotEmpty()) {
                        // Ближайшее событие
                        val next = upcomingEvents.first()
                        val daysUntil = calcDaysUntil(next.day, next.month)
                        views.setTextViewText(R.id.widget_today_label, "Ближайшее")
                        views.setTextViewText(R.id.widget_event_emoji, next.category.emoji)
                        views.setTextViewText(R.id.widget_event_title, next.title)
                        views.setTextViewText(
                            R.id.widget_countdown,
                            if (daysUntil == 1) "Завтра!" else "через $daysUntil дн."
                        )
                    } else {
                        // Нет событий
                        views.setTextViewText(R.id.widget_today_label, "Сегодня")
                        views.setTextViewText(R.id.widget_event_emoji, "📅")
                        views.setTextViewText(R.id.widget_event_title, "Сегодня нет важных дат")
                        views.setTextViewText(R.id.widget_countdown, "")
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
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
            return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        }
    }
}
