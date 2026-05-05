package com.example.lockin

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private var selectedHour = -1
    private var selectedMinute = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val taskTitleInput = findViewById<EditText>(R.id.taskTitleInput)
        val pickTimeBtn = findViewById<Button>(R.id.pickTimeBtn)
        val selectedTimeTxt = findViewById<TextView>(R.id.selectedTimeTxt)
        val saveTaskBtn = findViewById<Button>(R.id.saveTaskBtn)

        // Check if we are in Edit Mode
        val editTitle = intent.getStringExtra("edit_title")
        val editTime = intent.getStringExtra("edit_time")

        if (editTitle != null) {
            taskTitleInput.setText(editTitle)
            selectedTimeTxt.text = editTime
            // Extract hour and minute if needed, for simplicity we'll just let them pick again if they want to change
        }

        pickTimeBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                selectedHour = h
                selectedMinute = m
                selectedTimeTxt.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        saveTaskBtn.setOnClickListener {
            val title = taskTitleInput.text.toString().trim()
            val timeStr = selectedTimeTxt.text.toString()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            resultIntent.putExtra("task_title", title)
            resultIntent.putExtra("task_time", timeStr)
            resultIntent.putExtra("hour", selectedHour)
            resultIntent.putExtra("minute", selectedMinute)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
