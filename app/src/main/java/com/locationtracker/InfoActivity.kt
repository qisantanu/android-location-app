package com.locationtracker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.ui.InfoAdapter
import com.locationtracker.ui.RouteInfoAdapter
import com.locationtracker.viewmodel.InfoViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InfoActivity : AppCompatActivity() {

    private lateinit var infoViewModel: InfoViewModel
    private lateinit var infoRecyclerView: RecyclerView
    private lateinit var infoAdapter: InfoAdapter
    private lateinit var refreshButton: Button
    private lateinit var routeInfoRecyclerView: RecyclerView
    private lateinit var routeInfoAdapter: RouteInfoAdapter
    private lateinit var routeInfoTitle: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        infoViewModel = ViewModelProvider(this).get(InfoViewModel::class.java)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        infoRecyclerView = findViewById(R.id.infoRecyclerView)
        refreshButton = findViewById(R.id.refreshButton)
        routeInfoRecyclerView = findViewById(R.id.routeInfoRecyclerView)
        routeInfoTitle = findViewById(R.id.routeInfoTitle)


        infoAdapter = InfoAdapter(emptyMap())
        infoRecyclerView.layoutManager = LinearLayoutManager(this)
        infoRecyclerView.adapter = infoAdapter

        routeInfoAdapter = RouteInfoAdapter(emptyList())
        routeInfoRecyclerView.layoutManager = LinearLayoutManager(this)
        routeInfoRecyclerView.adapter = routeInfoAdapter

        refreshButton.setOnClickListener {
            infoViewModel.fetchInfo(this)
        }

        lifecycleScope.launch {
            infoViewModel.infoData.collect { data ->
                infoAdapter.updateData(data)
            }
        }

        lifecycleScope.launch {
            infoViewModel.routeInfo.collect { routeList ->
                routeInfoAdapter.updateData(routeList)
                routeInfoTitle.visibility = if (routeList.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        infoViewModel.fetchInfo(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
