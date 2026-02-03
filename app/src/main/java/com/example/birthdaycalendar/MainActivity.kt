package com.example.birthdaycalendar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.DateTimeException
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var repository: BirthdayRepository
    private lateinit var adapter: BirthdayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = BirthdayRepository(this)
        adapter = BirthdayAdapter(repository.getAll())

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        val addButton = findViewById<Button>(R.id.addButton)
        val listView = findViewById<RecyclerView>(R.id.birthdayList)

        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = adapter

        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val dateText = dateInput.text.toString().trim()
            if (name.isBlank() || dateText.isBlank()) {
                Toast.makeText(this, "Заполните имя и дату", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsed = parseDate(dateText)
            if (parsed == null) {
                Toast.makeText(this, "Введите дату в формате дд.мм", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            repository.add(Birthday(name, parsed.first, parsed.second))
            adapter.submitList(repository.getAll())
            NotificationScheduler.scheduleAll(this)
            BirthdayWidgetProvider.updateAllWidgets(this)

            nameInput.text.clear()
            dateInput.text.clear()
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.submitList(repository.getAll())
        BirthdayWidgetProvider.updateAllWidgets(this)
    }

    private fun parseDate(input: String): Pair<Int, Int>? {
        val parts = input.split(".")
        if (parts.size != 2) return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        return try {
            LocalDate.of(2000, month, day)
            Pair(day, month)
        } catch (ex: DateTimeException) {
            null
        }
    }
}
