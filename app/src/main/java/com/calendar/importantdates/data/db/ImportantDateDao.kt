package com.calendar.importantdates.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ImportantDateDao {

    @Query("SELECT * FROM important_dates ORDER BY month, day")
    fun getAllDates(): LiveData<List<ImportantDate>>

    @Query("SELECT * FROM important_dates WHERE month = :month ORDER BY day")
    fun getDatesByMonth(month: Int): LiveData<List<ImportantDate>>

    @Query("SELECT * FROM important_dates WHERE day = :day AND month = :month ORDER BY title")
    fun getDatesByDayAndMonth(day: Int, month: Int): LiveData<List<ImportantDate>>

    @Query("""
        SELECT * FROM important_dates
        WHERE (month = :month AND day >= :day) OR month > :month
        ORDER BY month, day
        LIMIT :limit
    """)
    fun getUpcomingDates(month: Int, day: Int, limit: Int = 10): LiveData<List<ImportantDate>>

    @Query("SELECT * FROM important_dates WHERE id = :id")
    suspend fun getDateById(id: Long): ImportantDate?

    @Query("SELECT DISTINCT month || '-' || day FROM important_dates")
    fun getAllMarkedDays(): LiveData<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDate(date: ImportantDate): Long

    @Update
    suspend fun updateDate(date: ImportantDate)

    @Delete
    suspend fun deleteDate(date: ImportantDate)

    @Query("DELETE FROM important_dates WHERE id = :id")
    suspend fun deleteDateById(id: Long)

    // Прямые синхронные методы для виджета/BootReceiver (без корутин, вызываются из фонового потока)
    @Query("SELECT * FROM important_dates ORDER BY month, day")
    fun getAllDatesDirect(): List<ImportantDate>

    @Query("SELECT * FROM important_dates WHERE externalId = :externalId LIMIT 1")
    suspend fun getByExternalId(externalId: String): ImportantDate?


    @Query("SELECT * FROM important_dates WHERE day = :day AND month = :month ORDER BY title")
    fun getDatesByDayAndMonthDirect(day: Int, month: Int): List<ImportantDate>

    @Query("""
        SELECT * FROM important_dates
        WHERE (month = :month AND day >= :day) OR month > :month
        ORDER BY month, day
        LIMIT :limit
    """)
    fun getUpcomingDatesDirect(month: Int, day: Int, limit: Int): List<ImportantDate>
}
