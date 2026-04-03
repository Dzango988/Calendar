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
import java.util.Calendar

class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            Thread {
                try {
                    val views = buildViews(context)
                    appWidgetManager.updateAppWidget(widgetId, views)
                } catch (e: Exception) {
                    val views = RemoteViews(context.packageName, R.layout.widget_calendar)
                    views.setTextViewText(R.id.widget_event_title, "Сегодня нет важных дат")
                    views.setTextViewText(R.id.widget_today_label, "Сегодня")
                    views.setTextViewText(R.id.widget_event_icon, "📅")
                    views.setTextViewText(R.id.widget_countdown, "")
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }.start()
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val dao = AppDatabase.getInstance(context).importantDateDao()
        val now = Calendar.getInstance()
        val todayDay = now.get(Calendar.DAY_OF_MONTH)
        val todayMonth = now.get(Calendar.MONTH) + 1

        // Прямые синхронные вызовы — без корутин, без runBlocking
        val todayEvents = dao.getDatesByDayAndMonthDirect(todayDay, todayMonth)
        val upcoming = dao.getUpcomingDatesDirect(todayMonth, todayDay, 5)

        val views = RemoteViews(context.packageName, R.layout.widget_calendar)

        // Клик по всему виджету открывает приложение
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pi)

        when {
            todayEvents.isNotEmpty() -> {
                val event = todayEvents.first()
                val extra = if (todayEvents.size > 1) " +ещё ${todayEvents.size - 1}" else ""
                views.setTextViewText(R.id.widget_today_label, "Сегодня")
                views.setTextViewText(R.id.widget_event_icon, event.category.emoji)
                views.setTextViewText(R.id.widget_event_title, event.title)
                views.setTextViewText(R.id.widget_countdown, "Сегодня!$extra")
            }
            upcoming.isNotEmpty() -> {
                val next = upcoming.first()
                val days = daysUntil(next.day, next.month)
                views.setTextViewText(R.id.widget_today_label, "Ближайшее")
                views.setTextViewText(R.id.widget_event_icon, next.category.emoji)
                views.setTextViewText(R.id.widget_event_title, next.title)
                views.setTextViewText(
                    R.id.widget_countdown,
                    if (days == 1) "Завтра!" else "через $days дн."
                )
            }
            else -> {
                views.setTextViewText(R.id.widget_today_label, "Сегодня")
                views.setTextViewText(R.id.widget_event_icon, "📅")
                views.setTextViewText(R.id.widget_event_title, "Сегодня нет важных дат")
                views.setTextViewText(R.id.widget_countdown, "")
            }
        }

        return views
    }

    private fun daysUntil(day: Int, month: Int): Int {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.YEAR, 1)
        }
        return ((target.timeInMillis - now.timeInMillis) / 86_400_000L).toInt().coerceAtLeast(1)
    }
}
