package com.example.elektronicarebeta1

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        val logo = findViewById<ImageView>(R.id.logoImageView)
        val appName = findViewById<TextView>(R.id.appNameTextView)
        val tagline = findViewById<TextView>(R.id.taglineTextView)

        // Initial state - views are invisible
        logo.alpha = 0f
        appName.alpha = 0f
        tagline.alpha = 0f

        // Create animations
        val logoAnim = createFadeInAnimation(logo, 0)
        val scaleX = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.5f, 1f)
        val appNameAnim = createFadeInAnimation(appName, 300)
        val taglineAnim = createFadeInAnimation(tagline, 600)

        // Combine scale animations
        val scaleAnimSet = AnimatorSet()
        scaleAnimSet.playTogether(scaleX, scaleY)
        scaleAnimSet.interpolator = AccelerateDecelerateInterpolator()
        scaleAnimSet.duration = 500

        // Play all animations together
        AnimatorSet().apply {
            play(logoAnim).with(scaleAnimSet)
            play(appNameAnim).after(logoAnim)
            play(taglineAnim).after(appNameAnim)
            start()
        }

        // Navigate after animations
        android.os.Handler(mainLooper).postDelayed({
            navigateToNextScreen()
        }, 2500)
    }

    private fun createFadeInAnimation(view: View, startDelay: Long): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = 500
            this.startDelay = startDelay
        }
    }

    private fun navigateToNextScreen() {
        val currentUser = auth.currentUser
        val prefs = getSharedPreferences("ElektroniCare", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("isFirstLaunch", true)

        val intent = when {
            currentUser != null -> Intent(this, DashboardActivity::class.java)
            isFirstLaunch -> Intent(this, OnboardingActivity::class.java)
            else -> Intent(this, WelcomeActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
