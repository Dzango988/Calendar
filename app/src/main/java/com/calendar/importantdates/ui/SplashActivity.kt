package com.calendar.importantdates.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.calendar.importantdates.databinding.ActivitySplashBinding
import com.calendar.importantdates.ui.onboarding.OnboardingActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Анимация логотипа
        binding.ivLogo.alpha = 0f
        binding.tvAppName.alpha = 0f
        binding.tvTagline.alpha = 0f

        binding.ivLogo.animate().alpha(1f).setDuration(600).start()
        binding.tvAppName.animate().alpha(1f).setStartDelay(300).setDuration(600).start()
        binding.tvTagline.animate().alpha(1f).setStartDelay(600).setDuration(600).start()

        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (OnboardingActivity.isOnboardingDone(this)) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(next)
            finish()
        }, 1800)
    }
}
