package com.calendar.importantdates.utils

import java.util.Calendar
import java.util.Locale

data class CalendarDay(
    val dayNumber: Int,   // 0 = пустая ячейка
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val hasEvent: Boolean = false,
    val isWeekend: Boolean = false
)

object CalendarGridHelper {

    private val MONTH_NAMES = arrayOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    private val DAY_NAMES = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    fun getWeekDayNames() = DAY_NAMES.toList()

    fun formatMonthYear(month: Int, year: Int): String =
        "${MONTH_NAMES[month - 1]} $year"

    fun formatDate(day: Int, month: Int, year: Int): String =
        "$day ${MONTH_NAMES[month - 1]} $year"

    fun buildCalendarDays(
        month: Int,
        year: Int,
        markedDays: List<String>,  // "month-day" format
        selectedDay: Int?
    ): List<CalendarDay> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val todayMonth = today.get(Calendar.MONTH) + 1
        val todayYear = today.get(Calendar.YEAR)

        // Первый день недели (1=Пн...7=Вс)
        var firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 2
        if (firstDayOfWeek < 0) firstDayOfWeek = 6

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val markedSet = markedDays.toSet()

        val days = mutableListOf<CalendarDay>()

        // Пустые ячейки в начале
        repeat(firstDayOfWeek) {
            days.add(CalendarDay(0))
        }

        for (day in 1..daysInMonth) {
            val isToday = day == todayDay && month == todayMonth && year == todayYear
            val isSelected = day == selectedDay
            val hasEvent = markedSet.contains("$month-$day")
            val dayOfWeek = (firstDayOfWeek + day - 1) % 7  // 0=Пн...6=Вс
            val isWeekend = dayOfWeek == 5 || dayOfWeek == 6

            days.add(
                CalendarDay(
                    dayNumber = day,
                    isToday = isToday,
                    isSelected = isSelected,
                    hasEvent = hasEvent,
                    isWeekend = isWeekend
                )
            )
        }

        // Заполняем оставшиеся ячейки до кратности 7
        while (days.size % 7 != 0) {
            days.add(CalendarDay(0))
        }

        return days
    }

    fun getDaysInMonth(month: Int, year: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
        }
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}
