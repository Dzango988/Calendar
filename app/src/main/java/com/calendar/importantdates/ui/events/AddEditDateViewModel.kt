package com.calendar.importantdates.ui.events

import android.app.Application
import androidx.lifecycle.*
import com.calendar.importantdates.data.db.DateCategory
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import com.calendar.importantdates.notifications.NotificationHelper
import kotlinx.coroutines.launch

class AddEditDateViewModel(
    app: Application,
    private val repository: DateRepository
) : AndroidViewModel(app) {

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun saveDate(
        existingId: Long?,
        title: String,
        description: String,
        day: Int,
        month: Int,
        year: Int?,
        category: DateCategory,
        reminderDaysBefore: Int,
        isRecurringYearly: Boolean,
        colorHex: String
    ) {
        if (title.isBlank()) {
            _saveSuccess.value = false
            return
        }

        val date = ImportantDate(
            id = existingId ?: 0,
            title = title.trim(),
            description = description.trim(),
            day = day,
            month = month,
            year = year,
            category = category,
            reminderDaysBefore = reminderDaysBefore,
            isRecurringYearly = isRecurringYearly,
            colorHex = colorHex
        )

        viewModelScope.launch {
            val savedDate = if (existingId != null && existingId > 0) {
                // Отменить старое напоминание перед обновлением
                NotificationHelper.cancelReminder(getApplication(), existingId)
                repository.updateDate(date)
                date
            } else {
                val newId = repository.insertDate(date)
                date.copy(id = newId)
            }
            // Запланировать новое напоминание
            NotificationHelper.scheduleReminder(getApplication(), savedDate)
            _saveSuccess.value = true
        }
    }
}

class AddEditDateViewModelFactory(
    private val app: Application,
    private val repository: DateRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditDateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditDateViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
