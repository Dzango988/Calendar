package com.calendar.importantdates.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calendar.importantdates.databinding.ItemCalendarDayBinding
import com.calendar.importantdates.utils.CalendarDay

class CalendarDayAdapter(
    private val onDayClick: (Int) -> Unit
) : ListAdapter<CalendarDay, CalendarDayAdapter.DayViewHolder>(DiffCallback) {

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay) {
            if (day.dayNumber == 0) {
                binding.tvDay.text = ""
                binding.root.isClickable = false
                binding.dotMarker.visibility = android.view.View.INVISIBLE
                binding.tvDay.background = null
                return
            }

            binding.tvDay.text = day.dayNumber.toString()
            binding.root.isClickable = true

            binding.dotMarker.visibility =
                if (day.hasEvent) android.view.View.VISIBLE else android.view.View.INVISIBLE

            when {
                day.isSelected -> {
                    binding.tvDay.setBackgroundResource(com.calendar.importantdates.R.drawable.bg_day_selected)
                    binding.tvDay.setTextColor(
                        binding.root.context.getColor(com.calendar.importantdates.R.color.white)
                    )
                }
                day.isToday -> {
                    binding.tvDay.setBackgroundResource(com.calendar.importantdates.R.drawable.bg_day_today)
                    binding.tvDay.setTextColor(
                        binding.root.context.getColor(com.calendar.importantdates.R.color.md_theme_primary)
                    )
                }
                day.isWeekend -> {
                    binding.tvDay.background = null
                    binding.tvDay.setTextColor(
                        binding.root.context.getColor(com.calendar.importantdates.R.color.weekend_color)
                    )
                }
                else -> {
                    binding.tvDay.background = null
                    binding.tvDay.setTextColor(
                        binding.root.context.getColor(com.calendar.importantdates.R.color.on_surface)
                    )
                }
            }

            binding.root.setOnClickListener { onDayClick(day.dayNumber) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay) =
            oldItem.dayNumber == newItem.dayNumber && oldItem.isSelected == newItem.isSelected

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay) =
            oldItem == newItem
    }
}
