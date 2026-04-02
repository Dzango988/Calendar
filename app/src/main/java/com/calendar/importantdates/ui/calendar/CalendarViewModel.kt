package com.calendar.importantdates.ui.calendar

import androidx.lifecycle.*
import com.calendar.importantdates.App
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarViewModel(private val repository: DateRepository) : ViewModel() {

    private val _currentMonth = MutableLiveData<Int>()
    private val _currentYear = MutableLiveData<Int>()

    val currentMonth: LiveData<Int> = _currentMonth
    val currentYear: LiveData<Int> = _currentYear

    private val _selectedDay = MutableLiveData<Int?>()
    val selectedDay: LiveData<Int?> = _selectedDay

    val allMarkedDays: LiveData<List<String>> = repository.getAllMarkedDays()

    val datesForSelectedDay: LiveData<List<ImportantDate>> =
        MediatorLiveData<List<ImportantDate>>().apply {
            fun update() {
                val day = _selectedDay.value ?: return
                val month = _currentMonth.value ?: return
                addSource(repository.getDatesByDayAndMonth(day, month)) { value = it }
            }
            addSource(_selectedDay) { update() }
            addSource(_currentMonth) { update() }
        }

    val datesForCurrentMonth: LiveData<List<ImportantDate>> =
        _currentMonth.switchMap { month ->
            repository.getDatesByMonth(month)
        }

    init {
        val now = Calendar.getInstance()
        _currentMonth.value = now.get(Calendar.MONTH) + 1  // 1-based
        _currentYear.value = now.get(Calendar.YEAR)
    }

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun clearSelection() {
        _selectedDay.value = null
    }

    fun previousMonth() {
        val month = _currentMonth.value ?: return
        val year = _currentYear.value ?: return
        if (month == 1) {
            _currentMonth.value = 12
            _currentYear.value = year - 1
        } else {
            _currentMonth.value = month - 1
        }
        _selectedDay.value = null
    }

    fun nextMonth() {
        val month = _currentMonth.value ?: return
        val year = _currentYear.value ?: return
        if (month == 12) {
            _currentMonth.value = 1
            _currentYear.value = year + 1
        } else {
            _currentMonth.value = month + 1
        }
        _selectedDay.value = null
    }

    fun goToToday() {
        val now = Calendar.getInstance()
        _currentMonth.value = now.get(Calendar.MONTH) + 1
        _currentYear.value = now.get(Calendar.YEAR)
        _selectedDay.value = now.get(Calendar.DAY_OF_MONTH)
    }
}

class CalendarViewModelFactory(private val repository: DateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
