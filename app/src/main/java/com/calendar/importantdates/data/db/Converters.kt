package com.calendar.importantdates.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(category: DateCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): DateCategory = DateCategory.valueOf(value)
}
