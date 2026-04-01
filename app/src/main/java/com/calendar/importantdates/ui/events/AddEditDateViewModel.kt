package com.calendar.importantdates.ui.events

import androidx.lifecycle.*
import com.calendar.importantdates.data.db.DateCategory
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.data.repository.DateRepository
import kotlinx.coroutines.launch

class AddEditDateViewModel(private val repository: DateRepository) : ViewModel() {

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
            if (existingId != null && existingId > 0) {
                repository.updateDate(date)
            } else {
                repository.insertDate(date)
            }
            _saveSuccess.value = true
        }
    }
}

class AddEditDateViewModelFactory(private val repository: DateRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditDateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditDateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
