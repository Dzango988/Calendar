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
                    updateWidget(context, appWidgetManager, widgetId)
                } catch (e: Exception) {
                    // Показываем заглушку при ошибке
                    showFallback(context, appWidgetManager, widgetId)
                }
            }.start()
        }
    }

    companion object {

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dao = AppDatabase.getInstance(context).importantDateDao()
            val now = Calendar.getInstance()
            val todayDay = now.get(Calendar.DAY_OF_MONTH)
            val todayMonth = now.get(Calendar.MONTH) + 1

            // Синхронные suspend функции вызываем через runBlocking
            val todayEvents = kotlinx.coroutines.runBlocking {
                dao.getDatesByDayAndMonthSync(todayDay, todayMonth)
            }
            val upcomingEvents = kotlinx.coroutines.runBlocking {
                dao.getUpcomingDatesSync(todayMonth, todayDay, 5)
            }

            val views = RemoteViews(context.packageName, R.layout.widget_calendar)

            // Клик по виджету — открывает приложение
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
                    views.setTextViewText(R.id.widget_event_icon, event.category.emoji)
                    views.setTextViewText(R.id.widget_event_title, event.title)
                    val extra = if (todayEvents.size > 1) "+${todayEvents.size - 1} ещё" else ""
                    views.setTextViewText(R.id.widget_countdown, if (extra.isNotEmpty()) extra else "Сегодня!")
                }
                upcomingEvents.isNotEmpty() -> {
                    val next = upcomingEvents.first()
                    val days = calcDaysUntil(next.day, next.month)
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
                    views.setTextViewText(R.id.widget_event_title, "Нет важных дат")
                    views.setTextViewText(R.id.widget_countdown, "")
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun showFallback(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            views.setTextViewText(R.id.widget_today_label, "Сегодня")
            views.setTextViewText(R.id.widget_event_icon, "📅")
            views.setTextViewText(R.id.widget_event_title, "Нет важных дат")
            views.setTextViewText(R.id.widget_countdown, "")
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
                if (!after(now)) add(Calendar.YEAR, 1)
            }
            val diff = event.timeInMillis - now.timeInMillis
            return (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        }
    }
}
