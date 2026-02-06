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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.data.AppPreferences
import com.locationtracker.repository.LogRepository
import com.locationtracker.ui.LogAdapter
import com.locationtracker.viewmodel.TrackingViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var urlEditText: EditText
    private lateinit var editUrlButton: ImageButton
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var logRecyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter
    private lateinit var distanceTextView: TextView
    private lateinit var locationTextView: TextView

    private lateinit var logRepository: LogRepository
    private lateinit var trackingViewModel: TrackingViewModel
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logRepository = LogRepository(this)
        trackingViewModel = ViewModelProvider(this)[TrackingViewModel::class.java]

        setContentView(R.layout.activity_main)

        // Initialize views
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)

        urlEditText = findViewById(R.id.urlEditText)
        editUrlButton = findViewById(R.id.editUrlButton)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        logRecyclerView = findViewById(R.id.logRecyclerView)
        distanceTextView = findViewById(R.id.distanceTextView)
        locationTextView = findViewById(R.id.locationTextView)
        val versionTextView = findViewById<TextView>(R.id.versionTextView)

        // Set version text
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionTextView.text = "Version ${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            versionTextView.text = "Version 1.3 (4)"
        }

        // Set initial URL
        urlEditText.setText(AppPreferences.getBaseUrl(this))
        urlEditText.setOnClickListener {
            if (!urlEditText.isFocusable) {
                // If not editable, make it editable on click
                makeUrlEditable(true)
            }
        }

        urlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newUrl = urlEditText.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    AppPreferences.setBaseUrl(this, newUrl)
                    makeUrlEditable(false)
                    Toast.makeText(this, "URL saved!", Toast.LENGTH_SHORT).show()
                } else {
                    // Revert to the old URL if the field is cleared
                    urlEditText.setText(AppPreferences.getBaseUrl(this))
                    Toast.makeText(this, "URL cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        editUrlButton.setOnClickListener {
            makeUrlEditable(!urlEditText.isFocusable)
        }

        startButton.setOnClickListener {
            val currentUrl = urlEditText.text.toString().trim()
            if (currentUrl.isNotEmpty()) {
                if (hasLocationPermissions()) {
                    startLocationService()
                    trackingViewModel.startTracking(this)
                    updateButtonStates()
                } else {
                    requestLocationPermissions()
                }
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        stopButton.setOnClickListener {
            stopLocationService()
            trackingViewModel.stopTracking(this)
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

        observeLogs() // Start observing logs
        observeTrackingData() // Start observing tracking data
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
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "Location tracking stopped.", Toast.LENGTH_SHORT).show()
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

    private fun observeLogs() {
        lifecycleScope.launch {
            logRepository.getAllLogs().collect { logs ->
                logAdapter.submitList(logs)
                // Scroll to top for newest logs if there are any
                if (logs.isNotEmpty()) {
                    logRecyclerView.scrollToPosition(0)
                }
            }
        }
    }

    private fun observeTrackingData() {
        lifecycleScope.launch {
            trackingViewModel.distance.collect { distance ->
                distanceTextView.text = "$distance m"
            }
        }
        
        lifecycleScope.launch {
            trackingViewModel.location.collect { location ->
                locationTextView.text = location
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }

    private fun makeUrlEditable(editable: Boolean) {
        urlEditText.isFocusable = editable
        urlEditText.isFocusableInTouchMode = editable
        urlEditText.isCursorVisible = editable
        if (editable) {
            urlEditText.requestFocus()
            urlEditText.setSelection(urlEditText.text.length)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel() // Cancel coroutine scope
    }
}
