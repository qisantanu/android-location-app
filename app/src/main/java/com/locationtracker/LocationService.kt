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
import com.locationtracker.utils.Logger
import kotlinx.coroutines.*

class LocationService : Service() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRepository: LocationRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isTracking = false
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "LocationServiceChannel"
        private const val LOCATION_INTERVAL = 30000L // 30 seconds
    }

    override fun onCreate() {
        super.onCreate()
        
        Logger.init(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        createNotificationChannel()
        setupLocationCallback()
        Logger.log("LocationService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_TRACKING" -> {
                if (!isTracking) {
                    val apiUrl = intent.getStringExtra("API_URL") ?: "http://192.168.29.181:3000/api/v1/"
                    locationRepository = LocationRepository(this, apiUrl)
                    Logger.log("Using API URL: $apiUrl")
                    
                    startForeground(NOTIFICATION_ID, createNotification())
                    startLocationUpdates()
                    isTracking = true
                    Logger.log("Location tracking started")
                }
            }
            "STOP_TRACKING" -> {
                stopLocationUpdates()
                stopForeground(true)
                stopSelf()
                isTracking = false
                Logger.log("Location tracking stopped")
            }
            else -> {
                locationRepository = LocationRepository(this, "http://192.168.29.181:3000/api/v1/")
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationUpdates()
                isTracking = true
                Logger.log("Location tracking started (default)")
            }
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
            .setContentText("Tracking location every 30 seconds")
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
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Logger.log("Location updates requested successfully")
        } catch (e: SecurityException) {
            Logger.log("Permission error: ${e.message}")
        }
    }
    
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Logger.log("Location updates stopped")
    }

    private fun handleLocationUpdate(location: Location) {
        val locationData = LocationData(
            lat = location.latitude,
            lng = location.longitude,
            trip_details = "---",
            accuracy = location.accuracy,
            timestamp = System.currentTimeMillis()
        )

        Logger.log("Location: ${location.latitude}, ${location.longitude} (±${location.accuracy}m)")

        serviceScope.launch {
            locationRepository.saveLocation(locationData)
            locationRepository.syncLocations()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
        Logger.log("LocationService destroyed")
    }
}
