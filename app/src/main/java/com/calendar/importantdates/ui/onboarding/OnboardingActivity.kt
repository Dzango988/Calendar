package com.calendar.importantdates.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.calendar.importantdates.databinding.ActivityOnboardingBinding
import com.calendar.importantdates.ui.MainActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    data class OnboardingPage(
        val emoji: String,
        val title: String,
        val description: String,
        val backgroundColor: Int
    )

    private val pages = listOf(
        OnboardingPage(
            emoji = "📅",
            title = "Важные даты",
            description = "Храните все важные даты в одном месте — дни рождения, праздники, годовщины и личные события",
            backgroundColor = android.graphics.Color.parseColor("#6750A4")
        ),
        OnboardingPage(
            emoji = "🔔",
            title = "Напоминания",
            description = "Настраивайте напоминания заранее — за 1, 3 или 7 дней до события, чтобы ничего не пропустить",
            backgroundColor = android.graphics.Color.parseColor("#0891B2")
        ),
        OnboardingPage(
            emoji = "🎨",
            title = "Категории и цвета",
            description = "Разделяйте события по категориям: дни рождения, годовщины, праздники, работа и здоровье",
            backgroundColor = android.graphics.Color.parseColor("#059669")
        ),
        OnboardingPage(
            emoji = "📱",
            title = "Виджет",
            description = "Добавьте виджет на рабочий стол — видите обратный отсчёт до ближайшего события прямо с главного экрана",
            backgroundColor = android.graphics.Color.parseColor("#DC2626")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = OnboardingPagerAdapter(pages)
        binding.viewPager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val isLast = position == pages.size - 1
                binding.btnNext.text = if (isLast) "Начать" else "Далее"
                binding.btnSkip.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
            }
        })

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pages.size - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener { finishOnboarding() }
    }

    private fun finishOnboarding() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_ONBOARDING_DONE = "onboarding_done"

        fun isOnboardingDone(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ONBOARDING_DONE, false)
        }
    }
}
