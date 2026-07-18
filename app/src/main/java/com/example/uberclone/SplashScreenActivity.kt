package com.example.uberclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uberclone.model.DriverInfoModel
import com.example.uberclone.ui.HomeActivity
import com.example.uberclone.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.AnimationUtils
import com.example.uberclone.ui.LoginActivity
import com.example.uberclone.ui.RegisterActivity
import com.example.uberclone.utils.NetworkMonitor
import com.example.uberclone.utils.SnackbarUtils
import com.example.uberclone.utils.UserUtils
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.rxjava3.disposables.Disposable

class SplashScreenActivity : AppCompatActivity() {
    companion object {
        private const val SPLASH_DELAY = 3000L
    }

    private var splashDisposable: Disposable? = null
    private lateinit var networkMonitor: NetworkMonitor

    private var authListenerAdded = false

    private lateinit var rootView: View
    private lateinit var imgLogo: ImageView
    private lateinit var txtTagline: TextView
    private lateinit var loadingDots: View
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    private lateinit var database: FirebaseDatabase

    private lateinit var driverInfoRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        rootView = findViewById(android.R.id.content)
        imgLogo = findViewById(R.id.imgLogo)
        txtTagline = findViewById(R.id.txtTagline)
        loadingDots = findViewById(R.id.loadingDots)


        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Constants.DRIVER_INFO_REFERENCE)

        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser

            if (user != null) {

                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        UserUtils.updateToken(this@SplashScreenActivity, token)
                    }

                checkUserFromFirebase()

            } else {

                showLoginLayout()

            }
        }


        networkMonitor = NetworkMonitor(this, object : NetworkMonitor.NetworkListener {

            override fun onNetworkAvailable() {
                SnackbarUtils.hideNoInternet()

                if (!isFinishing && !isDestroyed) {
                    addFirebaseAuthListener()
                }
            }

            override fun onNetworkLost() {
                SnackbarUtils.showNoInternet(window.decorView.rootView)
            }
        })



        imgLogo.visibility = View.VISIBLE
        txtTagline.visibility = View.VISIBLE
        displaySplashScreen()

    }

    override fun onStart() {
        super.onStart()
    }


    private fun displaySplashScreen() {
        splashDisposable = Completable
            .timer(SPLASH_DELAY, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                loadingDots.visibility = View.VISIBLE
                startLoadingAnimation()
                networkMonitor.register()
                if (networkMonitor.isConnected()) {
                    addFirebaseAuthListener()
                } else {
                    SnackbarUtils.showNoInternet(window.decorView.rootView)
                }
            }
    }


    private fun addFirebaseAuthListener() {

        if (authListenerAdded) {
            return
        }

        authListenerAdded = true

        firebaseAuth.addAuthStateListener(listener)
    }

    private fun showLoginLayout() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkUserFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        driverInfoRef
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    loadingDots.visibility = View.GONE
                    loadingDots.clearAnimation()
                    if (snapshot.exists()) {
                        val model = snapshot.getValue(DriverInfoModel::class.java)

                        if (model != null) {
                            goToHomeActivity(model)
                        } else {
                            Toast.makeText(
                                this@SplashScreenActivity,
                                "Failed to load driver data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {

                        startActivity(
                            Intent(
                                this@SplashScreenActivity,
                                RegisterActivity::class.java
                            )
                        )
                        finish()

                        // showRegisterUserLayout()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
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
        startHome()
    }


    private fun startHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
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

    override fun onStop() {
        super.onStop()

        if (authListenerAdded) {
            firebaseAuth.removeAuthStateListener(listener)
            authListenerAdded = false
        }
    }

    override fun onDestroy() {
        splashDisposable?.dispose()

        networkMonitor.unregister()

        if (authListenerAdded) {
            firebaseAuth.removeAuthStateListener(listener)
            authListenerAdded = false
        }

        super.onDestroy()
    }


}