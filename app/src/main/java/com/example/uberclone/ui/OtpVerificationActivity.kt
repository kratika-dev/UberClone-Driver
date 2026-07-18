package com.example.uberclone.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uberclone.databinding.ActivityOtpVerificationBinding
import com.example.uberclone.model.DriverInfoModel
import com.example.uberclone.utils.Constants
import com.example.uberclone.utils.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerificationBinding

    private lateinit var verificationId: String

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var phoneNumber: String

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadingDialog = LoadingDialog(this)

        verificationId = intent.getStringExtra("verificationId") ?: ""

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        binding.txtPhone.text =
            "Enter the 6-digit code sent to +91 $phoneNumber"

        startTimer()

        binding.btnBack.setOnClickListener {
            finish()
        }

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        binding.txtPhone.text =
            "Enter the 6-digit code sent to +91 $phoneNumber"

        binding.buttonVerify.setOnClickListener {

            val otp = binding.editOtp.text.toString().trim()

            if (otp.length != 6) {

                binding.editOtp.error = "Enter valid OTP"
                return@setOnClickListener
            }
            binding.buttonVerify.isEnabled = false
            loadingDialog.show()
            verifyOtp(otp)
        }

        binding.txtResend.setOnClickListener {
            // Resend OTP
        }
    }

    private fun startTimer() {

        object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.txtTimer.text =
                    "Resend OTP in 00:${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                binding.txtTimer.text = ""
                binding.txtResend.isEnabled = true
            }

        }.start()
    }

    private fun verifyOtp(code: String) {

        val credential = PhoneAuthProvider.getCredential(
            verificationId,
            code
        )

        signInWithCredential(credential)
    }

    private fun signInWithCredential(
        credential: PhoneAuthCredential
    ) {

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                loadingDialog.dismiss()
                binding.buttonVerify.isEnabled = true
                if (task.isSuccessful()) {

                    checkUserFromFirebase()
                } else {

                    Toast.makeText(
                        this,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun checkUserFromFirebase() {

        val driverInfoRef = FirebaseDatabase.getInstance()
            .getReference(Constants.DRIVER_INFO_REFERENCE)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        driverInfoRef.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val driver = snapshot.getValue(DriverInfoModel::class.java)

                        Constants.currentUser = driver

                        loadingDialog.dismiss()
                        startActivity(
                            Intent(
                                this@OtpVerificationActivity,
                                HomeActivity::class.java
                            )
                        )

                        finishAffinity()

                    } else {

                        val intent = Intent(
                            this@OtpVerificationActivity,
                            RegisterActivity::class.java
                        )

                        loadingDialog.dismiss()
                        binding.buttonVerify.isEnabled = true
                        intent.putExtra("phoneNumber", phoneNumber)

                        startActivity(intent)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    loadingDialog.dismiss()
                    binding.buttonVerify.isEnabled = true
                    Toast.makeText(
                        this@OtpVerificationActivity,
                        error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

}