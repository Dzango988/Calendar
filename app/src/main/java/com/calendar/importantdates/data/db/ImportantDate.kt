package com.calendar.importantdates.data.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "important_dates")
data class ImportantDate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val day: Int,
    val month: Int,        // 1-12
    val year: Int?,        // null = ежегодное событие
    val category: DateCategory,
    val reminderDaysBefore: Int = 1,  // за сколько дней напоминать
    val isRecurringYearly: Boolean = true,
    val colorHex: String = "#FF6B6B",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class DateCategory(val displayName: String, val emoji: String) {
    BIRTHDAY("День рождения", "🎂"),
    ANNIVERSARY("Годовщина", "💑"),
    HOLIDAY("Праздник", "🎉"),
    WORK("Рабочее", "💼"),
    HEALTH("Здоровье", "🏥"),
    OTHER("Другое", "📌")
}
