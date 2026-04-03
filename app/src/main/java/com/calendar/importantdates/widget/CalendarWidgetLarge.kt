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

class CalendarWidgetLarge : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            Thread {
                try {
                    val views = buildViews(context)
                    appWidgetManager.updateAppWidget(id, views)
                } catch (e: Exception) {
                    val v = RemoteViews(context.packageName, R.layout.widget_large)
                    v.setTextViewText(R.id.large_header, "Важные даты")
                    v.setViewVisibility(R.id.large_empty, View.VISIBLE)
                    v.setViewVisibility(R.id.large_row1, View.GONE)
                    v.setViewVisibility(R.id.large_row2, View.GONE)
                    v.setViewVisibility(R.id.large_row3, View.GONE)
                    v.setViewVisibility(R.id.large_row4, View.GONE)
                    appWidgetManager.updateAppWidget(id, v)
                }
            }.start()
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val data = WidgetDataHelper.load(context, 4)
        val views = RemoteViews(context.packageName, R.layout.widget_large)

        val pi = PendingIntent.getActivity(
            context, 30,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_large_root, pi)

        views.setTextViewText(R.id.large_header, "Важные даты")

        val rowIds = listOf(R.id.large_row1, R.id.large_row2, R.id.large_row3, R.id.large_row4)
        val emojiIds = listOf(R.id.large_emoji1, R.id.large_emoji2, R.id.large_emoji3, R.id.large_emoji4)
        val titleIds = listOf(R.id.large_title1, R.id.large_title2, R.id.large_title3, R.id.large_title4)
        val daysIds = listOf(R.id.large_days1, R.id.large_days2, R.id.large_days3, R.id.large_days4)

        val allEvents = buildList {
            data.todayEvents.forEach { add(Pair(it, 0)) }
            data.upcoming.forEach { e ->
                if (data.todayEvents.none { it.id == e.id }) {
                    add(Pair(e, WidgetDataHelper.daysUntil(e.day, e.month)))
                }
            }
        }.take(4)

        if (allEvents.isEmpty()) {
            views.setViewVisibility(R.id.large_empty, View.VISIBLE)
            rowIds.forEach { views.setViewVisibility(it, View.GONE) }
        } else {
            views.setViewVisibility(R.id.large_empty, View.GONE)
            allEvents.forEachIndexed { index, (event, days) ->
                views.setViewVisibility(rowIds[index], View.VISIBLE)
                views.setTextViewText(emojiIds[index], event.category.emoji)
                views.setTextViewText(titleIds[index], event.title)
                views.setTextViewText(daysIds[index], if (days == 0) "Сегодня!" else WidgetDataHelper.countdownText(days))
            }
            for (i in allEvents.size until 4) {
                views.setViewVisibility(rowIds[i], View.GONE)
            }
        }

        return views
    }
}
