package com.locationtracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.locationtracker.R
import com.locationtracker.data.RouteDetail

class RouteInfoAdapter(private var routeInfoList: List<Pair<String, RouteDetail>>) :
    RecyclerView.Adapter<RouteInfoAdapter.RouteInfoViewHolder>() {

    class RouteInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeTextView: TextView = itemView.findViewById(R.id.placeTextView)
        val distanceTextView: TextView = itemView.findViewById(R.id.distanceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteInfoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.route_info_item, parent, false)
        return RouteInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteInfoViewHolder, position: Int) {
        val (place, routeDetail) = routeInfoList[position]
        holder.placeTextView.text = place
        holder.distanceTextView.text = "${routeDetail.distance} meters"
    }

    override fun getItemCount(): Int {
        return routeInfoList.size
    }

    fun updateData(newRouteInfoList: List<Pair<String, RouteDetail>>) {
        routeInfoList = newRouteInfoList
        notifyDataSetChanged()
    }
}
