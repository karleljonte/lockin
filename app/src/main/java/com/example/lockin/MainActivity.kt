package com.example.lockin

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private var clickedPosition: Int = -1
    private lateinit var apiHelper: ApiHelper

    private lateinit var currentTaskTitle: TextView
    private lateinit var currentTaskTime: TextView
    private lateinit var doneCount: TextView
    private lateinit var pendingCount: TextView
    private lateinit var missedCount: TextView
    private lateinit var mAdView: AdView

    private var mInterstitialAd: InterstitialAd? = null
    private val TAG = "MainActivity"
    private val INTERSTITIAL_AD_ID = "ca-app-pub-1487691257561134/1349720235"

    // Launcher for TaskDetailsActivity
    private val detailsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val action = result.data?.getStringExtra("action")
            val pos = result.data?.getIntExtra("position", -1) ?: -1

            when (action) {
                "update_status" -> {
                    val newStatus = result.data?.getStringExtra("status") ?: "Pending"
                    if (pos != -1) {
                        taskList[pos].status = newStatus
                        taskAdapter.notifyItemChanged(pos)
                        saveAndRefresh()
                    }
                }
                "delete" -> {
                    if (pos != -1) {
                        taskAdapter.removeTask(pos)
                        saveAndRefresh()
                    }
                }
                "edit" -> {
                    if (pos != -1) {
                        clickedPosition = pos
                        val task = taskList[pos]
                        val intent = Intent(this, AddTaskActivity::class.java).apply {
                            putExtra("edit_title", task.title)
                            putExtra("edit_time", task.time)
                        }
                        addTaskLauncher.launch(intent)
                    }
                }
            }
        }
    }

    private val addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val title = result.data?.getStringExtra("task_title") ?: ""
            val time = result.data?.getStringExtra("task_time") ?: "00:00"
            val hour = result.data?.getIntExtra("hour", -1) ?: -1
            val minute = result.data?.getIntExtra("minute", -1) ?: -1

            if (title.isNotEmpty()) {
                if (clickedPosition != -1) {
                    val task = taskList[clickedPosition]
                    task.title = title
                    task.time = time
                    taskAdapter.updateTask(clickedPosition, task)
                    clickedPosition = -1
                } else {
                    val newTask = Task(title, time, "Pending")
                    taskAdapter.addTask(newTask)
                }
                scheduleNotification(title, hour, minute)
                saveAndRefresh()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {
            loadInterstitialAd()
        }

        apiHelper = ApiHelper(this)
        checkPermissions()

        currentTaskTitle = findViewById(R.id.currentTaskTitle)
        currentTaskTime = findViewById(R.id.currentTaskTime)
        doneCount = findViewById(R.id.doneCount)
        pendingCount = findViewById(R.id.pendingCount)
        missedCount = findViewById(R.id.missedCount)

        // Banner Ad
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val recyclerView = findViewById<RecyclerView>(R.id.taskRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Load persisted tasks
        taskList.clear()
        taskList.addAll(apiHelper.getTasksLocal())

        taskAdapter = TaskAdapter(taskList, 
            onTaskClick = { position, task ->
                val intent = Intent(this, TaskDetailsActivity::class.java).apply {
                    putExtra("title", task.title)
                    putExtra("time", task.time)
                    putExtra("status", task.status)
                    putExtra("position", position)
                }
                detailsLauncher.launch(intent)
            }
        )
        recyclerView.adapter = taskAdapter

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        findViewById<TextView>(R.id.welcomeTxt).text = "Hi ${prefs.getString("username", "User")} 👋"

        findViewById<FloatingActionButton>(R.id.addTaskBtn).setOnClickListener {
            showInterstitialAd {
                clickedPosition = -1
                addTaskLauncher.launch(Intent(this, AddTaskActivity::class.java))
            }
        }

        findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        updateStats()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, INTERSTITIAL_AD_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed.")
                    mInterstitialAd = null
                    loadInterstitialAd() // Load the next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Ad failed to show.")
                    mInterstitialAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }
            mInterstitialAd?.show(this)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
            onAdDismissed()
        }
    }

    private fun saveAndRefresh() {
        apiHelper.saveTasks(taskList)
        updateStats()
    }

    private fun updateStats() {
        val done = taskList.count { it.status == "Done" }
        val pending = taskList.count { it.status == "Pending" }
        val missed = taskList.count { it.status == "Missed" }

        doneCount.text = done.toString()
        pendingCount.text = pending.toString()
        missedCount.text = missed.toString()

        val focusTask = taskList.firstOrNull { it.status == "Pending" }
        currentTaskTitle.text = focusTask?.title ?: "All Clear! 🚀"
        currentTaskTime.text = focusTask?.time ?: "--:--"
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }

    private fun scheduleNotification(title: String, hour: Int, minute: Int) {
        if (hour == -1 || minute == -1) return
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TaskReminderReceiver::class.java).apply { putExtra("task_title", title) }
        val pi = PendingIntent.getBroadcast(this, title.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }
}
