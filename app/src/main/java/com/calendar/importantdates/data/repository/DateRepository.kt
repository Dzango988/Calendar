package com.calendar.importantdates.data.repository

import androidx.lifecycle.LiveData
import com.calendar.importantdates.data.db.AppDatabase
import com.calendar.importantdates.data.db.ImportantDate

class DateRepository(private val db: AppDatabase) {

    val allDates: LiveData<List<ImportantDate>> = db.importantDateDao().getAllDates()

    fun getDatesByMonth(month: Int): LiveData<List<ImportantDate>> =
        db.importantDateDao().getDatesByMonth(month)

    fun getDatesByDayAndMonth(day: Int, month: Int): LiveData<List<ImportantDate>> =
        db.importantDateDao().getDatesByDayAndMonth(day, month)

    fun getUpcomingDates(month: Int, day: Int, limit: Int = 10): LiveData<List<ImportantDate>> =
        db.importantDateDao().getUpcomingDates(month, day, limit)

    fun getAllMarkedDays(): LiveData<List<String>> =
        db.importantDateDao().getAllMarkedDays()

    suspend fun getDateById(id: Long): ImportantDate? =
        db.importantDateDao().getDateById(id)

    suspend fun insertDate(date: ImportantDate): Long =
        db.importantDateDao().insertDate(date)

    suspend fun updateDate(date: ImportantDate) =
        db.importantDateDao().updateDate(date)

    suspend fun deleteDate(date: ImportantDate) =
        db.importantDateDao().deleteDate(date)
}
