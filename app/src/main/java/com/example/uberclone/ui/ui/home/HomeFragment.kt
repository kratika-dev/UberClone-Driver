package com.example.uberclone.ui.ui.home

import android.Manifest
import android.animation.ValueAnimator
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.uberclone.R
import com.example.uberclone.databinding.FragmentHomeBinding
import com.firebase.geofire.GeoFire
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.uberclone.model.DriverRequestReceived
import com.example.uberclone.remote.GoogleAPI
import com.example.uberclone.remote.RetrofitClient
import com.example.uberclone.utils.Constants
import com.example.uberclone.utils.UserUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.SquareCap
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import io.reactivex.rxjava3.disposables.Disposable
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var loadingLayout: RelativeLayout
    private lateinit var txtLoading: TextView
    private var lastCameraUpdateTime = 0L
    private var currentCameraMode = ""

    private var isNavigationMode = false

    private var isFirstLocationReceived = false
    private var isRouteDrawing = false

    //driver to destination
    private var lastRouteUpdateTime = 0L

    private var lastRouteLocation: Location? = null

    private val ROUTE_UPDATE_INTERVAL = 5000L      // 5 seconds

    private val ROUTE_UPDATE_DISTANCE = 20f        // 20 meters


    private var isFirstRouteDraw = true

    private var currentLocation: Location? = null

    //driver side cancellation
    private lateinit var layoutTripControl: MaterialCardView
    private lateinit var btnCancelRide: MaterialButton

    private lateinit var btnStartTrip: MaterialButton

    private lateinit var btnCompleteTrip: MaterialButton

    private var hasArrivedAtPickup = false

    private lateinit var txtTripTitle: TextView

    //online offline

    private var isDriverOnline = false

    private var isDriverBusy = false

    private lateinit var cardDriverStatus: CardView

    private lateinit var txtDriverStatus: TextView

    private lateinit var txtDriverStatusDesc: TextView

    private lateinit var btnToggleOnline: MaterialButton

    private lateinit var chip_accept: Chip
    private lateinit var chip_decline: Chip
    private lateinit var layout_accept: CardView
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var txt_estimate_time: TextView
    private lateinit var txt_estimate_distance: TextView
    private lateinit var root_layout: FrameLayout

    private var driverRequestReceived: DriverRequestReceived? = null

    private var countDownEvent: Disposable? = null

    private var _binding: FragmentHomeBinding? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //Online system
    private lateinit var onlineRef: DatabaseReference
    private lateinit var currentUserRef: DatabaseReference
    private lateinit var driversLocationReference: DatabaseReference
    private lateinit var geoFire: GeoFire

    private val compositeDisposable = CompositeDisposable()
    private lateinit var googleAPI: GoogleAPI

    private var routeAnimator: ValueAnimator? = null

    private var pickupLatLng: LatLng? = null

    private var destinationLatLng: LatLng? = null

    private var hasStartedTrip = false
    private var hasArrivedAtDestination = false

    private var blackPolyline: Polyline? = null

    private var greyPolyline: Polyline? = null

    private var polylineOptions: PolylineOptions? = null

    private var blackPolylineOptions: PolylineOptions? = null

    private var polyLineList: MutableList<LatLng>? = null

    private var driverMarker: Marker? = null

    private var destinationMarker: Marker? = null

    private var markerAnimator: ValueAnimator? = null

    private var pickupMarker: Marker? = null

    private lateinit var driverInfoRef: DatabaseReference

    private var rideRequestRef: DatabaseReference? = null
    private var rideRequestListener: ValueEventListener? = null

    private var currentRequestId: String? = null

    private val onlineValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!snapshot.exists()) return
            if (!::driversLocationReference.isInitialized) return

        }

        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
        }

    }

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initViews(root)

        showLoading(getString(R.string.loading_preparing_map))

        init()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        return root
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onDriverRequestReceived(event: DriverRequestReceived) {

        Log.d("REQUEST_DEBUG", "Start Trip clicked")
        Log.d("REQUEST_DEBUG", "driverRequestReceived = $driverRequestReceived")
        Log.d("REQUEST_DEBUG", "requestId = ${driverRequestReceived?.requestId}")

        driverRequestReceived = event

        Log.d("REQUEST_DEBUG", "Assigned requestId = ${driverRequestReceived?.requestId}")

        hasArrivedAtPickup = false

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation
            .addOnFailureListener { e ->
                Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
            }
            .addOnSuccessListener { location ->

                preparePickupNavigation(location, event.pickupLocation)

                showIncomingRideRequestUI()

                startRideRequestCountdown()

            }
    }


    private fun startRideRequestCountdown() {

        countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                circularProgressBar.progress += 1f
            }
            .takeUntil { it == 100L }

            .doOnComplete {

                if (!isAdded || _binding == null) {
                    return@doOnComplete
                }

                if (driverRequestReceived != null) {

                    context?.let {
                        Toast.makeText(
                            it,
                            "Request Denied",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    UserUtils.sendDeclineRequest(
                        root_layout,
                        activity,
                        driverRequestReceived!!.key,
                        driverRequestReceived!!.requestId
                    )
                    layout_accept.visibility = View.GONE
                    driverRequestReceived = null
                }
            }
            .subscribe(
                {
                    // onNext (unused)
                },
                { error ->
                    Log.e("RXJAVA", "Countdown error", error)
                }
            )
    }

    private fun initViews(view: View?) {
        loadingLayout = view?.findViewById(R.id.loading_layout) as RelativeLayout
        txtLoading = view.findViewById(R.id.txt_loading)
        cardDriverStatus = view?.findViewById(R.id.card_driver_status) as MaterialCardView
        txtDriverStatus = view?.findViewById(R.id.txt_driver_status) as TextView
        txtDriverStatusDesc = view?.findViewById(R.id.txt_driver_status_desc) as TextView
        btnToggleOnline = view?.findViewById(R.id.btn_toggle_online) as MaterialButton

        chip_accept = view?.findViewById(R.id.chip_accept) as Chip
        chip_decline = view?.findViewById(R.id.chip_decline) as Chip
        layout_accept = view.findViewById(R.id.layout_accept) as CardView
        circularProgressBar = view.findViewById(R.id.circular_progress_bar) as CircularProgressBar
        txt_estimate_time = view.findViewById(R.id.txt_estimate_time) as TextView
        txt_estimate_distance = view.findViewById(R.id.txt_estimate_distance) as TextView
        root_layout = view.findViewById(R.id.root_layout) as FrameLayout

        layoutTripControl = view.findViewById(R.id.layout_trip_control)
        btnCancelRide = view.findViewById(R.id.btn_cancel_ride)
        btnStartTrip = view.findViewById(R.id.btn_start_trip)
        btnCompleteTrip = view.findViewById(R.id.btn_complete_trip)
        txtTripTitle = view.findViewById(R.id.txt_trip_title)


        btnStartTrip.visibility = View.GONE
        layoutTripControl.visibility = View.GONE

        circularProgressBar.progress = 100f
        circularProgressBar.progressMax = 100f
        circularProgressBar.progressBarWidth = 8f
        circularProgressBar.backgroundProgressBarWidth = 8f

        btnToggleOnline.setOnClickListener {
            setUpOnlineButton()
        }

        chip_accept.setOnClickListener {
            onAcceptRideClicked()
        }

        chip_decline.setOnClickListener {
            setupDeclineButton()
        }

        btnCancelRide.setOnClickListener {
            setupCancelRideButton()
        }

        btnStartTrip.setOnClickListener {
            onStartTripClicked()
        }
        btnCompleteTrip.setOnClickListener {
            onCompleteTripClicked()
        }


    }

    @SuppressLint("MissingPermission")
    private fun init() {
        googleAPI = RetrofitClient.instance!!.create(GoogleAPI::class.java)
        onlineRef = FirebaseDatabase.getInstance().reference.child(".info/connected")
        locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        ).build()

        driverInfoRef = FirebaseDatabase.getInstance()
            .getReference(Constants.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        restoreDriverStatus()
        restoreBusyStatus()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                val location = locationResult.lastLocation ?: return

                currentLocation = location

                if (!isFirstLocationReceived) {

                    isFirstLocationReceived = true

                    txtLoading.text = "Almost ready..."

                    loadingLayout.postDelayed({

                        hideLoading()

                    }, 300)
                }
                Log.d(
                    "LOCATION_CALLBACK",
                    "Location = ${location.latitude}, ${location.longitude}"
                )

                handleDriverNavigation(location)

                Log.d("LOCATION", "Location callback called")
                Log.d(
                    "LOCATION",
                    "Lat: ${location.latitude}, Lng: ${location.longitude}"
                )

                updateDriverLocationInFirebase(location)
            }
        }

        registerOnlineSystem()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun showLoading(message: String = "Please wait...") {

        if (loadingLayout.visibility == View.VISIBLE) {
            txtLoading.text = message
            return
        }

        txtLoading.text = message

        loadingLayout.alpha = 0f
        loadingLayout.visibility = View.VISIBLE

        loadingLayout.animate()
            .alpha(1f)
            .setDuration(250)
            .start()
    }

    private fun hideLoading() {

        if (loadingLayout.visibility != View.VISIBLE)
            return

        loadingLayout.animate()
            .alpha(0f)
            .setDuration(250)
            .withEndAction {

                loadingLayout.visibility = View.GONE
                loadingLayout.alpha = 1f

            }
            .start()
    }

    private fun setUpOnlineButton() {
        if (isDriverOnline) {
            goOffline()
        } else {
            goOnline()
        }

    }

    private fun onAcceptRideClicked() {
        if (driverRequestReceived != null) {
            val request = driverRequestReceived ?: return
            val driverKey = request.key
            val requestId = request.requestId

            showLoading(getString(R.string.loading_accepting_ride))
            chip_accept.isEnabled = false
            chip_decline.isEnabled = false


            // Driver accepted ride
            isDriverBusy = true

            updateDriverBusyStatus(true)

            updateDriverStatusUI(isDriverOnline)


            currentRequestId = requestId

            resetRouteTracking()

            listenForRideCancellation(currentRequestId!!)


            loadRideDetails(currentRequestId!!) {
                hideLoading()
                chip_accept.isEnabled = true
                chip_decline.isEnabled = true

                chip_accept.visibility = View.GONE
                chip_decline.visibility = View.GONE
                layout_accept.visibility = View.GONE

                layoutTripControl.visibility = View.VISIBLE

                txtTripTitle.text = "Ride Accepted"

                btnStartTrip.visibility = View.GONE
                btnCompleteTrip.visibility = View.GONE
                btnCancelRide.visibility = View.VISIBLE

                countDownEvent?.dispose()
                countDownEvent = null

                circularProgressBar.progress = 0f

                UserUtils.sendAcceptRequest(
                    root_layout,
                    activity,
                    driverKey,
                    requestId
                )

                EventBus.getDefault().removeStickyEvent(DriverRequestReceived::class.java)

              //  driverRequestReceived = null
            }
        }

    }

    private fun setupDeclineButton() {
        if (driverRequestReceived != null) {

            countDownEvent?.dispose()
            countDownEvent = null

            chip_decline.visibility = View.GONE
            layout_accept.visibility = View.GONE
            mMap.clear()
            circularProgressBar.progress = 0f

            Log.d("DECLINE_TEST", "requestId = ${driverRequestReceived?.key}")

            UserUtils.sendDeclineRequest(
                root_layout,
                activity,
                driverRequestReceived!!.key,
                driverRequestReceived!!.requestId
            )
            EventBus.getDefault().removeStickyEvent(DriverRequestReceived::class.java)

            driverRequestReceived = null
        }
    }

    private fun setupCancelRideButton() {
        if (currentRequestId == null) {

            Toast.makeText(
                requireContext(),
                "No active ride",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        showLoading(getString(R.string.loading_cancelling_ride))

        btnCancelRide.isEnabled = false

        FirebaseDatabase.getInstance()
            .getReference(Constants.RIDE_REQUEST_REFERENCE)
            .child(currentRequestId!!)
            .child("status")
            .setValue("CANCELLED_BY_DRIVER")
            .addOnSuccessListener {

                UserUtils.sendRideCancellationRequest(
                    requireContext(),
                    currentRequestId!!,
                    "DRIVER"
                )

                hideLoading()

                countDownEvent?.dispose()
                countDownEvent = null

                layoutTripControl.visibility = View.GONE

                driverRequestReceived = null

                layout_accept.visibility = View.GONE

                circularProgressBar.progress = 0f

                mMap.clear()


                // Driver is free again
                isDriverBusy = false
                updateDriverBusyStatus(false)

                hasArrivedAtPickup = false

                // Refresh online/offline UI
                updateDriverOnlineStatus(isDriverOnline)

                updateDriverStatusUI(isDriverOnline)

                // Clear active request ID
                currentRequestId = null


                Toast.makeText(requireContext(), "Ride cancelled", Toast.LENGTH_SHORT).show()

                resetRouteTracking()

                btnCancelRide.isEnabled = true

            }
            .addOnFailureListener {
                hideLoading()
                btnCancelRide.isEnabled = true
            }
    }

    private fun onStartTripClicked() {

        if (driverRequestReceived == null) {
            Log.e("START_TRIP", "driverRequestReceived is NULL")
            return
        }
        FirebaseDatabase.getInstance()
            .getReference(Constants.RIDE_REQUEST_REFERENCE)
            .child(driverRequestReceived!!.requestId)
            .child("status")
            .setValue("TRIP_STARTED")
            .addOnSuccessListener {
                Log.d("TRIP_STATUS", "Trip started status updated")

                UserUtils.sendTripStartedRequest(
                    root_layout,
                    activity,
                    driverRequestReceived!!.key,
                    driverRequestReceived!!.requestId
                )
            }
            .addOnFailureListener {
                Log.e("TRIP_STATUS", it.message ?: "Error")
            }

        showLoading(getString(R.string.loading_starting_trip))

        btnStartTrip.isEnabled = false

        hasStartedTrip = true
        hasArrivedAtDestination = false

        btnStartTrip.visibility = View.GONE

        txtTripTitle.text = "Trip in Progress"


        btnCancelRide.visibility = View.GONE
        btnCompleteTrip.visibility = View.GONE   // keep hidden until arrival
        layoutTripControl.visibility = View.GONE   // keep hidden until arrival

        pickupMarker?.remove()

        destinationLatLng?.let {

            destinationMarker?.remove()

            destinationMarker = mMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Destination")
            )

            currentLocation?.let { location ->

                drawRoute(
                    LatLng(location.latitude, location.longitude),
                    it
                )

            }
        }

        hideLoading()

        btnStartTrip.isEnabled = true

    }

    private fun onCompleteTripClicked() {
        if (currentRequestId == null) return
        showLoading(getString(R.string.loading_completing_trip))

        btnCompleteTrip.isEnabled = false

        FirebaseDatabase.getInstance()
            .getReference(Constants.RIDE_REQUEST_REFERENCE)
            .child(currentRequestId!!)
            .child("status")
            .setValue("COMPLETED")
            .addOnSuccessListener {
                hideLoading()

                // Send Trip Completed notification to Rider
                driverRequestReceived?.let { request ->
                    UserUtils.sendTripCompletedRequest(
                        requireView(),
                        requireActivity(),
                        request.key,
                        request.requestId
                    )
                }


                // Driver becomes available again
                isDriverBusy = false
                updateDriverBusyStatus(false)
                updateDriverStatusUI(isDriverOnline)

                // Reset trip flags
                hasStartedTrip = false
                hasArrivedAtPickup = false
                hasArrivedAtDestination = false

                // Stop listening for cancellation
                rideRequestListener?.let {
                    rideRequestRef?.removeEventListener(it)
                }
                rideRequestListener = null
                rideRequestRef = null

                // Clear request data
                currentRequestId = null
                driverRequestReceived = null

                pickupLatLng = null
                destinationLatLng = null

                // Hide controls
                btnStartTrip.visibility = View.GONE
                btnCompleteTrip.visibility = View.GONE
                btnCancelRide.visibility = View.GONE
                layoutTripControl.visibility = View.GONE

                // Clear map
                mMap.clear()

                pickupMarker = null
                destinationMarker = null
                driverMarker = null

                greyPolyline = null
                blackPolyline = null

                Toast.makeText(requireContext(), "Trip Completed", Toast.LENGTH_SHORT).show()

                resetRouteTracking()

                btnCompleteTrip.isEnabled = true

            }.addOnFailureListener {
                hideLoading()
                btnCompleteTrip.isEnabled = true
            }
    }

    private fun registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener)
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        //   geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)

        compositeDisposable.clear()
        if (EventBus.getDefault().hasSubscriberForEvent(HomeFragment::class.java)) {
            EventBus.getDefault().removeStickyEvent(HomeFragment::class.java)
            EventBus.getDefault().unregister(this)
        }
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        txtLoading.text = "Getting your location..."

        mMap.setPadding(
            0,
            350,
            0,
            550
        )

        //request permission

        Dexter.withContext(context)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
                override fun onPermissionGranted(permissions: PermissionGrantedResponse?) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    mMap.setOnMyLocationButtonClickListener {

                        fusedLocationProviderClient.lastLocation
                            .addOnFailureListener { it: Exception ->
                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                            }
                            .addOnSuccessListener { location ->

                                val userLatLng =
                                    LatLng(location.latitude, location.longitude)

                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLatLng,
                                        10f
                                    )
                                )
                            }
                        true
                    }

                    val view =
                        mapFragment.view?.findViewById<View>("1".toInt())?.parent as View

                    val locationButton =
                        view.findViewById<View>("2".toInt())

                    val params =
                        locationButton.layoutParams as RelativeLayout.LayoutParams

                    params.addRule(RelativeLayout.ALIGN_TOP, 0)

                    params.addRule(
                        RelativeLayout.ALIGN_PARENT_BOTTOM,
                        RelativeLayout.TRUE
                    )

                    params.bottomMargin = 50
                }


                override fun onPermissionDenied(permissions: PermissionDeniedResponse?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            })


        mMap.uiSettings.isZoomControlsEnabled = true

        try {
            val success = googleMap.setMapStyle(context?.let {
                MapStyleOptions.loadRawResourceStyle(
                    it,
                    R.raw.user_maps_style
                )
            })
            if (!success) {
                Log.d("Google Map", "error")
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }


    }

    private fun handleDriverNavigation(location: Location) {

        val newPos = LatLng(
            location.latitude,
            location.longitude
        )

        Log.d("DRIVER_MARKER", "isDriverBusy = $isDriverBusy")

        if (isDriverBusy) {

            updateDriverMarker(newPos)

            if (!hasStartedTrip) {

                checkDriverArrival(newPos)

            } else {

                updateLiveRoute()

                checkDestinationArrival(newPos)

            }

        } else {

            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(newPos, 10f)
            )

        }
    }

    private fun updateDriverLocationInFirebase(location: Location) {

        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addressList: List<Address>?
        try {
            addressList = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )

            Log.d("STEP1", "Location callback called")


            val cityName = addressList?.get(0)!!.locality

            Log.d("STEP2", "City = $cityName")

            driversLocationReference =
                FirebaseDatabase.getInstance()
                    .getReference(Constants.DRIVER_LOCATION_REFERENCE).child(cityName)


            Log.d(
                "STEP3",
                "Path = ${Constants.DRIVER_LOCATION_REFERENCE}/$cityName"
            )



            currentUserRef =
                driversLocationReference.child(
                    FirebaseAuth.getInstance().currentUser!!.uid
                )

            geoFire = GeoFire(driversLocationReference)


            if (isDriverOnline) {

                geoFire.setLocation(
                    FirebaseAuth.getInstance().currentUser!!.uid,
                    GeoLocation(
                        location.latitude,
                        location.longitude
                    )
                ) { key: String?, error: DatabaseError? ->

                    if (error != null) {

                        Log.e("GEOFIRE", error.message)

                    } else {

                        Log.d("GEOFIRE", "Location Updated")

                    }

                }
            }


        } catch (e: IOException) {
            Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
        }


    }

    private fun createDriverMarker(position: LatLng) {

        Log.d("DRIVER_MARKER", "Creating driver marker")

        driverMarker = mMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("You")
                .icon(getCarBitmap())
                .anchor(0.5f, 0.5f)
                .flat(true)
        )
    }

    private fun animateDriverMarker(startPosition: LatLng, endPosition: LatLng) {

        val marker = driverMarker ?: return

        val bearing = getBearing(startPosition, endPosition)
        marker.rotation = bearing
        markerAnimator?.cancel()

        markerAnimator = ValueAnimator.ofFloat(0f, 1f)

        markerAnimator?.duration = 1000

        markerAnimator?.addUpdateListener { animation ->

            val fraction = animation.animatedValue as Float

            val lat = startPosition.latitude +
                    (endPosition.latitude - startPosition.latitude) * fraction

            val lng = startPosition.longitude +
                    (endPosition.longitude - startPosition.longitude) * fraction


            val animatedPosition = LatLng(lat, lng)

            marker.position = animatedPosition

            updateNavigationCamera(
                animatedPosition,
                bearing
            )

        }

        markerAnimator?.start()


    }

    private fun updateDriverMarker(newPosition: LatLng) {
        if (driverMarker == null) {
            createDriverMarker(newPosition)
            return
        }
        val startPosition = driverMarker!!.position

        if (startPosition == newPosition) {
            return
        }

        animateDriverMarker(startPosition, newPosition)
    }

    private fun getBearing(start: LatLng, end: LatLng): Float {

        val location1 = Location("start")
        location1.latitude = start.latitude
        location1.longitude = start.longitude

        val location2 = Location("end")
        location2.latitude = end.latitude
        location2.longitude = end.longitude

        return location1.bearingTo(location2)
    }

    private fun getPointAhead(
        position: LatLng,
        bearing: Float,
        distanceMeters: Double
    ): LatLng {

        val radius = 6378137.0

        val bearingRad = Math.toRadians(bearing.toDouble())

        val lat1 = Math.toRadians(position.latitude)
        val lng1 = Math.toRadians(position.longitude)

        val lat2 = asin(
            sin(lat1) * cos(distanceMeters / radius) +
                    cos(lat1) * sin(distanceMeters / radius) * cos(bearingRad)
        )

        val lng2 = lng1 + atan2(
            sin(bearingRad) * sin(distanceMeters / radius) * cos(lat1),
            cos(distanceMeters / radius) - sin(lat1) * sin(lat2)
        )

        return LatLng(
            Math.toDegrees(lat2),
            Math.toDegrees(lng2)
        )
    }

    private fun getCarBitmap(): BitmapDescriptor {

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.img_car)

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            130,
            130,
            false
        )

        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    private fun updateNavigationCamera(position: LatLng, bearing: Float) {

        val pickup = pickupLatLng ?: return


        Log.d(
            "CAMERA_DEBUG",
            "updateNavigationCamera called"
        )


        val distanceToPickup = getDistanceBetween(
            position,
            pickup
        )


        Log.d(
            "CAMERA_DEBUG",
            "Distance to pickup = $distanceToPickup"
        )


        // Avoid camera updates too frequently
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastCameraUpdateTime < 1000) {
            return
        }

        lastCameraUpdateTime = currentTime



        when {

            distanceToPickup < 50 -> {

                if (currentCameraMode != "ARRIVAL") {
                    currentCameraMode = "ARRIVAL"
                }

                moveToNavigationCamera(position, bearing)
            }

            distanceToPickup < 500 -> {

                if (currentCameraMode != "NAVIGATION") {
                    currentCameraMode = "NAVIGATION"
                }

                moveToNavigationCamera(position, bearing)
            }

            else -> {

                if (currentCameraMode != "OVERVIEW") {
                    currentCameraMode = "OVERVIEW"
                }

                moveToOverviewCamera(position)
            }
        }

    }

    private fun moveToNavigationCamera(position: LatLng, bearing: Float) {


        val cameraTarget = getPointAhead(
            position,
            bearing,
            120.0
        )


        val cameraPosition = CameraPosition.Builder()
            .target(cameraTarget)
            .zoom(18f)
            .bearing(bearing)
            .tilt(65f)
            .build()


        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            1000,
            null
        )
    }

    private fun moveToOverviewCamera(position: LatLng) {

        val cameraPosition = CameraPosition.Builder()
            .target(position)
            .zoom(15f)
            .tilt(0f)
            .bearing(0f)
            .build()


        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            1000,
            null
        )
    }

    private fun updateLiveRoute() {

        Log.d(
            "LIVE_ROUTE",
            "Updating route from ${currentLocation!!.latitude},${currentLocation!!.longitude} to ${destinationLatLng}"
        )


        if (!hasStartedTrip) return

        if (destinationLatLng == null) return

        if (currentLocation == null) return

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastRouteUpdateTime < ROUTE_UPDATE_INTERVAL)
            return

        if (lastRouteLocation != null) {

            val distance =
                currentLocation!!.distanceTo(lastRouteLocation!!)


            Log.d(
                "LIVE_ROUTE",
                "Distance since last update = $distance"
            )


            Log.d(
                "LIVE_ROUTE",
                "LastRouteLocation = ${lastRouteLocation?.latitude}, ${lastRouteLocation?.longitude}"
            )

            Log.d(
                "LIVE_ROUTE",
                "CurrentLocation = ${currentLocation?.latitude}, ${currentLocation?.longitude}"
            )


            if (distance < ROUTE_UPDATE_DISTANCE)
                return
        }

        lastRouteUpdateTime = currentTime
        lastRouteLocation = Location(currentLocation!!)

        Log.d("LIVE_ROUTE", "Refreshing route...")

        drawRoute(
            LatLng(
                currentLocation!!.latitude,
                currentLocation!!.longitude
            ),
            destinationLatLng!!
        )

    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {

        if (isRouteDrawing) {
            Log.d("ROUTE_DEBUG", "Skipping route request. Previous request still running.")
            return
        }

        isRouteDrawing = true

        Log.d(
            "ROUTE_CALL",
            "Origin=$origin Destination=$destination"
        )


        compositeDisposable.add(
            googleAPI.getDirections(
                "driving",
                "less_driving",
                "${origin.latitude},${origin.longitude}",
                "${destination.latitude},${destination.longitude}",
                getString(R.string.api_key)
            )!!
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(

                    { returnResult ->

                        try {

                            polyLineList = parsePolyline(returnResult)

                            drawPolylines()

                            updateRouteCamera(origin, destination)

                            isRouteDrawing = false



                            Log.d(
                                "ROUTE_DEBUG",
                                "Route drawn: $origin -> $destination"
                            )

                        } catch (e: Exception) {

                            isRouteDrawing = false


                            Toast.makeText(
                                requireContext(),
                                "Unable to calculate the route.",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.e(
                                "ROUTE_DEBUG",
                                e.message ?: ""
                            )
                        }

                    },

                    { throwable ->

                        isRouteDrawing = false


                        Toast.makeText(
                            requireContext(),
                            "Unable to connect. Please check your internet.",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.e(
                            "ROUTE_DEBUG",
                            "Directions API failed",
                            throwable
                        )
                    }

                )
        )
    }

    private fun parsePolyline(result: String): MutableList<LatLng> {

        val jsonObject = JSONObject(result)
        val jsonArray = jsonObject.getJSONArray("routes")

        val route = jsonArray.getJSONObject(0)

        val polyline = route
            .getJSONObject("overview_polyline")
            .getString("points")

        return Constants.decodePoly(polyline)
    }

    private fun drawPolylines() {

        greyPolyline?.remove()
        blackPolyline?.remove()

        polylineOptions = PolylineOptions()
            .color(Color.GRAY)
            .width(12f)
            .startCap(SquareCap())
            .jointType(JointType.ROUND)
            .addAll(polyLineList!!)

        greyPolyline = mMap.addPolyline(polylineOptions!!)

        blackPolylineOptions = PolylineOptions()
            .color(Color.BLACK)
            .width(12f)
            .startCap(SquareCap())
            .jointType(JointType.ROUND)
            .addAll(polyLineList!!)

        blackPolyline = mMap.addPolyline(blackPolylineOptions!!)
    }

    private fun updateRouteCamera(origin: LatLng, destination: LatLng) {

        val bounds = LatLngBounds.Builder()
            .include(origin)
            .include(destination)
            .build()

        if (isFirstRouteDraw) {

            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 250)
            )

            isFirstRouteDraw = false

        } else {

            val cameraPosition = CameraPosition.Builder()
                .target(origin)
                .zoom(mMap.cameraPosition.zoom)
                .bearing(currentLocation?.bearing ?: 0f)
                .tilt(45f)
                .build()

            mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition)
            )
        }
    }

    private fun preparePickupNavigation(currentLocation: Location, pickupLocation: String) {
        val origin = LatLng(
            currentLocation.latitude,
            currentLocation.longitude
        )

        val destination = LatLng(
            pickupLocation.split(",")[0].toDouble(),
            pickupLocation.split(",")[1].toDouble()
        )

        pickupLatLng = destination

        Log.d("NAV_DEBUG", "Origin = $origin")

        Log.d("NAV_DEBUG", "Destination = $destination")

        drawRoute(origin, destination)

        pickupMarker?.remove()

        pickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(destination)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .title("Pickup Location")
        )
    }

    private fun showIncomingRideRequestUI() {
        countDownEvent?.dispose()
        countDownEvent = null

        circularProgressBar.progress = 0f

        chip_accept.visibility = View.VISIBLE
        chip_decline.visibility = View.VISIBLE

        layoutTripControl.visibility = View.GONE
        layout_accept.visibility = View.VISIBLE
    }

    private fun isWithinDistance(
        current: LatLng,
        target: LatLng,
        thresholdMeters: Float
    ): Boolean {

        val result = FloatArray(1)

        Location.distanceBetween(
            current.latitude,
            current.longitude,
            target.latitude,
            target.longitude,
            result
        )

        return result[0] <= thresholdMeters
    }

    private fun getDistanceBetween(current: LatLng, target: LatLng): Float {

        val result = FloatArray(1)

        Location.distanceBetween(
            current.latitude,
            current.longitude,
            target.latitude,
            target.longitude,
            result
        )

        return result[0]
    }

    private fun checkDriverArrival(driverLocation: LatLng) {

        val pickup = pickupLatLng ?: return

        val result = FloatArray(1)

        Location.distanceBetween(
            driverLocation.latitude,
            driverLocation.longitude,
            pickup.latitude,
            pickup.longitude,
            result
        )

        val distance = result[0]

        Log.d(
            "ARRIVAL_DEBUG",
            "Distance = $distance meters"
        )


        if (!hasArrivedAtPickup &&
            isWithinDistance(driverLocation, pickup, 15000f)
        ) {
            hasArrivedAtPickup = true

            pickupMarker?.remove()
            pickupMarker = null

            btnStartTrip.visibility = View.VISIBLE

            Toast.makeText(
                requireContext(),
                "Arrived at pickup",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun checkDestinationArrival(driverLocation: LatLng) {

        val destination = destinationLatLng ?: return

        val result = FloatArray(1)

        Location.distanceBetween(
            driverLocation.latitude,
            driverLocation.longitude,
            destination.latitude,
            destination.longitude,
            result
        )

        val distance = result[0]

        Log.d("DESTINATION_DEBUG", "Distance = $distance meters")

        if (
            isWithinDistance(
                driverLocation,
                destination,
                15000f
            ) && !hasArrivedAtDestination
        ) {

            hasArrivedAtDestination = true

            Log.d("DESTINATION_DEBUG", "Driver reached destination")

            btnCancelRide.visibility = View.GONE
            layoutTripControl.visibility = View.VISIBLE
            btnCompleteTrip.visibility = View.VISIBLE

            txtTripTitle.text = "Destination Reached"


            Toast.makeText(
                requireContext(),
                "Destination reached",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateDriverStatusUI(isOnline: Boolean) {

        if (!isOnline) {

            txtDriverStatus.text = "🔴 Offline"
            txtDriverStatusDesc.text = "You are not receiving ride requests"
            btnToggleOnline.text = "Go Online"

            btnToggleOnline.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )

        } else if (isDriverBusy && !hasStartedTrip) {

            txtDriverStatus.text = "📍 Heading to Pickup"
            txtDriverStatusDesc.text = "Navigate to the rider's pickup location"
            btnToggleOnline.text = "Go Offline"

            btnToggleOnline.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )

        } else if (isDriverBusy && hasStartedTrip) {

            txtDriverStatus.text = "🚕 On Trip"
            txtDriverStatusDesc.text = "Navigate to the destination"
            btnToggleOnline.text = "Go Offline"

            btnToggleOnline.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )

        } else {

            txtDriverStatus.text = "🟢 Online"
            txtDriverStatusDesc.text = "You are receiving ride requests"
            btnToggleOnline.text = "Go Offline"

            btnToggleOnline.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            )
        }
    }

    private fun goOnline() {
        showLoading(getString(R.string.loading_connecting))

        updateDriverOnlineStatus(
            true,
            onSuccess = {

                isDriverOnline = true

                updateDriverStatusUI(true)

                hideLoading()

                Toast.makeText(
                    requireContext(),
                    "You are now online",
                    Toast.LENGTH_SHORT
                ).show()

            },
            onFailure = { exception ->

                hideLoading()

                Toast.makeText(
                    requireContext(),
                    exception.message ?: "Failed to go online",
                    Toast.LENGTH_SHORT
                ).show()

            }
        )
    }

    private fun goOffline() {

        showLoading(getString(R.string.loading_offline))

        updateDriverOnlineStatus(
            false,
            onSuccess = {

                if (!::geoFire.isInitialized) {

                    hideLoading()

                    Toast.makeText(
                        requireContext(),
                        "GeoFire not initialized",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@updateDriverOnlineStatus
                }

                geoFire.removeLocation(
                    FirebaseAuth.getInstance().currentUser!!.uid
                ) { _, error ->

                    hideLoading()

                    if (error != null) {

                        Toast.makeText(
                            requireContext(),
                            "Couldn't go offline. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.e("GEOFIRE", error.message ?: "Unknown error")

                        return@removeLocation
                    }

                    isDriverOnline = false
                    isDriverBusy = false

                    updateDriverBusyStatus(false)
                    updateDriverStatusUI(false)

                    Toast.makeText(
                        requireContext(),
                        "You are now OFFLINE",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onFailure = { exception ->

                hideLoading()

                Toast.makeText(
                    requireContext(),
                    "Couldn't go offline. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

                Log.e("ONLINE_STATUS", "Failed to go offline", exception)
            }
        )
    }

    private fun updateDriverOnlineStatus(
        online: Boolean,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {

        driverInfoRef.child("isOnline")
            .setValue(online)
            .addOnSuccessListener {
                onSuccess?.invoke()
            }
            .addOnFailureListener {
                onFailure?.invoke(it)
            }

    }

    private fun updateDriverBusyStatus(busy: Boolean) {

        driverInfoRef.child("isBusy")
            .setValue(busy)
            .addOnSuccessListener {

                Log.d("BUSY_STATUS", "Saved = $busy")

            }
            .addOnFailureListener {

                Log.e("BUSY_STATUS", it.message ?: "")

            }

    }

    private fun restoreDriverStatus() {

        driverInfoRef.child("isOnline")
            .get()
            .addOnSuccessListener { snapshot ->

                Log.d("ONLINE_DEBUG", "Snapshot exists = ${snapshot.exists()}")
                Log.d("ONLINE_DEBUG", "Snapshot value = ${snapshot.value}")

                isDriverOnline =
                    snapshot.getValue(Boolean::class.java) ?: false

                Log.d("ONLINE_DEBUG", "Restored = $isDriverOnline")

                updateDriverStatusUI(isDriverOnline)


            }
            .addOnFailureListener {

                Log.e("ONLINE_DEBUG", "Restore failed", it)
            }
    }

    private fun restoreBusyStatus() {

        driverInfoRef.child("isBusy")
            .get()
            .addOnSuccessListener { snapshot ->

                isDriverBusy =
                    snapshot.getValue(Boolean::class.java) ?: false


                Log.d(
                    "BUSY_DEBUG",
                    "Restored busy = $isDriverBusy"
                )

            }
            .addOnFailureListener {

                Log.e(
                    "BUSY_DEBUG",
                    "Restore failed",
                    it
                )

            }

    }

    private fun listenForRideCancellation(requestId: String) {

        rideRequestRef = FirebaseDatabase.getInstance()
            .getReference(Constants.RIDE_REQUEST_REFERENCE)
            .child(requestId)

        rideRequestListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (!snapshot.exists()) return

                val status = snapshot.child("status")
                    .getValue(String::class.java)

                Log.d("RIDE_CANCEL", "RequestId=$requestId Status=$status")

                when (status) {

                    "CANCELLED" -> {

                        Log.d("RIDE_CANCEL", "Ride cancelled")

                        onRideCancelledByRider()
                    }

                    "TRIP_STARTED" -> {

                        Log.d("TRIP_STATUS", "Trip started confirmed")

                        hasStartedTrip = true
                        hasArrivedAtDestination = false

                        updateDriverStatusUI(isDriverOnline)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RIDE_CANCEL", error.message)
            }
        }

        rideRequestRef!!.addValueEventListener(rideRequestListener!!)
    }

    private fun loadRideDetails(requestId: String, onSuccess: () -> Unit) {

        Log.d("TRIP_DEBUG", "loadRideDetails() requestId = $requestId")

        FirebaseDatabase.getInstance()
            .getReference(Constants.RIDE_REQUEST_REFERENCE)
            .child(requestId)
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.exists()) return@addOnSuccessListener

                hideLoading()
                chip_accept.isEnabled = true
                chip_decline.isEnabled = true


                val destinationLat =
                    snapshot.child("destinationLat")
                        .getValue(Double::class.java) ?: return@addOnSuccessListener

                val destinationLng =
                    snapshot.child("destinationLng")
                        .getValue(Double::class.java) ?: return@addOnSuccessListener


                if (destinationLat == null || destinationLng == null) {

                    hideLoading()

                    chip_accept.isEnabled = true
                    chip_decline.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "Invalid destination",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                destinationLatLng = LatLng(
                    destinationLat,
                    destinationLng
                )
                onSuccess()
            }.addOnFailureListener {

                hideLoading()
                chip_accept.isEnabled = true
                chip_decline.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Unable to accept ride",
                    Toast.LENGTH_SHORT
                ).show()

                Log.e(
                    "TRIP_DEBUG",
                    it.message ?: ""
                )
            }


    }

    private fun onRideCancelledByRider() {

        Log.d("RIDE_CANCEL", "onRideCancelledByRider() called")

        // Driver is available again
        isDriverBusy = false
        updateDriverBusyStatus(false)

        hasArrivedAtPickup = false

        updateDriverStatusUI(isDriverOnline)

        // Stop listening for this ride request
        rideRequestListener?.let {
            rideRequestRef?.removeEventListener(it)
        }

        rideRequestListener = null
        rideRequestRef = null

        EventBus.getDefault().removeStickyEvent(DriverRequestReceived::class.java)

        // Clear current request data
        driverRequestReceived = null
        currentRequestId = null

        // Reset request UI (safe even if already hidden)

        layout_accept.visibility = View.GONE

        layoutTripControl.visibility = View.GONE

        circularProgressBar.progress = 0f

        // Notify driver
        Toast.makeText(
            requireContext(),
            "Ride cancelled by rider",
            Toast.LENGTH_SHORT
        ).show()

        resetRouteTracking()

        Log.d("RIDE_CANCEL", "Driver is ready for new ride requests")
    }

    private fun resetRouteTracking() {

        markerAnimator?.cancel()
        markerAnimator = null

        isFirstRouteDraw = true

        lastRouteLocation = null

        lastRouteUpdateTime = 0L

    }

}
