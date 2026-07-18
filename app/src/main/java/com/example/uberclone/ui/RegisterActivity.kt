package com.example.uberclone.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uberclone.databinding.ActivityRegisterBinding
import com.example.uberclone.model.DriverInfoModel
import com.example.uberclone.utils.Constants
import com.example.uberclone.utils.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var driverInfoRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadingDialog = LoadingDialog(this)

        driverInfoRef = FirebaseDatabase
            .getInstance()
            .getReference(Constants.DRIVER_INFO_REFERENCE)

        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        binding.editPhone.setText(phoneNumber)

        binding.buttonRegister.setOnClickListener {

            val firstName = binding.editFirstName.text.toString().trim()
            val lastName = binding.editLastName.text.toString().trim()
            val phone = binding.editPhone.text.toString().trim()

            if (firstName.isEmpty()) {
                binding.editFirstName.error = "Enter first name"
                binding.editFirstName.requestFocus()
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                binding.editLastName.error = "Enter last name"
                binding.editLastName.requestFocus()
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                binding.editPhone.error = "Enter phone number"
                binding.editPhone.requestFocus()
                return@setOnClickListener
            }

            val driver = DriverInfoModel(
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phone,
                avatar = "",
                rating = 5f,
                totalRating = 0f,
                ratingCount = 0,
                isOnline = false,
                isBusy = false
            )

            val uid = firebaseAuth.currentUser?.uid ?: return@setOnClickListener
            binding.buttonRegister.isEnabled = false
            loadingDialog.show()

            driverInfoRef.child(uid)
                .setValue(driver)
                .addOnSuccessListener {
                    loadingDialog.dismiss()
                    binding.buttonRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        "Registration Successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    Constants.currentUser = driver

                    startActivity(
                        Intent(
                            this,
                            HomeActivity::class.java
                        )
                    )

                    finishAffinity()
                }
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    binding.buttonRegister.isEnabled = true
                    Toast.makeText(
                        this,
                        it.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}