package com.locationtracker.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.R
import com.locationtracker.data.LogEntry
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter : ListAdapter<LogEntry, LogAdapter.LogViewHolder>(LogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val logEntry = getItem(position)
        holder.bind(logEntry)
    }

    inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logMessage: TextView = itemView.findViewById(R.id.logMessage)
        private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun bind(logEntry: LogEntry) {
            val formattedTime = dateFormat.format(Date(logEntry.timestamp))
            logMessage.text = "[$formattedTime] - [${logEntry.status}] - ${logEntry.message}"

            // Alternate row colors
            if (adapterPosition % 2 == 0) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.logRowEven))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.logRowOdd))
            }

            // Highlight errors
            if (logEntry.status == "ERROR") {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.logErrorBackground))
                logMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.logErrorText))
            } else {
                logMessage.setTextColor(ContextCompat.getColor(itemView.context, R.color.textColor))
            }
        }
    }
}

class LogDiffCallback : DiffUtil.ItemCallback<LogEntry>() {
    override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
        return oldItem == newItem
    }
}
