package com.calendar.importantdates.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.calendar.importantdates.data.db.ImportantDate
import com.calendar.importantdates.databinding.ItemDateBinding
import com.calendar.importantdates.utils.CalendarGridHelper

class DatesAdapter(
    private val onItemClick: (ImportantDate) -> Unit,
    private val onDeleteClick: ((ImportantDate) -> Unit)?
) : ListAdapter<ImportantDate, DatesAdapter.DateViewHolder>(DiffCallback) {

    inner class DateViewHolder(private val binding: ItemDateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: ImportantDate) {
            binding.tvTitle.text = date.title
            binding.tvCategory.text = "${date.category.emoji} ${date.category.displayName}"
            binding.tvDate.text = buildDateText(date)
            binding.tvDescription.text = date.description

            if (date.description.isBlank()) {
                binding.tvDescription.visibility = android.view.View.GONE
            } else {
                binding.tvDescription.visibility = android.view.View.VISIBLE
            }

            try {
                binding.colorIndicator.setBackgroundColor(Color.parseColor(date.colorHex))
            } catch (e: IllegalArgumentException) {
                binding.colorIndicator.setBackgroundColor(Color.parseColor("#FF6B6B"))
            }

            binding.root.setOnClickListener { onItemClick(date) }

            if (onDeleteClick != null) {
                binding.btnDelete.visibility = android.view.View.VISIBLE
                binding.btnDelete.setOnClickListener { onDeleteClick.invoke(date) }
            } else {
                binding.btnDelete.visibility = android.view.View.GONE
            }
        }

        private fun buildDateText(date: ImportantDate): String {
            val monthNames = arrayOf(
                "янв", "фев", "мар", "апр", "май", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"
            )
            val monthName = monthNames.getOrNull(date.month - 1) ?: ""
            return if (date.year != null) {
                "${date.day} $monthName ${date.year}"
            } else {
                "${date.day} $monthName (каждый год)"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ImportantDate>() {
        override fun areItemsTheSame(oldItem: ImportantDate, newItem: ImportantDate) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ImportantDate, newItem: ImportantDate) =
            oldItem == newItem
    }
}
