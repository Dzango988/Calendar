package com.calendar.importantdates.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.calendar.importantdates.R
import com.calendar.importantdates.ui.MainActivity

class CalendarWidgetPill : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            Thread {
                try {
                    val views = buildViews(context)
                    appWidgetManager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    val v = RemoteViews(context.packageName, R.layout.widget_pill)
                    v.setTextViewText(R.id.pill_emoji, "📅")
                    v.setTextViewText(R.id.pill_title, "Нет важных дат")
                    v.setTextViewText(R.id.pill_sub, "Сегодня")
                    v.setViewVisibility(R.id.pill_countdown, View.GONE)
                    appWidgetManager.updateAppWidget(id, v)
                }
            }.start()
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val data = WidgetDataHelper.load(context, 3)
        val views = RemoteViews(context.packageName, R.layout.widget_pill)

        val pi = PendingIntent.getActivity(
            context, 20,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_pill_root, pi)

        when {
            data.todayEvents.isNotEmpty() -> {
                val e = data.todayEvents.first()
                views.setTextViewText(R.id.pill_emoji, e.category.emoji)
                views.setTextViewText(R.id.pill_title, e.title)
                views.setTextViewText(R.id.pill_sub, "Сегодня")
                views.setTextViewText(R.id.pill_countdown, "Сегодня!")
                views.setViewVisibility(R.id.pill_countdown, View.VISIBLE)
            }
            data.upcoming.isNotEmpty() -> {
                val next = data.upcoming.first()
                val days = WidgetDataHelper.daysUntil(next.day, next.month)
                views.setTextViewText(R.id.pill_emoji, next.category.emoji)
                views.setTextViewText(R.id.pill_title, next.title)
                views.setTextViewText(R.id.pill_sub, "Ближайшее")
                views.setTextViewText(R.id.pill_countdown, WidgetDataHelper.countdownText(days))
                views.setViewVisibility(R.id.pill_countdown, View.VISIBLE)
            }
            else -> {
                views.setTextViewText(R.id.pill_emoji, "📅")
                views.setTextViewText(R.id.pill_title, "Нет важных дат")
                views.setTextViewText(R.id.pill_sub, "Сегодня")
                views.setViewVisibility(R.id.pill_countdown, View.GONE)
            }
        }
        return views
    }
}
