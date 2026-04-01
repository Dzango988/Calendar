package com.calendar.importantdates.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.calendar.importantdates.App
import com.calendar.importantdates.R
import com.calendar.importantdates.data.db.DateCategory
import com.calendar.importantdates.databinding.FragmentAddEditDateBinding
import com.calendar.importantdates.utils.CalendarGridHelper

class AddEditDateFragment : Fragment() {

    private var _binding: FragmentAddEditDateBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditDateFragmentArgs by navArgs()

    private val viewModel: AddEditDateViewModel by viewModels {
        AddEditDateViewModelFactory((requireActivity().application as App).repository)
    }

    private val categories = DateCategory.values()

    private val colorOptions = listOf(
        "#FF6B6B", "#FF8E53", "#FFD93D", "#6BCB77",
        "#4D96FF", "#A855F7", "#EC4899", "#14B8A6"
    )
    private var selectedColorIndex = 0
    private var selectedCategoryIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditDateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupReminderSpinner()
        setupColorPicker()
        populateFields()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupCategorySpinner() {
        val categoryNames = categories.map { "${it.emoji} ${it.displayName}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupReminderSpinner() {
        val options = listOf("В день события", "За 1 день", "За 3 дня", "За 7 дней", "Без напоминания")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReminder.adapter = adapter
        binding.spinnerReminder.setSelection(1)
    }

    private fun setupColorPicker() {
        val colorViews = listOf(
            binding.color1, binding.color2, binding.color3, binding.color4,
            binding.color5, binding.color6, binding.color7, binding.color8
        )

        colorViews.forEachIndexed { index, view ->
            try {
                view.setBackgroundColor(android.graphics.Color.parseColor(colorOptions[index]))
            } catch (e: Exception) { /* ignore */ }

            view.setOnClickListener {
                selectedColorIndex = index
                updateColorSelection(colorViews)
            }
        }
        updateColorSelection(colorViews)
    }

    private fun updateColorSelection(views: List<View>) {
        views.forEachIndexed { index, view ->
            view.scaleX = if (index == selectedColorIndex) 1.3f else 1.0f
            view.scaleY = if (index == selectedColorIndex) 1.3f else 1.0f
            view.elevation = if (index == selectedColorIndex) 8f else 2f
        }
    }

    private fun populateFields() {
        val existingDate = args.existingDate
        if (existingDate != null) {
            requireActivity().title = "Редактировать"
            binding.etTitle.setText(existingDate.title)
            binding.etDescription.setText(existingDate.description)
            binding.etDay.setText(existingDate.day.toString())
            binding.etMonth.setText(existingDate.month.toString())
            binding.etYear.setText(existingDate.year?.toString() ?: "")
            binding.switchRecurring.isChecked = existingDate.isRecurringYearly

            val catIndex = categories.indexOfFirst { it == existingDate.category }
            if (catIndex >= 0) binding.spinnerCategory.setSelection(catIndex)

            val colorIndex = colorOptions.indexOfFirst { it == existingDate.colorHex }
            if (colorIndex >= 0) selectedColorIndex = colorIndex

            val reminderIndex = reminderDaysToIndex(existingDate.reminderDaysBefore)
            binding.spinnerReminder.setSelection(reminderIndex)
        } else {
            requireActivity().title = "Добавить дату"
            binding.etDay.setText(args.selectedDay.toString())
            binding.etMonth.setText(args.selectedMonth.toString())
            binding.etYear.setText(args.selectedYear.toString())
        }

        // Год скрыт/показан в зависимости от переключателя
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.tilYear.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        val isRecurring = existingDate?.isRecurringYearly ?: true
        binding.switchRecurring.isChecked = isRecurring
        binding.tilYear.visibility = if (isRecurring) View.GONE else View.VISIBLE
    }

    private fun reminderDaysToIndex(days: Int): Int = when (days) {
        0 -> 0
        1 -> 1
        3 -> 2
        7 -> 3
        -1 -> 4
        else -> 1
    }

    private fun indexToReminderDays(index: Int): Int = when (index) {
        0 -> 0
        1 -> 1
        2 -> 3
        3 -> 7
        4 -> -1
        else -> 1
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()
            val day = binding.etDay.text.toString().toIntOrNull() ?: 1
            val month = binding.etMonth.text.toString().toIntOrNull() ?: 1
            val isRecurring = binding.switchRecurring.isChecked
            val year = if (isRecurring) null else binding.etYear.text.toString().toIntOrNull()
            val category = categories[binding.spinnerCategory.selectedItemPosition]
            val reminderDays = indexToReminderDays(binding.spinnerReminder.selectedItemPosition)
            val color = colorOptions[selectedColorIndex]
            val existingId = args.existingDate?.id

            if (title.isBlank()) {
                binding.tilTitle.error = "Введите название"
                return@setOnClickListener
            }
            if (day < 1 || day > CalendarGridHelper.getDaysInMonth(month, year ?: 2024)) {
                binding.tilDay.error = "Неверный день"
                return@setOnClickListener
            }
            if (month < 1 || month > 12) {
                binding.tilMonth.error = "Неверный месяц"
                return@setOnClickListener
            }

            binding.tilTitle.error = null
            binding.tilDay.error = null
            binding.tilMonth.error = null

            viewModel.saveDate(
                existingId, title, description, day, month, year,
                category, reminderDays, isRecurring, color
            )
        }
    }

    private fun observeViewModel() {
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Сохранено!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                binding.tilTitle.error = "Введите название"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
