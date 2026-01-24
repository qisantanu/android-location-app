package com.locationtracker.ui.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.R
import com.locationtracker.data.InfoItem

class InfoItemAdapter(private var infoList: List<InfoItem>) :
    RecyclerView.Adapter<InfoItemAdapter.InfoViewHolder>() {

    class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
        val valueTextView: TextView = itemView.findViewById(R.id.valueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_info, parent, false)
        return InfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        val infoItem = infoList[position]
        holder.labelTextView.text = infoItem.label
        holder.valueTextView.text = infoItem.value
    }

    override fun getItemCount(): Int = infoList.size

    fun updateData(newList: List<InfoItem>) {
        infoList = newList
        notifyDataSetChanged()
    }
}
