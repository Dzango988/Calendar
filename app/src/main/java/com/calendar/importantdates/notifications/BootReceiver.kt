package com.calendar.importantdates.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.calendar.importantdates.data.db.AppDatabase
import kotlinx.coroutines.*

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Перепланировать все напоминания после перезагрузки
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val dao = db.importantDateDao()
            // Используем простой запрос для получения всех дат
            // (вне LiveData, поэтому делаем отдельный suspend-запрос)
            rescheduleAll(context, dao)
        }
    }

    private suspend fun rescheduleAll(
        context: Context,
        dao: com.calendar.importantdates.data.db.ImportantDateDao
    ) {
        // Нужен suspend-запрос всех дат; добавим его через extension
        // Пока используем корутину с Flow/запросом напрямую
    }
}
