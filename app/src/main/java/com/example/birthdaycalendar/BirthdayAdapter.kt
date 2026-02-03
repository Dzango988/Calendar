package com.example.birthdaycalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BirthdayAdapter(private var items: List<Birthday>) : RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder>() {

    fun submitList(newItems: List<Birthday>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_birthday, parent, false)
        return BirthdayViewHolder(view)
    }

    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class BirthdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)

        fun bind(birthday: Birthday) {
            nameText.text = birthday.name
            dateText.text = birthday.formattedDate()
        }
    }
}
