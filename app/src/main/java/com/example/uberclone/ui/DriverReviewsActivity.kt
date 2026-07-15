package com.example.uberclone.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uberclone.R
import com.example.uberclone.adapter.ReviewAdapter
import com.example.uberclone.model.ReviewDisplayModel
import com.example.uberclone.model.ReviewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.facebook.shimmer.ShimmerFrameLayout


class DriverReviewsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerReviews: RecyclerView
    private lateinit var txtAverageRating: TextView
    private lateinit var txtRatingCount: TextView
    private lateinit var txtEmpty: TextView

    private lateinit var adapter: ReviewAdapter

    private val reviewList = ArrayList<ReviewDisplayModel>()

    private lateinit var reviewRef: DatabaseReference

    private lateinit var shimmerLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_reviews)

        toolbar = findViewById(R.id.toolbar)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        recyclerReviews = findViewById(R.id.recyclerReviews)
        txtAverageRating = findViewById(R.id.txtAverageRating)
        txtRatingCount = findViewById(R.id.txtRatingCount)
        txtEmpty = findViewById(R.id.txtEmpty)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        adapter = ReviewAdapter(reviewList)

        recyclerReviews.layoutManager = LinearLayoutManager(this)

        recyclerReviews.adapter = adapter


        loadReviews()
    }

    private fun loadReviews() {
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        recyclerReviews.visibility = View.GONE
        txtEmpty.visibility = View.GONE

        val driverId = FirebaseAuth.getInstance().currentUser!!.uid

        reviewRef = FirebaseDatabase.getInstance()
            .getReference("DriverReviews")
            .child(driverId)

        reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                reviewList.clear()

                if (!snapshot.exists()) {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    txtEmpty.visibility = View.VISIBLE
                    recyclerReviews.visibility = View.GONE

                    adapter.notifyDataSetChanged()
                    return
                }

                    var pendingRequests = snapshot.childrenCount.toInt()

                    for (reviewSnapshot in snapshot.children) {

                        val review = reviewSnapshot.getValue(ReviewModel::class.java)

                        if (review == null) {
                            pendingRequests--

                            if (pendingRequests == 0) {
                                shimmerLayout.stopShimmer()
                                shimmerLayout.visibility = View.GONE
                                adapter.notifyDataSetChanged()

                                txtEmpty.visibility =
                                    if (reviewList.isEmpty()) View.VISIBLE else View.GONE

                                recyclerReviews.visibility =
                                    if (reviewList.isEmpty()) View.GONE else View.VISIBLE
                            }

                            continue
                        }

                        FirebaseDatabase.getInstance()
                            .getReference("Riders")
                            .child(review.riderId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {

                                override fun onDataChange(riderSnapshot: DataSnapshot) {

                                    val firstName = riderSnapshot.child("firstName")
                                        .getValue(String::class.java) ?: ""

                                    val lastName = riderSnapshot.child("lastName")
                                        .getValue(String::class.java) ?: ""

                                    val avatar = riderSnapshot.child("avatar")
                                        .getValue(String::class.java) ?: ""

                                    reviewList.add(
                                        ReviewDisplayModel(
                                            riderName = "$firstName $lastName",
                                            riderAvatar = avatar,
                                            rating = review.rating,
                                            review = review.review,
                                            timestamp = review.timestamp
                                        )
                                    )

                                    pendingRequests--

                                    if (pendingRequests == 0) {
                                        shimmerLayout.stopShimmer()
                                        shimmerLayout.visibility = View.GONE

                                        reviewList.sortByDescending { it.timestamp }
                                        updateRatingSummary()

                                        adapter.notifyDataSetChanged()

                                        txtEmpty.visibility =
                                            if (reviewList.isEmpty()) View.VISIBLE else View.GONE

                                        recyclerReviews.visibility =
                                            if (reviewList.isEmpty()) View.GONE else View.VISIBLE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                    pendingRequests--

                                    if (pendingRequests == 0) {
                                        shimmerLayout.stopShimmer()
                                        shimmerLayout.visibility = View.GONE

                                        reviewList.sortByDescending { it.timestamp }

                                        updateRatingSummary()

                                        adapter.notifyDataSetChanged()

                                        txtEmpty.visibility =
                                            if (reviewList.isEmpty()) View.VISIBLE else View.GONE

                                        recyclerReviews.visibility =
                                            if (reviewList.isEmpty()) View.GONE else View.VISIBLE
                                    }
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE
                    txtEmpty.visibility = View.VISIBLE
                    recyclerReviews.visibility = View.GONE
                }
            })
    }

    private fun updateRatingSummary() {

        val reviewCount = reviewList.size

        if (reviewCount == 0) {
            txtAverageRating.text = "⭐ 0.0"
            txtRatingCount.text = "0 Reviews"
            return
        }

        val averageRating = reviewList.sumOf { it.rating.toDouble() } / reviewCount

        txtAverageRating.text = String.format("⭐ %.1f", averageRating)

        txtRatingCount.text =
            if (reviewCount == 1)
                "1 Review"
            else
                "$reviewCount Reviews"
    }
}