package com.example.birthdaycalendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class BirthdayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, BirthdayWidgetProvider::class.java))
            updateWidgets(context, appWidgetManager, ids)
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val repository = BirthdayRepository(context)
            val next = repository.getNextBirthday()
            val content = next?.let { "${it.name} • ${it.formattedDate()}" }
                ?: context.getString(R.string.widget_empty)

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_birthday)
                views.setTextViewText(R.id.widgetContent, content)
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
