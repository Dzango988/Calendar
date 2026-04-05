package com.calendar.importantdates.data.sync

import android.content.Context
import android.provider.CalendarContract
import com.calendar.importantdates.data.db.DateCategory
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import java.util.Calendar

object GoogleCalendarImporter {

    /**
     * Читает события из системного Google Календаря и импортирует новые в базу.
     * Возвращает количество импортированных событий.
     * Требует разрешение READ_CALENDAR.
     */
    suspend fun import(context: Context, repository: DateRepository): Int {
        var count = 0

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.CALENDAR_DISPLAY_NAME
        )

        val selection = "${CalendarContract.Events.DELETED} = 0 AND ${CalendarContract.Events.TITLE} IS NOT NULL"

        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            null,
            "${CalendarContract.Events.DTSTART} ASC"
        ) ?: return 0

        cursor.use {
            val idCol = it.getColumnIndex(CalendarContract.Events._ID)
            val titleCol = it.getColumnIndex(CalendarContract.Events.TITLE)
            val descCol = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val startCol = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val allDayCol = it.getColumnIndex(CalendarContract.Events.ALL_DAY)
            val rruleCol = it.getColumnIndex(CalendarContract.Events.RRULE)
            val calNameCol = it.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME)

            while (it.moveToNext()) {
                val externalId = "gcal_${it.getLong(idCol)}"
                val title = it.getString(titleCol)?.trim() ?: continue
                if (title.isBlank()) continue

                // Пропустить уже импортированные
                if (repository.getByExternalId(externalId) != null) continue

                val startMs = it.getLong(startCol)
                val rrule = if (rruleCol >= 0) it.getString(rruleCol) else null
                val isRecurring = rrule?.contains("YEARLY") == true ||
                        it.getString(calNameCol)?.lowercase()?.contains("birthday") == true ||
                        it.getString(calNameCol)?.lowercase()?.contains("день рождения") == true

                val cal = Calendar.getInstance().apply { timeInMillis = startMs }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val month = cal.get(Calendar.MONTH) + 1
                val year = if (isRecurring) null else cal.get(Calendar.YEAR)

                val calName = if (calNameCol >= 0) it.getString(calNameCol)?.lowercase() ?: "" else ""
                val category = when {
                    calName.contains("birthday") || calName.contains("день рождения") -> DateCategory.BIRTHDAY
                    calName.contains("holiday") || calName.contains("праздник") -> DateCategory.HOLIDAY
                    else -> detectCategory(title)
                }

                val description = if (descCol >= 0) it.getString(descCol) ?: "" else ""

                val date = ImportantDate(
                    title = title,
                    description = description,
                    day = day,
                    month = month,
                    year = year,
                    category = category,
                    reminderDaysBefore = 1,
                    isRecurringYearly = isRecurring,
                    colorHex = categoryColor(category),
                    externalId = externalId
                )

                repository.insertDate(date)
                count++
            }
        }

        return count
    }

    private fun detectCategory(title: String): DateCategory {
        val lower = title.lowercase()
        return when {
            lower.contains("день рождения") || lower.contains("birthday") || lower.contains("д.р.") -> DateCategory.BIRTHDAY
            lower.contains("годовщина") || lower.contains("anniversary") -> DateCategory.ANNIVERSARY
            lower.contains("праздник") || lower.contains("holiday") -> DateCategory.HOLIDAY
            lower.contains("работа") || lower.contains("meeting") || lower.contains("встреча") -> DateCategory.WORK
            else -> DateCategory.OTHER
        }
    }

    private fun categoryColor(category: DateCategory): String = when (category) {
        DateCategory.BIRTHDAY -> "#FF6B6B"
        DateCategory.ANNIVERSARY -> "#EC4899"
        DateCategory.HOLIDAY -> "#FFD93D"
        DateCategory.WORK -> "#4D96FF"
        DateCategory.HEALTH -> "#6BCB77"
        DateCategory.OTHER -> "#A855F7"
    }
}
