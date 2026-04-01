package com.calendar.importantdates.ui.calendar

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.calendar.importantdates.App
import com.calendar.importantdates.R
import com.calendar.importantdates.databinding.FragmentCalendarBinding
import com.calendar.importantdates.ui.adapters.DatesAdapter
import com.calendar.importantdates.utils.CalendarGridHelper
import java.util.Calendar

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory((requireActivity().application as App).repository)
    }

    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var datesAdapter: DatesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupCalendarGrid()
        setupDatesRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.calendar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_today -> {
                        viewModel.goToToday()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupCalendarGrid() {
        calendarAdapter = CalendarDayAdapter { day ->
            viewModel.selectDay(day)
        }
        binding.rvCalendarGrid.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            itemAnimator = null
        }
    }

    private fun setupDatesRecyclerView() {
        datesAdapter = DatesAdapter(
            onItemClick = { date ->
                val action = CalendarFragmentDirections.actionCalendarToEditDate(date)
                findNavController().navigate(action)
            },
            onDeleteClick = null
        )
        binding.rvDayEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = datesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
        binding.fabAddDate.setOnClickListener {
            val action = CalendarFragmentDirections.actionCalendarToAddDate(
                selectedDay = viewModel.selectedDay.value ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                selectedMonth = viewModel.currentMonth.value ?: (Calendar.getInstance().get(Calendar.MONTH) + 1),
                selectedYear = viewModel.currentYear.value ?: Calendar.getInstance().get(Calendar.YEAR)
            )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.currentMonth.observe(viewLifecycleOwner) { refreshCalendar() }
        viewModel.currentYear.observe(viewLifecycleOwner) { refreshCalendar() }

        viewModel.allMarkedDays.observe(viewLifecycleOwner) {
            refreshCalendar()
        }

        viewModel.selectedDay.observe(viewLifecycleOwner) { day ->
            refreshCalendar()
            if (day != null) {
                binding.layoutDayEvents.visibility = View.VISIBLE
                val month = viewModel.currentMonth.value ?: 1
                val year = viewModel.currentYear.value ?: Calendar.getInstance().get(Calendar.YEAR)
                binding.tvSelectedDate.text = CalendarGridHelper.formatDate(day, month, year)
            } else {
                binding.layoutDayEvents.visibility = View.GONE
            }
        }

        viewModel.datesForSelectedDay.observe(viewLifecycleOwner) { dates ->
            datesAdapter.submitList(dates)
            binding.tvNoDayEvents.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun refreshCalendar() {
        val month = viewModel.currentMonth.value ?: return
        val year = viewModel.currentYear.value ?: return
        val selectedDay = viewModel.selectedDay.value
        val markedDays = viewModel.allMarkedDays.value ?: emptyList()

        binding.tvMonthYear.text = CalendarGridHelper.formatMonthYear(month, year)

        val days = CalendarGridHelper.buildCalendarDays(month, year, markedDays, selectedDay)
        calendarAdapter.submitList(days)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
