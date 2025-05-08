package com.example.elektronicarebeta1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Initialize buttons
        val createAccountButton = findViewById<Button>(R.id.create_account_button)
        val signInButton = findViewById<Button>(R.id.sign_in_button)

        // Set click listeners
        createAccountButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        signInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
