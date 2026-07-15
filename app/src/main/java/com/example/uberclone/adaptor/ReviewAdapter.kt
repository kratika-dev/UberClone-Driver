package com.example.uberclone.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uberclone.R
import com.example.uberclone.model.ReviewDisplayModel
import com.example.uberclone.model.ReviewModel
import com.google.android.gms.maps.model.Circle
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(
    private val reviewList: List<ReviewDisplayModel>
) : RecyclerView.Adapter<ReviewAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtRiderName: TextView =
            itemView.findViewById(R.id.txtRiderName)

        val txtRating: TextView =
            itemView.findViewById(R.id.txtRating)

        val txtReview: TextView =
            itemView.findViewById(R.id.txtReview)

        val txtDate: TextView =
            itemView.findViewById(R.id.txtDate)

        val imgRider =
            itemView.findViewById<CircleImageView>(R.id.imgRider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount() = reviewList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val review = reviewList[position]

        holder.txtRating.text =
            "⭐ %.1f".format(review.rating)

        holder.txtReview.text =
            review.review

        holder.txtDate.text =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(review.timestamp))

        holder.txtRiderName.text = review.riderName

        Log.d("REVIEW_AVATAR", review.riderAvatar)

        if (review.riderAvatar.isNotEmpty()) {

            Picasso.get()
                .load(review.riderAvatar)
                .placeholder(R.drawable.profile)   // Shown while loading
                .error(R.drawable.profile)
                .into(holder.imgRider)

        }

    }
}