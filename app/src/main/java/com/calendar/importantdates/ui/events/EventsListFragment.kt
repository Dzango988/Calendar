package com.calendar.importantdates.ui.events

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.calendar.importantdates.App
import com.calendar.importantdates.R
import com.calendar.importantdates.databinding.FragmentEventsListBinding
import com.calendar.importantdates.ui.adapters.DatesAdapter
import java.util.Calendar

class EventsListFragment : Fragment() {

    private var _binding: FragmentEventsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventsListViewModel by viewModels {
        EventsListViewModelFactory((requireActivity().application as App).repository)
    }

    private lateinit var allDatesAdapter: DatesAdapter
    private lateinit var upcomingDatesAdapter: DatesAdapter

    private var showAll = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeViewModel()
    }

    private fun setupAdapters() {
        val deleteHandler = { date: com.calendar.importantdates.data.db.ImportantDate ->
            AlertDialog.Builder(requireContext())
                .setTitle("Удалить событие")
                .setMessage("Удалить «${date.title}»?")
                .setPositiveButton("Удалить") { _, _ -> viewModel.deleteDate(date) }
                .setNegativeButton("Отмена", null)
                .show()
            Unit
        }

        upcomingDatesAdapter = DatesAdapter(
            onItemClick = { date ->
                val action = EventsListFragmentDirections.actionEventsListToEditDate(date)
                findNavController().navigate(action)
            },
            onDeleteClick = deleteHandler
        )

        allDatesAdapter = DatesAdapter(
            onItemClick = { date ->
                val action = EventsListFragmentDirections.actionEventsListToEditDate(date)
                findNavController().navigate(action)
            },
            onDeleteClick = deleteHandler
        )

        binding.rvUpcomingDates.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingDatesAdapter
        }

        binding.rvAllDates.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allDatesAdapter
        }

        binding.btnToggleAll.setOnClickListener {
            showAll = !showAll
            binding.layoutAllDates.visibility = if (showAll) View.VISIBLE else View.GONE
            binding.btnToggleAll.text = if (showAll) "Скрыть все" else "Показать все"
        }

        binding.fabAddDate.setOnClickListener {
            val now = Calendar.getInstance()
            val action = EventsListFragmentDirections.actionEventsListToAddDate(
                selectedDay = now.get(Calendar.DAY_OF_MONTH),
                selectedMonth = now.get(Calendar.MONTH) + 1,
                selectedYear = now.get(Calendar.YEAR)
            )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewModel.upcomingDates.observe(viewLifecycleOwner) { dates ->
            upcomingDatesAdapter.submitList(dates)
            binding.tvNoUpcoming.visibility = if (dates.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.allDates.observe(viewLifecycleOwner) { dates ->
            allDatesAdapter.submitList(dates)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
