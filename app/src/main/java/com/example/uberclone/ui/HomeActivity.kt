package com.example.uberclone.ui

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.uberclone.databinding.ActivityHomeBinding
import com.example.uberclone.R
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI
import com.example.uberclone.SplashScreenActivity
import com.example.uberclone.utils.Constants
import com.example.uberclone.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.StringBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.os.Build
import android.provider.Settings

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    private lateinit var navView: NavigationView

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var navController: NavController

    private lateinit var imageAvatar: CircleImageView

    private lateinit var txtAverageRating: TextView
    private lateinit var txtRatingCount: TextView

    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var uri: Uri? = null

    private lateinit var waitingDialog: AlertDialog

    private lateinit var storageReference: StorageReference
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocation =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseLocation =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true


            if (!fineLocation && !coarseLocation) {

                if (!shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {

                    showPermissionSettingsDialog(
                        "Location permission is permanently denied. Please enable it from settings."
                    )

                } else {

                    Snackbar.make(
                        binding.root,
                        "Location permission is required",
                        Snackbar.LENGTH_LONG
                    ).show()

                }
            }
        }


    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

            if (!isGranted) {

                if (!shouldShowRequestPermissionRationale(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {

                    showPermissionSettingsDialog(
                        "Notification permission is permanently denied. Please enable it from settings."
                    )

                } else {

                    Snackbar.make(
                        binding.root,
                        "Notification permission is recommended",
                        Snackbar.LENGTH_LONG
                    ).show()

                }
            }

            // continue to location permission
            checkLocationPermission()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window.statusBarColor = getColor(R.color.black)
        window.navigationBarColor = getColor(R.color.black)

        setSupportActionBar(binding.appBarHome.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_home)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, _, _ ->

            supportActionBar?.title = "Driver"

        }

        navView.setupWithNavController(navController)

        init()

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                uri = it.data?.data!!
                imageAvatar.setImageURI(uri)

                showUploadDialog()
            }
        }

        checkNotificationPermission()

    }

    private fun init() {
        storageReference = FirebaseStorage.getInstance().reference

        waitingDialog = AlertDialog.Builder(this@HomeActivity)
            .setMessage("Waiting...")
            .setCancelable(false).create()

        navView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {

                R.id.nav_sign_out -> {
                    val builder = AlertDialog.Builder(this@HomeActivity)

                    builder.setTitle("Sign out")
                        .setMessage("Do you really want to sign out?")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Sign Out") { _, _ ->
                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this@HomeActivity, SplashScreenActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                            startActivity(intent)
                            finish()
                        }
                        .setCancelable(false)

                    val dialog = builder.create()

                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getColor(R.color.colorError))      // Red

                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getColor(R.color.colorPrimary))    // Black
                    }

                    dialog.show()

                    true
                }

                R.id.nav_reviews -> {

                    startActivity(
                        Intent(this, DriverReviewsActivity::class.java)
                    )

                  //  drawerLayout.closeDrawers()

                    true
                }

                R.id.nav_earnings -> {

                    startActivity(
                        Intent(this, EarningsActivity::class.java)
                    )

                 //   drawerLayout.closeDrawers()

                    true
                }

                R.id.nav_recent_trips -> {

                    startActivity(
                        Intent(this, DriverRecentTripsActivity::class.java)
                    )

               //     drawerLayout.closeDrawers()
                    true
                }

                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)

                    drawerLayout.closeDrawers()

                    true
                }
            }

        }

        val headerView = navView.getHeaderView(0)

        val textName = headerView.findViewById<TextView>(R.id.text_view_name)
        val textViewPhone = headerView.findViewById<TextView>(R.id.text_view_phone)
        imageAvatar = headerView.findViewById(R.id.img_avatar)
        txtAverageRating = headerView.findViewById(R.id.txtAverageRating)
        txtRatingCount = headerView.findViewById(R.id.txtRatingCount)



        if (Constants.currentUser != null && Constants.currentUser?.avatar != null && !TextUtils.isEmpty(
                Constants.currentUser?.avatar
            )
        ) {
            Picasso.get()
                .load(Constants.currentUser?.avatar)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(imageAvatar)

        }else {
            imageAvatar.setImageResource(R.drawable.profile)
        }

        imageAvatar.setOnClickListener {
            getImage()
        }
        textName.text = Constants.buildWelcomeMessage()
        textViewPhone.text = Constants.currentUser?.phoneNumber

        Constants.currentUser?.let { driver ->

            txtAverageRating.text =
                "%.1f".format(driver.rating)

            txtRatingCount.text =
                "${driver.ratingCount} ratings"

        }
    }

    private fun showUploadDialog() {
        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setTitle("Change Avatar")
        builder.setMessage("Do you really want to change the avatar")
            .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("CHANGE") { dialog, _ ->
                if (uri != null) {
                    waitingDialog.show()

                    val filePath = storageReference

                        .child("avatar_images")

                        .child(uri!!.lastPathSegment!!)

                    filePath.putFile(uri!!)

                        .addOnSuccessListener { task ->

                            val result = task.metadata?.reference?.downloadUrl

                            result?.addOnSuccessListener { it ->

                                uri = it
                                val updateData = mutableMapOf<String, Any>()
                                updateData.put("avatar", uri.toString())
                                UserUtils.updateuser(drawerLayout, updateData)
                                waitingDialog.dismiss()
                            }

                        }.addOnProgressListener { taskSnapshot ->
                            val progress =
                                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                            waitingDialog.setMessage(
                                StringBuilder("Uploading: ").append(progress).append("%")
                            )

                        }.addOnFailureListener {

                            waitingDialog.dismiss()

                            Snackbar.make(
                                drawerLayout,
                                it.message ?: "Upload failed",
                                Snackbar.LENGTH_SHORT
                            ).show()

                        }

                }
                dialog.dismiss()

            }.setCancelable(false)

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.black))
        }
        dialog.show()

    }

    private fun getImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getResult.launch(intent)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun checkNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )

        } else {

            checkLocationPermission()

        }
    }



    private fun checkLocationPermission() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (!fineGranted && !coarseGranted) {

            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }



    private fun showPermissionSettingsDialog(message: String) {

        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage(message)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .show()
    }



    private fun openAppSettings() {

        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )

        startActivity(intent)

    }
}