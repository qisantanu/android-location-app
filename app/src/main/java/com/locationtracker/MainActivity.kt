package com.locationtracker

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.data.AppPreferences
import com.locationtracker.repository.LogRepository
import com.locationtracker.ui.LogAdapter
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var urlEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var logRecyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter

    private lateinit var logRepository: LogRepository
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logRepository = LogRepository(this)

        setContentView(R.layout.activity_main)

        // Initialize views
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)

        urlEditText = findViewById(R.id.urlEditText)
        saveButton = findViewById(R.id.saveButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        logRecyclerView = findViewById(R.id.logRecyclerView)

        // Set initial URL
        urlEditText.setText(AppPreferences.getBaseUrl(this))

        // Set up button listeners
        saveButton.setOnClickListener {
            val newUrl = urlEditText.text.toString().trim()
            if (newUrl.isNotEmpty()) {
                AppPreferences.setBaseUrl(this, newUrl)
                Toast.makeText(this, "URL saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        startButton.setOnClickListener {
            if (hasLocationPermissions()) {
                startLocationService()
                updateButtonStates()
            } else {
                requestLocationPermissions()
            }
        }

        stopButton.setOnClickListener {
            stopLocationService()
            updateButtonStates()
        }

        // Set up RecyclerView for logs
        logAdapter = LogAdapter()
        logRecyclerView.layoutManager = LinearLayoutManager(this)
        logRecyclerView.adapter = logAdapter

        if (!hasLocationPermissions()) {
            requestLocationPermissions()
        }
        updateButtonStates()

        activityScope.launch { loadLogs() }
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                updateButtonStates()
            } else {
                Toast.makeText(this, "Location permissions denied.", Toast.LENGTH_SHORT).show()
                updateButtonStates()
            }
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        Toast.makeText(this, "Location tracking started.", Toast.LENGTH_SHORT).show()
        activityScope.launch { loadLogs() } // Refresh logs after starting service
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "Location tracking stopped.", Toast.LENGTH_SHORT).show()
        activityScope.launch { loadLogs() } // Refresh logs after stopping service
    }

    private fun isLocationServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun updateButtonStates() {
        val isRunning = isLocationServiceRunning()
        startButton.isEnabled = !isRunning
        stopButton.isEnabled = isRunning
    }

    private suspend fun loadLogs() {
        withContext(Dispatchers.IO) {
            val logs = logRepository.getAllLogs()
            withContext(Dispatchers.Main) {
                logAdapter.submitList(logs)
                logRecyclerView.scrollToPosition(0) // Scroll to top for newest logs
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
        activityScope.launch { loadLogs() } // Refresh logs when activity resumes
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Cancel coroutine scope
    }
}