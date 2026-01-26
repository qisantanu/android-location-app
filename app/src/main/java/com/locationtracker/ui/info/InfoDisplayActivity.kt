package com.locationtracker.ui.info

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.R

class InfoDisplayActivity : AppCompatActivity() {

    private lateinit var viewModel: InfoDisplayViewModel
    private lateinit var infoAdapter: InfoItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var refreshButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_display)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Information Display"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        recyclerView = findViewById(R.id.infoRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        refreshButton = findViewById(R.id.refreshButton)

        infoAdapter = InfoItemAdapter(emptyList())
        recyclerView.adapter = infoAdapter

        viewModel = ViewModelProvider(this).get(InfoDisplayViewModel::class.java)

        observeViewModel()

        refreshButton.setOnClickListener {
            viewModel.fetchInfo()
        }
    }

    private fun observeViewModel() {
        viewModel.infoData.observe(this) { infoList ->
            infoAdapter.updateData(infoList)
            recyclerView.visibility = if (infoList.isNotEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            refreshButton.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorTextView.text = errorMessage
            errorTextView.visibility = if (errorMessage != null) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
