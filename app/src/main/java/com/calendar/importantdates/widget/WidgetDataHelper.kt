package com.calendar.importantdates.widget

import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.db.AppDatabase
import android.content.Context
import java.util.Calendar

data class WidgetData(
    val todayEvents: List<ImportantDate>,
    val upcoming: List<ImportantDate>
)

object WidgetDataHelper {

    fun load(context: Context, upcomingLimit: Int = 5): WidgetData {
        val dao = AppDatabase.getInstance(context).importantDateDao()
        val now = Calendar.getInstance()
        val day = now.get(Calendar.DAY_OF_MONTH)
        val month = now.get(Calendar.MONTH) + 1
        return WidgetData(
            todayEvents = dao.getDatesByDayAndMonthDirect(day, month),
            upcoming = dao.getUpcomingDatesDirect(month, day, upcomingLimit)
        )
    }

    fun daysUntil(day: Int, month: Int): Int {
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

    fun countdownText(days: Int): String = when (days) {
        1 -> "Завтра!"
        else -> "через $days дн."
    }
}
