package com.locationtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.locationtracker.utils.Logger

class MainActivity : AppCompatActivity() {
    
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var btnStartTracking: Button
    private lateinit var btnStopTracking: Button
    private lateinit var tvLog: TextView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var logUpdateRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Logger.init(this)
        
        btnStartTracking = findViewById(R.id.btnStartTracking)
        btnStopTracking = findViewById(R.id.btnStopTracking)
        tvLog = findViewById(R.id.tvLog)
        
        btnStartTracking.setOnClickListener {
            if (hasLocationPermissions()) {
                startLocationService()
            } else {
                requestLocationPermissions()
            }
        }
        
        btnStopTracking.setOnClickListener {
            stopLocationService()
        }
        
        updateLogDisplay()
        startLogUpdates()
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        Logger.log("Requesting location permissions")
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
                Logger.log("Location permissions granted")
                startLocationService()
            } else {
                Logger.log("Location permissions denied")
            }
        }
    }

    private fun startLocationService() {
        Logger.log("Starting location tracking service")
        val serviceIntent = Intent(this, LocationService::class.java)
        serviceIntent.action = "START_TRACKING"
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    
    private fun stopLocationService() {
        Logger.log("Stopping location tracking service")
        val serviceIntent = Intent(this, LocationService::class.java)
        serviceIntent.action = "STOP_TRACKING"
        startService(serviceIntent)
    }
    
    private fun updateLogDisplay() {
        tvLog.text = Logger.getLogs()
    }
    
    private fun startLogUpdates() {
        logUpdateRunnable = Runnable {
            updateLogDisplay()
            handler.postDelayed(logUpdateRunnable, 1000) // Update every second
        }
        handler.post(logUpdateRunnable)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(logUpdateRunnable)
    }
}
