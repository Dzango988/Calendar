package com.calendar.importantdates.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.calendar.importantdates.R
import com.calendar.importantdates.ui.MainActivity

class CalendarWidgetMini : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            Thread {
                try {
                    val views = buildViews(context)
                    appWidgetManager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    val views = RemoteViews(context.packageName, R.layout.widget_mini)
                    views.setTextViewText(R.id.mini_emoji, "📅")
                    views.setTextViewText(R.id.mini_days, "—")
                    appWidgetManager.updateAppWidget(id, views)
                }
            }.start()
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val data = WidgetDataHelper.load(context, 3)
        val views = RemoteViews(context.packageName, R.layout.widget_mini)

        val pi = PendingIntent.getActivity(
            context, 10,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_mini_root, pi)

        when {
            data.todayEvents.isNotEmpty() -> {
                views.setTextViewText(R.id.mini_emoji, data.todayEvents.first().category.emoji)
                views.setTextViewText(R.id.mini_days, "Сегодня!")
            }
            data.upcoming.isNotEmpty() -> {
                val next = data.upcoming.first()
                val days = WidgetDataHelper.daysUntil(next.day, next.month)
                views.setTextViewText(R.id.mini_emoji, next.category.emoji)
                views.setTextViewText(R.id.mini_days, if (days == 1) "Завтра" else "$days дн.")
            }
            else -> {
                views.setTextViewText(R.id.mini_emoji, "📅")
                views.setTextViewText(R.id.mini_days, "—")
            }
        }
        return views
    }
}
