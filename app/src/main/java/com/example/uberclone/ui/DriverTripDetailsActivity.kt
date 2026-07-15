package com.example.uberclone.ui

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.exceptions.domerrors.DataError
import com.example.uberclone.R
import com.example.uberclone.model.DriverReviewModel
import com.example.uberclone.model.RiderInfoModel
import com.example.uberclone.model.TripModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class DriverTripDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var trip: TripModel

    private lateinit var txtPickup: TextView
    private lateinit var txtDestination: TextView
    private lateinit var txtDistance: TextView
    private lateinit var txtDuration: TextView
    private lateinit var txtFare: TextView

    private lateinit var txtRiderName: TextView
    private lateinit var txtRiderPhone: TextView
    private lateinit var txtRating: TextView
    private lateinit var txtReview: TextView

    private lateinit var imgDriver: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_trip_details)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        initViews()

        trip = intent.getSerializableExtra("tripData") as TripModel

        setupMap()
        loadTripData()
        loadRiderInfo()
        loadReview()
    }

    private fun initViews() {

        imgDriver = findViewById(R.id.imgDriver)
        txtPickup = findViewById(R.id.txtPickup)
        txtDestination = findViewById(R.id.txtDestination)
        txtDistance = findViewById(R.id.txtDistance)
        txtDuration = findViewById(R.id.txtDuration)
        txtFare = findViewById(R.id.txtFare)

        txtRiderName = findViewById(R.id.txtRiderName)
        txtRiderPhone = findViewById(R.id.txtRiderPhone)
        txtRating = findViewById(R.id.txtRating)
        txtReview = findViewById(R.id.txtReview)
    }

    private fun setupMap() {

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mapContainer, it)
                    .commit()
            }

        mapFragment.getMapAsync(this)
    }

    private fun loadTripData() {

        txtPickup.text = trip.origin
        txtDestination.text = trip.destination

        txtDistance.text = "Distance: ${trip.distanceInKm} km"
        txtDuration.text = "Duration: ${trip.durationInMinutes} mins"
        txtRating.text = "No review yet"

        txtFare.text = "₹%.2f".format(trip.fare)

    }

    private fun loadRiderInfo() {

        FirebaseDatabase.getInstance()
            .getReference("Riders")
            .child(trip.riderId)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {

                    val rider =
                        snapshot.getValue(RiderInfoModel::class.java)

                    rider?.let {

                        if (it.avatar.isNotEmpty()) {

                            Picasso.get()
                                .load(it.avatar)
                                .placeholder(R.drawable.profile)   // Shown while loading
                                .error(R.drawable.profile)
                                .into(imgDriver)

                        }
                        else {
                            imgDriver.setImageResource(R.drawable.profile)
                        }

                        txtRiderName.text =
                            "${it.firstName} ${it.lastName}"

                        txtRiderPhone.text =
                            it.phoneNumber
                    }
                }

            }
            .addOnFailureListener {

                Log.e("RIDER_INFO", it.message ?: "")
            }
    }

    private fun loadReview() {

        FirebaseDatabase.getInstance()
            .getReference("DriverReviews")
            .child(trip.driverId)
            .get()
            .addOnSuccessListener { snapshot ->

                for (reviewSnapshot in snapshot.children) {

                    val review =
                        reviewSnapshot.getValue(DriverReviewModel::class.java)

                    if (review?.tripId == trip.tripId) {

                        txtRating.text =
                            "⭐ ${review.rating}"

                        txtReview.text =
                            review.review

                        break
                    }
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        val origin = LatLng(trip.originLat, trip.originLng)
        val destination = LatLng(trip.destinationLat, trip.destinationLng)

        map.addMarker(
            MarkerOptions().position(origin).title("Pickup")
        )

        map.addMarker(
            MarkerOptions().position(destination).title("Destination")
        )

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 14f))

        drawRoute(origin, destination)
    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {
        // reuse your existing drawPath() or Directions API method
    }

}