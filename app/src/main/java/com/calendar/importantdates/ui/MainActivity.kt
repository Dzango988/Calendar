package com.calendar.importantdates.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlin.math.roundToInt
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.calendar.importantdates.R
import com.calendar.importantdates.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var bannerAd: BannerAdView? = null
    private var bannerLoaded = false
    private var bannerRequested = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handle if needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.calendarFragment, R.id.eventsListFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.calendarFragment) {
                if (!bannerRequested) loadCalendarBanner()
                binding.calendarBannerContainer.visibility =
                    if (bannerLoaded) View.VISIBLE else View.GONE
            } else {
                binding.calendarBannerContainer.visibility = View.GONE
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun loadCalendarBanner() {
        bannerRequested = true
        val container = binding.calendarBannerContainer
        container.post {
            if (isDestroyed || isFinishing) return@post
            val widthPixels = if (container.width > 0) container.width else resources.displayMetrics.widthPixels
            val widthDp = (widthPixels / resources.displayMetrics.density).roundToInt()
            val ad = BannerAdView(this)
            bannerAd = ad
            container.addView(ad, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            ))
            ad.setAdSize(BannerAdSize.sticky(this, widthDp))
            ad.setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {
                    if (isDestroyed || isFinishing) return
                    bannerLoaded = true
                    if (navController.currentDestination?.id == R.id.calendarFragment) {
                        container.visibility = View.VISIBLE
                    }
                }
                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    bannerLoaded = false
                    container.visibility = View.GONE
                }
                override fun onAdClicked() = Unit
                override fun onImpression(impressionData: ImpressionData?) = Unit
            })
            ad.loadAd(AdRequest.Builder("R-M-20479975-1").build())
        }
    }

    override fun onDestroy() {
        bannerAd?.destroy()
        bannerAd = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
