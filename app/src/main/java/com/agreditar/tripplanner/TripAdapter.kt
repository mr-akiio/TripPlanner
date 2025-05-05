package com.agreditar.tripplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripAdapter(
    private val tripList: List<Trip>,
    private val onItemClickListener: (Trip) -> Unit // Click listener
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destinationTextView: TextView = itemView.findViewById(R.id.destinationTextView)
        val datesTextView: TextView = itemView.findViewById(R.id.datesTextView)
        // Add other TextViews for trip data here
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = tripList[position]
        holder.destinationTextView.text = currentTrip.destination
        holder.datesTextView.text = "${currentTrip.startDate} - ${currentTrip.endDate}"
        // Set other trip data here

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClickListener(currentTrip) // Pass the trip data to the listener
        }
    }

    override fun getItemCount() = tripList.size
}