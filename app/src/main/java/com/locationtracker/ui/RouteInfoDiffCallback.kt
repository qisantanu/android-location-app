package com.locationtracker.ui

import androidx.recyclerview.widget.DiffUtil
import com.locationtracker.data.RouteDetail

class RouteInfoDiffCallback(
    private val oldList: List<Pair<String, RouteDetail>>,
    private val newList: List<Pair<String, RouteDetail>>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].first == newList[newItemPosition].first
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
