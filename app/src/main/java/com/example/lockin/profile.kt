package com.example.lockin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        apiHelper = ApiHelper(this)

        // 🔹 CONNECT VIEWS
        val profileUsername = findViewById<TextView>(R.id.profileUsername)
        val profileEmail = findViewById<TextView>(R.id.profileEmail)

        val totalTasks = findViewById<TextView>(R.id.totalTasks)
        val completedTasks = findViewById<TextView>(R.id.completedTasks)
        val pendingTasks = findViewById<TextView>(R.id.pendingTasks)

        val clearTasksBtn = findViewById<Button>(R.id.clearTasksBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        // 🔹 LOAD USER DATA
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        profileUsername.text = prefs.getString("username", "User")
        profileEmail.text = prefs.getString("email", "email@example.com")

        // 🔹 GET REAL TASK DATA
        val tasks = apiHelper.getTasksLocal()

        fun updateUI() {
            totalTasks.text = tasks.size.toString()
            completedTasks.text = tasks.count { it.status == "Done" }.toString()
            pendingTasks.text = tasks.count { it.status == "Pending" }.toString()
        }

        updateUI()

        // 🔹 CLEAR TASKS
        clearTasksBtn.setOnClickListener {
            tasks.clear()
            apiHelper.saveTasks(tasks)
            updateUI()
        }

        // 🔹 LOGOUT
        logoutBtn.setOnClickListener {
            prefs.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}
