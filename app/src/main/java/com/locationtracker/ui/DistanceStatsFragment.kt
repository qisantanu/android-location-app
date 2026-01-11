package com.locationtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textview.MaterialTextView
import com.locationtracker.BuildConfig
import com.locationtracker.R
import com.locationtracker.viewmodel.DistanceStatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DistanceStatsFragment : Fragment() {
    
    private val viewModel: DistanceStatsViewModel by viewModels()
    
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var totalDistanceText: MaterialTextView
    private lateinit var distanceLast60MinText: MaterialTextView
    private lateinit var averageSpeedText: MaterialTextView
    private lateinit var tripDurationText: MaterialTextView
    private lateinit var activeTripsText: MaterialTextView
    private lateinit var locationsCountText: MaterialTextView
    private lateinit var lastUpdatedText: MaterialTextView
    private lateinit var appVersionText: MaterialTextView
    private lateinit var loadingIndicator: View
    private lateinit var errorMessage: MaterialTextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_distance_stats, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        // Set version text after views are initialized
        appVersionText.text = "v${BuildConfig.VERSION_NAME}"
        setupObservers()
        setupSwipeRefresh()
    }
    
    private fun initializeViews(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        totalDistanceText = view.findViewById(R.id.totalDistanceValue)
        distanceLast60MinText = view.findViewById(R.id.distanceLast60MinValue)
        averageSpeedText = view.findViewById(R.id.averageSpeedValue)
        tripDurationText = view.findViewById(R.id.tripDurationValue)
        activeTripsText = view.findViewById(R.id.activeTripsValue)
        locationsCountText = view.findViewById(R.id.locationsCountValue)
        lastUpdatedText = view.findViewById(R.id.lastUpdatedValue)
        appVersionText = view.findViewById(R.id.appVersionValue)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        errorMessage = view.findViewById(R.id.errorMessage)
    }
    
    private fun setupObservers() {
        viewModel.distanceStats.observe(viewLifecycleOwner) { stats ->
            if (stats != null) {
                updateUI(stats)
                errorMessage.visibility = View.GONE
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                errorMessage.text = "Error: $error"
                errorMessage.visibility = View.VISIBLE
            }
        }
        
        viewModel.lastUpdated.observe(viewLifecycleOwner) { timestamp ->
            if (timestamp > 0) {
                updateLastUpdatedTime(timestamp)
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.manualRefresh()
        }
    }
    
    private fun updateUI(stats: com.locationtracker.data.DistanceStats) {
        totalDistanceText.text = String.format("%.2f km", stats.totalDistanceToday)
        distanceLast60MinText.text = String.format("%.2f km", stats.distanceLast60Minutes)
        averageSpeedText.text = String.format("%.2f km/h", stats.averageSpeed)
        
        // Convert milliseconds to HH:MM:SS
        val hours = stats.tripDuration / 3600000
        val minutes = (stats.tripDuration % 3600000) / 60000
        val seconds = (stats.tripDuration % 60000) / 1000
        tripDurationText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        
        activeTripsText.text = stats.activeTrips.toString()
        locationsCountText.text = stats.locationsCount.toString()
    }
    
    private fun updateLastUpdatedTime(timestamp: Long) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        lastUpdatedText.text = sdf.format(date)
    }
}
