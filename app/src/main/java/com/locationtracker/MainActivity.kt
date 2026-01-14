package com.locationtracker

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.locationtracker.data.AppPreferences

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var urlEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Basic UI
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        urlEditText = EditText(this).apply {
            hint = "Enter backend URL"
            setText(AppPreferences.getBaseUrl(this@MainActivity))
        }

        saveButton = Button(this).apply {
            text = "Save URL"
            setOnClickListener {
                val newUrl = urlEditText.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    AppPreferences.setBaseUrl(this@MainActivity, newUrl)
                    Toast.makeText(this@MainActivity, "URL saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "URL cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }

        startButton = Button(this).apply {
            text = "Start Tracking"
            setOnClickListener {
                if (hasLocationPermissions()) {
                    startLocationService()
                    updateButtonStates()
                } else {
                    requestLocationPermissions()
                }
            }
        }

        stopButton = Button(this).apply {
            text = "Stop Tracking"
            setOnClickListener {
                stopLocationService()
                updateButtonStates()
            }
        }

        layout.addView(urlEditText)
        layout.addView(saveButton)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)

        if (!hasLocationPermissions()) {
            requestLocationPermissions()
        }
        updateButtonStates()
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
        val hasPermissions = hasLocationPermissions()

        startButton.isEnabled = !isRunning && hasPermissions
        stopButton.isEnabled = isRunning
    }

    override fun onResume() {
        super.onResume()
        updateButtonStates()
    }
}