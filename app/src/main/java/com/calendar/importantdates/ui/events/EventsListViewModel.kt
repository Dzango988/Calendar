package com.calendar.importantdates.ui.events

import android.app.Application
import androidx.lifecycle.*
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import com.calendar.importantdates.notifications.NotificationHelper
import kotlinx.coroutines.launch
import java.util.Calendar

class EventsListViewModel(
    app: Application,
    private val repository: DateRepository
) : AndroidViewModel(app) {

    val allDates: LiveData<List<ImportantDate>> = repository.allDates

    private val _currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val _currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val upcomingDates: LiveData<List<ImportantDate>> =
        repository.getUpcomingDates(_currentMonth, _currentDay, 20)

    private val _syncMessage = MutableLiveData<String?>()
    val syncMessage: LiveData<String?> = _syncMessage

    fun deleteDate(date: ImportantDate) {
        viewModelScope.launch {
            NotificationHelper.cancelReminder(getApplication(), date.id)
            repository.deleteDate(date)
        }
    }

    fun onSyncMessageShown() {
        _syncMessage.value = null
    }

    fun importFromGoogleCalendar() {
        viewModelScope.launch {
            try {
                val imported = com.calendar.importantdates.data.sync.GoogleCalendarImporter
                    .import(getApplication(), repository)
                _syncMessage.value = if (imported > 0)
                    "Импортировано событий: $imported"
                else
                    "Новых событий не найдено"
            } catch (e: Exception) {
                _syncMessage.value = "Ошибка синхронизации: ${e.message}"
            }
        }
    }
}

class EventsListViewModelFactory(
    private val app: Application,
    private val repository: DateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventsListViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
