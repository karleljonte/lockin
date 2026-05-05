package com.example.lockin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splashscreen)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
            // Check for user_id to ensure a valid session
            val userId = prefs.getInt("user_id", -1)

            if (userId != -1) {
                // already logged in → go dashboard
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // not logged in → go login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
