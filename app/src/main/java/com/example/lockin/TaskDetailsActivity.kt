package com.example.lockin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TaskDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        val title = intent.getStringExtra("title") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val status = intent.getStringExtra("status") ?: "Pending"
        val position = intent.getIntExtra("position", -1)

        val detailTitle = findViewById<TextView>(R.id.detailTitle)
        val detailTime = findViewById<TextView>(R.id.detailTime)
        val detailStatus = findViewById<TextView>(R.id.detailStatus)

        detailTitle.text = title
        detailTime.text = time
        detailStatus.text = "Status: $status"

        findViewById<Button>(R.id.markDoneBtn).setOnClickListener {
            finishWithResult("Done", position)
        }

        findViewById<Button>(R.id.markMissedBtn).setOnClickListener {
            finishWithResult("Missed", position)
        }

        findViewById<Button>(R.id.editTaskBtn).setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("action", "edit")
            resultIntent.putExtra("position", position)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        findViewById<Button>(R.id.deleteTaskBtn).setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("action", "delete")
            resultIntent.putExtra("position", position)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun finishWithResult(newStatus: String, position: Int) {
        val resultIntent = Intent()
        resultIntent.putExtra("action", "update_status")
        resultIntent.putExtra("status", newStatus)
        resultIntent.putExtra("position", position)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
