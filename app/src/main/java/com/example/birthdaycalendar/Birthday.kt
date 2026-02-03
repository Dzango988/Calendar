package com.example.birthdaycalendar

data class Birthday(
    val name: String,
    val day: Int,
    val month: Int
) {
    fun formattedDate(): String = "%02d.%02d".format(day, month)
}
