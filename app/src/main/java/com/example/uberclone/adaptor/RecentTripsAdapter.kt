package com.example.uberclone.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uberclone.model.TripModel
import com.example.uberclone.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentTripsAdapter(
    private val list: List<TripModel>,
    private val listener: OnTripClickListener
) : RecyclerView.Adapter<RecentTripsAdapter.MyViewHolder>() {

    interface OnTripClickListener {
        fun onTripClick(trip: TripModel)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtRoute: TextView = itemView.findViewById(R.id.txtRoute)
        val txtFare: TextView = itemView.findViewById(R.id.txtFare)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recent_trip, parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val trip = list[position]

        holder.txtRoute.text =
            "${trip.origin} → ${trip.destination}"

        holder.txtFare.text =
            "₹%.2f".format(trip.fare)

        holder.txtDate.text =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(trip.timestamp))

        holder.itemView.setOnClickListener {
            listener.onTripClick(trip)
        }
    }
}