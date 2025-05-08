package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button

    private val onboardingPages = listOf(
        OnboardingPage(
            R.drawable.ic_phone_outline,
            "Phone Repairs",
            "Expert repairs for all smartphone brands with genuine parts"
        ),
        OnboardingPage(
            R.drawable.ic_laptop,
            "Laptop Services",
            "Professional laptop repair and maintenance services"
        ),
        OnboardingPage(
            R.drawable.ic_tv,
            "TV Repairs",
            "Specialized repair for all your TV devices"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        setupViews()
        setupViewPager()
        setupButtons()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        indicatorContainer = findViewById(R.id.indicatorContainer)
        nextButton = findViewById(R.id.nextButton)
        skipButton = findViewById(R.id.skipButton)
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingPages)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = true

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateButtonText(position)
            }
        })

        indicatorContainer.removeAllViews()
        for (i in onboardingPages.indices) {
            val indicator = View(this).apply {
                setBackgroundResource(R.drawable.circle_purple_bg)
                layoutParams = LinearLayout.LayoutParams(12, 12).apply {
                    marginStart = 8
                    marginEnd = 8
                }
            }
            indicatorContainer.addView(indicator)
        }
        updateIndicators(0)
    }

    private fun setupButtons() {
        nextButton.setOnClickListener {
            if (viewPager.currentItem < onboardingPages.size - 1) {
                viewPager.currentItem++
            } else {
                val prefs = getSharedPreferences("ElektroniCare", MODE_PRIVATE)
                prefs.edit().putBoolean("isFirstLaunch", false).apply()

                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        skipButton.setOnClickListener {
            val prefs = getSharedPreferences("ElektroniCare", MODE_PRIVATE)
            prefs.edit().putBoolean("isFirstLaunch", false).apply()

            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicatorContainer.childCount) {
            val indicator = indicatorContainer.getChildAt(i)
            indicator.setBackgroundResource(
                if (i == position) R.drawable.circle_bg
                else R.drawable.circle_purple_bg
            )
        }
    }

    private fun updateButtonText(position: Int) {
        nextButton.text = if (position == onboardingPages.size - 1) "Get Started" else "Next"
    }
}

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)
