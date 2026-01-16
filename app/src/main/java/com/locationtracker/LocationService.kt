package com.locationtracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.locationtracker.data.LocationData
import com.locationtracker.repository.LocationRepository
import com.locationtracker.repository.LogRepository
import kotlinx.coroutines.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRepository: LocationRepository
    private lateinit var logRepository: LogRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob()) // New Main Scope

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "LocationServiceChannel"
        private const val LOCATION_INTERVAL = 30000L // 30 seconds
    }

    override fun onCreate() {
        super.onCreate()
        try {
            logRepository = LogRepository(this)
            log("INFO", "LocationService: onCreate called.")

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            locationRepository = LocationRepository(this, logRepository) // Updated constructor

            createNotificationChannel()
            setupLocationCallback()
            mainScope.launch { // Clean old logs on service start, using mainScope
                logRepository.cleanOldLogs()
            }
            log("INFO", "Location service created.")
        } catch (e: Exception) {
            log("CRITICAL", "LocationService: onCreate crashed: ${e.message}")
            stopSelf() // Stop the service if it crashed during creation
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            log("INFO", "LocationService: onStartCommand called.")
            startForeground(NOTIFICATION_ID, createNotification())
            startLocationUpdates()
            log("INFO", "Location tracking started.")
        } catch (e: Exception) {
            log("CRITICAL", "LocationService: onStartCommand crashed: ${e.message}")
            stopSelf() // Stop the service if it crashed during command handling
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking location in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            LOCATION_INTERVAL
        ).build()

        try {
            mainScope.launch { // Ensure requestLocationUpdates is called on main thread
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                log("INFO", "Location updates requested.")
            }
        } catch (e: SecurityException) {
            log("ERROR", "Location permission missing: ${e.message}")
        } catch (e: Exception) {
            log("ERROR", "Failed to request location updates: ${e.message}")
        }
    }

    private fun handleLocationUpdate(location: Location) {
        log("INFO", "New location received: Lat=${location.latitude}, Lng=${location.longitude}, Acc=${location.accuracy}")
        val locationData = LocationData(
            lat = location.latitude,
            lng = location.longitude,
            trip_details = "---",
            accuracy = location.accuracy,
            timestamp = System.currentTimeMillis()
        )

        serviceScope.launch { // Save and sync operations on IO dispatcher
            try {
                locationRepository.saveLocation(locationData)
                log("INFO", "Location saved locally.")
                locationRepository.syncLocations() // Let the repository handle the batching logic
            } catch (e: Exception) {
                log("ERROR", "Error in handleLocationUpdate (save/sync): ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            log("INFO", "Stopping location updates and syncing remaining data.")
            fusedLocationClient.removeLocationUpdates(locationCallback)

            // Perform a final, blocking sync to ensure all data is sent.
            runBlocking {
                try {
                    val unsyncedCount = locationRepository.getUnsyncedCount()
                    if (unsyncedCount > 0) {
                        log("INFO", "Performing final sync of $unsyncedCount locations.")
                        locationRepository.syncLocations()
                    } else {
                        log("INFO", "No unsynced locations to sync on shutdown.")
                    }
                } catch (e: Exception) {
                    log("ERROR", "Final sync failed: ${e.message}")
                }
            }

            // Cancel coroutine scopes after all operations are complete.
            serviceScope.cancel()
            mainScope.cancel()
            log("INFO", "Location tracking stopped. Service destroyed.")
        } catch (e: Exception) {
            log("CRITICAL", "LocationService: onDestroy crashed: ${e.message}")
        }
    }

    private fun log(status: String, message: String) {
        // Ensure logRepository is initialized before trying to log
        if (this::logRepository.isInitialized) {
            serviceScope.launch { // Log on IO dispatcher
                logRepository.insertLog(status, message)
            }
        } else {
            // Fallback logging if logRepository isn't ready (shouldn't happen after onCreate)
            android.util.Log.e("LocationService", "LogRepository not initialized: $message")
        }
    }
}
