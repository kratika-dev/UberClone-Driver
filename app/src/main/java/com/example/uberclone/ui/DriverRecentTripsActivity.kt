package com.example.uberclone.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uberclone.R
import com.example.uberclone.adaptor.RecentTripsAdapter
import com.example.uberclone.model.TripModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DriverRecentTripsActivity : AppCompatActivity(),
    RecentTripsAdapter.OnTripClickListener {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerTrips: RecyclerView
    private lateinit var txtEmpty: TextView

    private lateinit var adapter: RecentTripsAdapter

    private val tripList = ArrayList<TripModel>()

    private lateinit var shimmerLayout: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_recent_trips)


        initViews()

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapter = RecentTripsAdapter(tripList, this)

        recyclerTrips.layoutManager = LinearLayoutManager(this)
        recyclerTrips.adapter = adapter

        loadTrips()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        recyclerTrips = findViewById(R.id.recyclerTrips)
        txtEmpty = findViewById(R.id.txtEmpty)
    }

    private fun loadTrips() {
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        recyclerTrips.visibility = View.GONE
        txtEmpty.visibility = View.GONE

        val driverId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("Trips")
            .orderByChild("driverId")
            .equalTo(driverId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    tripList.clear()

                    for (tripSnapshot in snapshot.children) {

                        val trip = tripSnapshot.getValue(TripModel::class.java)

                        if (trip != null) {
                            tripList.add(trip)
                        }
                    }

                    tripList.sortByDescending { it.timestamp }

                    // Hide shimmer
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE

                    if (tripList.isEmpty()) {
                        txtEmpty.visibility = View.VISIBLE
                        recyclerTrips.visibility = View.GONE
                    } else {
                        txtEmpty.visibility = View.GONE
                        recyclerTrips.visibility = View.VISIBLE
                    }

                    adapter.notifyDataSetChanged()

                    txtEmpty.visibility =
                        if (tripList.isEmpty()) View.VISIBLE
                        else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.visibility = View.GONE

                    txtEmpty.visibility = View.VISIBLE
                    recyclerTrips.visibility = View.GONE
                }
            })
    }

    override fun onTripClick(trip: TripModel) {

        val intent = Intent(this, DriverTripDetailsActivity::class.java)
        intent.putExtra("tripData", trip)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}