package com.calendar.importantdates.ui.events

import androidx.lifecycle.*
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class EventsListViewModel(private val repository: DateRepository) : ViewModel() {

    val allDates: LiveData<List<ImportantDate>> = repository.allDates

    private val _currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val _currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val upcomingDates: LiveData<List<ImportantDate>> =
        repository.getUpcomingDates(_currentMonth, _currentDay, 20)

    fun deleteDate(date: ImportantDate) {
        viewModelScope.launch {
            repository.deleteDate(date)
        }
    }
}

class EventsListViewModelFactory(private val repository: DateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventsListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
