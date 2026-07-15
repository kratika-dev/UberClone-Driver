package com.example.uberclone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.uberclone.model.DriverInfoModel
import com.example.uberclone.ui.HomeActivity
import com.example.uberclone.utils.Constants
import com.example.uberclone.utils.UserUtils
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.Arrays
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator

import android.content.pm.PackageManager

import android.os.Build
import android.widget.ImageView
import android.widget.TextView

import android.view.animation.AnimationUtils

import androidx.core.content.ContextCompat


class SplashScreenActivity : AppCompatActivity() {
    companion object {
        private val LOGIN_REQUEST_CODE = 12332
        private const val SPLASH_DELAY = 1500L
    }

    private var isHomeStarted = false
    private lateinit var imgLogo: ImageView
    private lateinit var txtTagline: TextView
    private lateinit var loadingDots: View
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private lateinit var database: FirebaseDatabase

    private lateinit var driverInfoRef: DatabaseReference


    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        imgLogo = findViewById(R.id.imgLogo)
        txtTagline = findViewById(R.id.txtTagline)
        loadingDots = findViewById(R.id.loadingDots)

        init()

        animateLogo {
            animateTagline()
            displaySplashScreen()
        }


        notificationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->

                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }

                // Continue regardless of the user's choice
                checkLocationPermission()
            }



        getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    loadingDots.visibility = View.GONE
                    loadingDots.clearAnimation()


                } else {

                    loadingDots.visibility = View.GONE
                    loadingDots.clearAnimation()

                    val response = IdpResponse.fromResultIntent(result.data)

                    Toast.makeText(

                        this,

                        response?.error?.message ?: "Sign in cancelled",

                        Toast.LENGTH_LONG

                    ).show()

                }

            }


    }

    private fun animateLogo(onAnimationEnd: (() -> Unit)? = null) {
        imgLogo.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            elevation = 0f
        }

        val fadeAnimator = ObjectAnimator.ofFloat(imgLogo, View.ALPHA, 0f, 1f)
        val scaleXAnimator = ObjectAnimator.ofFloat(imgLogo, View.SCALE_X, 0.8f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(imgLogo, View.SCALE_Y, 0.8f, 1f)

        AnimatorSet().apply {

            playTogether(
                fadeAnimator,
                scaleXAnimator,
                scaleYAnimator
            )

            duration = 900
            interpolator = AccelerateDecelerateInterpolator()

            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {

                    imgLogo.animate()
                        .translationZ(8f)
                        .setDuration(300)
                        .start()

                    startLogoFloatingAnimation()

                    onAnimationEnd?.invoke()
                }
            })

            start()
        }
    }

    private fun animateTagline() {
        txtTagline.apply {
            alpha = 0f
            translationY = 20f
        }

        txtTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(150)
            .start()
    }

    private fun startLogoFloatingAnimation() {
        imgLogo.animate()
            .translationY(-8f)
            .setDuration(1200)
            .withEndAction {
                imgLogo.animate()
                    .translationY(0f)
                    .setDuration(1200)
                    .withEndAction {
                        startLogoFloatingAnimation()
                    }
                    .start()
            }
            .start()
    }

    override fun onStart() {
        super.onStart()
        displaySplashScreen()
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener)
        }
        super.onStop()
    }

    private fun displaySplashScreen() {
        Completable.timer(SPLASH_DELAY, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                loadingDots.visibility = View.VISIBLE

                startLoadingAnimation()
                firebaseAuth.addAuthStateListener(listener)
            }
    }

    private fun init() {
        initFirebase()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null) {

                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val token = task.result
                            Log.d("FCM_TOKEN", token)
                            UserUtils.updateToken(this@SplashScreenActivity,token)
                        }else{
                            Toast.makeText(this@SplashScreenActivity, "Failed to get FCM token ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                checkUserFromFirebase()

            } else {
                showLoginLayout()
            }
        }
    }

    private fun initFirebase() {

        database = FirebaseDatabase.getInstance()

        driverInfoRef =
            database.getReference(Constants.DRIVER_INFO_REFERENCE)

        providers = listOf(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        firebaseAuth = FirebaseAuth.getInstance()

    }

    private fun showLoginLayout() {
        loadingDots.visibility = View.VISIBLE
        startLoadingAnimation()

        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.sign_in_layout)
            .setPhoneButtonId(R.id.button_phone_sign_in)
            .setGoogleButtonId(R.id.button_google_sign_in)
            .build()

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.LoginTheme)
            .setAvailableProviders(providers)
            .build()

        getResult.launch(signInIntent)
    }

    private fun checkUserFromFirebase() {
        driverInfoRef
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("TEST", "onDataChange called")
                    loadingDots.visibility = View.GONE
                    loadingDots.clearAnimation()
                    if (snapshot.exists()) {
                        Log.d("TEST", "User exists")
                       val model = snapshot.getValue(DriverInfoModel::class.java)
                        goToHomeActivity(model!!)
                    } else {
                        Log.d("TEST", "User doesn't exist")
                        showRegisterUserLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TEST", error.message)
                    loadingDots.visibility = View.GONE
                    loadingDots.clearAnimation()
                    Toast.makeText(this@SplashScreenActivity, error.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }


    private fun goToHomeActivity(model: DriverInfoModel) {
        loadingDots.visibility = View.GONE
        loadingDots.clearAnimation()
        Constants.currentUser = model
        checkPermissions()
    }

    private fun showRegisterUserLayout() {
        val builder = AlertDialog.Builder(this, R.style.DialogTheme)
        val itemView = LayoutInflater.from(this).inflate(R.layout.register_layout, null)

        val edit_text_name = itemView.findViewById<View>(R.id.edit_first_name) as TextInputEditText
        val edit_text_last_name =
            itemView.findViewById<View>(R.id.edit_last_name) as TextInputEditText
        val edit_text_phone_number =
            itemView.findViewById<View>(R.id.edit_phone) as TextInputEditText

        val button_continue = itemView.findViewById<Button>(R.id.button_register)

        FirebaseAuth.getInstance().currentUser?.phoneNumber?.let {

            edit_text_phone_number.setText(it)

        }

        builder.setView(itemView)
        val dialog = builder.create()
        dialog.show()

        button_continue.setOnClickListener {
            if (edit_text_name.text.toString().trim().isEmpty()) {

                Toast.makeText(this, "Please enter a first name", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            } else if (edit_text_last_name.text.toString().trim().isEmpty()) {

                Toast.makeText(this, "Please enter a last name", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            } else if (edit_text_phone_number.text.toString().trim().isEmpty()) {

                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            } else {
                val model = DriverInfoModel(
                    edit_text_name.text.toString(),
                    edit_text_last_name.text.toString(),
                    edit_text_phone_number.text.toString(),
                    "",
                    0f,
                    0f,
                    0
                )
                driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(model)
                    .addOnFailureListener {

                        Toast.makeText(
                            this@SplashScreenActivity,
                            "${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        loadingDots.visibility = View.GONE
                        loadingDots.clearAnimation()
                    }
                    .addOnSuccessListener {

                        Toast.makeText(
                            this@SplashScreenActivity,
                            "Register Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                        goToHomeActivity(model)
                        loadingDots.visibility = View.GONE
                        loadingDots.clearAnimation()

                    }
            }

        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission()
        } else {
            checkLocationPermission()
        }
    }
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            if (fineLocation) {
                startHome()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun startHome() {
        if (isHomeStarted) {
            return
        }

        isHomeStarted = true
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            when {

                ContextCompat.checkSelfPermission(

                    this,

                    Manifest.permission.POST_NOTIFICATIONS

                ) == PackageManager.PERMISSION_GRANTED -> {

                    // Permission already granted

                }

                else -> {

                    notificationPermissionLauncher.launch(

                        Manifest.permission.POST_NOTIFICATIONS

                    )

                }

            }

        }

    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startHome()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun startLoadingAnimation() {

        val dot1 = loadingDots.findViewById<View>(R.id.dot1)
        val dot2 = loadingDots.findViewById<View>(R.id.dot2)
        val dot3 = loadingDots.findViewById<View>(R.id.dot3)

        val animation1 = AnimationUtils.loadAnimation(
            this,
            R.anim.dot_pulse
        )

        val animation2 = AnimationUtils.loadAnimation(
            this,
            R.anim.dot_pulse
        )

        val animation3 = AnimationUtils.loadAnimation(
            this,
            R.anim.dot_pulse
        )

        animation2.startOffset = 200
        animation3.startOffset = 400

        dot1.startAnimation(animation1)
        dot2.startAnimation(animation2)
        dot3.startAnimation(animation3)
    }
}