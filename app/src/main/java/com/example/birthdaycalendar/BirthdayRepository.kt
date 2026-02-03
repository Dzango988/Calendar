package com.example.birthdaycalendar

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

class BirthdayRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<Birthday> {
        val json = prefs.getString(KEY_BIRTHDAYS, null) ?: return emptyList()
        val array = JSONArray(json)
        val result = mutableListOf<Birthday>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                Birthday(
                    name = obj.getString(KEY_NAME),
                    day = obj.getInt(KEY_DAY),
                    month = obj.getInt(KEY_MONTH)
                )
            )
        }
        return result.sortedBy { nextOccurrence(it) }
    }

    fun add(birthday: Birthday) {
        val current = getAll().toMutableList()
        current.add(birthday)
        saveAll(current)
    }

    fun saveAll(birthdays: List<Birthday>) {
        val array = JSONArray()
        birthdays.forEach { birthday ->
            val obj = JSONObject()
            obj.put(KEY_NAME, birthday.name)
            obj.put(KEY_DAY, birthday.day)
            obj.put(KEY_MONTH, birthday.month)
            array.put(obj)
        }
        prefs.edit().putString(KEY_BIRTHDAYS, array.toString()).apply()
    }

    fun getNextBirthday(): Birthday? {
        return getAll().minByOrNull { nextOccurrence(it) }
    }

    private fun nextOccurrence(birthday: Birthday): LocalDate {
        val now = LocalDate.now()
        val thisYear = LocalDate.of(now.year, birthday.month, birthday.day)
        return if (thisYear.isBefore(now)) {
            thisYear.plusYears(1)
        } else {
            thisYear
        }
    }

    companion object {
        private const val PREFS_NAME = "birthdays"
        private const val KEY_BIRTHDAYS = "birthdays_json"
        private const val KEY_NAME = "name"
        private const val KEY_DAY = "day"
        private const val KEY_MONTH = "month"
    }
}
