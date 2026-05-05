package com.example.lockin

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class ApiHelper(private val context: Context) {

    private val userPrefs = context.getSharedPreferences("local_users", Context.MODE_PRIVATE)
    private val sessionPrefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val taskPrefs = context.getSharedPreferences("task_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    // ===================== LOCAL SIGNUP =====================
    fun signup(username: String, email: String, pass: String) {
        if (userPrefs.contains(email)) {
            Toast.makeText(context, "User already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        val userJson = JSONObject().apply {
            put("user_id", (100..999).random())
            put("username", username)
            put("email", email)
            put("password", pass)
        }

        userPrefs.edit().putString(email, userJson.toString()).apply()
        
        Toast.makeText(context, "Account Created Locally!", Toast.LENGTH_LONG).show()
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    // ===================== LOCAL LOGIN =====================
    fun login(email: String, pass: String) {
        val userData = userPrefs.getString(email, null)

        if (userData != null) {
            val user = JSONObject(userData)
            val storedPass = user.getString("password")

            if (storedPass == pass) {
                sessionPrefs.edit()
                    .putInt("user_id", user.getInt("user_id"))
                    .putString("username", user.getString("username"))
                    .putString("email", email) // Save email for scoped tasks
                    .apply()

                context.startActivity(Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
                return
            }
        }
        
        Toast.makeText(context, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
    }

    // ===================== LOCAL TASKS PERSISTENCE =====================
    
    fun saveTasks(tasks: List<Task>) {
        val currentUser = sessionPrefs.getString("email", "default_user") ?: "default_user"
        val json = gson.toJson(tasks)
        taskPrefs.edit().putString("tasks_$currentUser", json).apply()
    }

    fun getTasksLocal(): MutableList<Task> {
        val currentUser = sessionPrefs.getString("email", "default_user") ?: "default_user"
        val json = taskPrefs.getString("tasks_$currentUser", null)
        return if (json == null) {
            // Initial sample tasks
            mutableListOf(
                Task(title = "Study UI Design", time = "10:00 AM", status = "Pending"),
                Task(title = "Workout", time = "05:00 PM", status = "Pending")
            )
        } else {
            val type = object : TypeToken<MutableList<Task>>() {}.type
            gson.fromJson(json, type)
        }
    }

    // Compatibility for existing code
    fun getTasks(api: String, callback: (List<Task>?) -> Unit) {
        callback(getTasksLocal())
    }

    fun getDashboard(api: String, callback: (JSONObject?) -> Unit) {
        val tasks = getTasksLocal()
        val data = JSONObject().apply {
            put("completed_tasks", tasks.count { it.status == "Done" || it.status == "Completed" })
            put("pending_tasks", tasks.count { it.status == "Pending" })
        }
        callback(data)
    }
}
