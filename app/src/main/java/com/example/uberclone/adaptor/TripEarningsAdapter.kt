package com.example.uberclone.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uberclone.R
import com.example.uberclone.model.EarningModel
import com.example.uberclone.model.TripModel
import java.text.SimpleDateFormat
import java.util.*

class TripEarningsAdapter(
    private val list: List<EarningModel>
) : RecyclerView.Adapter<TripEarningsAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtRoute: TextView =
            itemView.findViewById(R.id.txtRoute)

        val txtFare: TextView =
            itemView.findViewById(R.id.txtFare)

        val txtDate: TextView =
            itemView.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_earning, parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = list[position]

        holder.txtRoute.text =
            "${item.origin} → ${item.destination}"

        holder.txtFare.text =
            "₹%.1f".format(item.fare)

        holder.txtDate.text =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            ).format(Date(item.timestamp))
    }


}