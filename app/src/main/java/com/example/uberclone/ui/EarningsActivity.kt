package com.example.uberclone.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uberclone.R
import com.google.android.material.appbar.MaterialToolbar
import com.example.uberclone.adapter.TripEarningsAdapter
import com.example.uberclone.model.EarningModel
import com.example.uberclone.model.TripModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class EarningsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    private lateinit var txtTodayEarnings: TextView
    private lateinit var mainLayout: LinearLayout
    private lateinit var txtTodayTrips: TextView
    private lateinit var txtTotalTrips: TextView
    private lateinit var txtEmpty: TextView

    private lateinit var recyclerEarnings: RecyclerView

    private lateinit var tripRef: DatabaseReference

    private lateinit var adapter: TripEarningsAdapter

    private val earningList = ArrayList<EarningModel>()

    private lateinit var txtWeekEarnings: TextView
    private lateinit var txtMonthEarnings: TextView
    private lateinit var txtTotalEarnings: TextView

    private lateinit var shimmerLayout: ShimmerFrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_earnings)

        toolbar = findViewById(R.id.toolbar)
        shimmerLayout = findViewById(R.id.shimmerLayout)
        mainLayout = findViewById(R.id.mainLayout)
        txtTodayEarnings = findViewById(R.id.txtTodayEarnings)
        txtTodayTrips = findViewById(R.id.txtTodayTrips)
        txtTotalTrips = findViewById(R.id.txtTotalTrips)
        txtEmpty = findViewById(R.id.txtEmpty)

        txtWeekEarnings = findViewById(R.id.txtWeekEarnings)
        txtMonthEarnings = findViewById(R.id.txtMonthEarnings)
        txtTotalEarnings = findViewById(R.id.txtTotalEarnings)

        recyclerEarnings = findViewById(R.id.recyclerEarnings)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        recyclerEarnings.layoutManager = LinearLayoutManager(this)
        adapter = TripEarningsAdapter(earningList)
        recyclerEarnings.adapter = adapter

        loadEarnings()
    }

    private fun loadEarnings() {
        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        mainLayout.visibility = View.GONE
        txtEmpty.visibility = View.GONE

        val driverId = FirebaseAuth.getInstance().currentUser!!.uid

        tripRef = FirebaseDatabase.getInstance()
            .getReference("Trips")

        tripRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                earningList.clear()

                var todayTrips = 0
                var totalTrips = 0
                var todayEarnings = 0.0

                var weekEarnings = 0.0

                var monthEarnings = 0.0

                var totalEarnings = 0.0

                val calendarToday = Calendar.getInstance()

                for (tripSnapshot in snapshot.children) {

                    val trip =
                        tripSnapshot.getValue(TripModel::class.java)

                    if (trip == null)
                        continue

                    if (trip.driverId != driverId)
                        continue

                    if (trip.status != "COMPLETED")
                        continue

                    totalEarnings += trip.fare

                    totalTrips++

                    // Add to RecyclerView
                    earningList.add(
                        EarningModel(
                            trip.origin,
                            trip.destination,
                            trip.fare,
                            trip.timestamp
                        )
                    )

                    val tripCalendar = Calendar.getInstance()
                    tripCalendar.timeInMillis = trip.timestamp

                    val currentWeek = calendarToday.get(Calendar.WEEK_OF_YEAR)

                    val currentMonth = calendarToday.get(Calendar.MONTH)

                    val currentYear = calendarToday.get(Calendar.YEAR)

                    if (tripCalendar.get(Calendar.YEAR) == currentYear &&
                        tripCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek) {

                        weekEarnings += trip.fare
                    }

                    if (tripCalendar.get(Calendar.YEAR) == currentYear &&
                        tripCalendar.get(Calendar.MONTH) == currentMonth) {

                        monthEarnings += trip.fare
                    }

                    val sameDay =
                        calendarToday.get(Calendar.YEAR) ==
                                tripCalendar.get(Calendar.YEAR)
                                &&
                                calendarToday.get(Calendar.DAY_OF_YEAR) ==
                                tripCalendar.get(Calendar.DAY_OF_YEAR)

                    if (sameDay) {

                        todayTrips++

                        todayEarnings += trip.fare
                    }

                }

                earningList.sortByDescending {
                    it.timestamp
                }

                txtTodayTrips.text =
                    todayTrips.toString()

                txtTotalTrips.text =
                    totalTrips.toString()

                txtTodayEarnings.text =
                    "₹%.1f".format(todayEarnings)

                txtWeekEarnings.text =
                    "₹%.1f".format(weekEarnings)

                txtMonthEarnings.text =
                    "₹%.1f".format(monthEarnings)

                txtTotalEarnings.text =
                    "₹%.1f".format(totalEarnings)

                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE

                if (earningList.isEmpty()) {
                    txtEmpty.visibility = View.VISIBLE
                    mainLayout.visibility = View.GONE
                } else {
                    txtEmpty.visibility = View.GONE
                    mainLayout.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                shimmerLayout.stopShimmer()
                shimmerLayout.visibility = View.GONE
                txtEmpty.visibility = View.VISIBLE
                mainLayout.visibility = View.GONE
            }

        })

    }
}