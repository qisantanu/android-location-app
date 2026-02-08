package com.locationtracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.R

class InfoAdapter(private var data: Map<String, String>) : RecyclerView.Adapter<InfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val keyTextView: TextView = view.findViewById(R.id.keyTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.info_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = data.keys.elementAt(position)
        holder.keyTextView.text = key
        holder.valueTextView.text = data[key]
    }

    override fun getItemCount() = data.size

    fun updateData(newData: Map<String, String>) {
        data = newData
        notifyDataSetChanged()
    }
}
