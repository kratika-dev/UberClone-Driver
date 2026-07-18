package com.example.uberclone.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uberclone.databinding.ActivityLoginBinding
import com.example.uberclone.utils.LoadingDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var phoneNumber: String

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LOGIN_DEBUG", "Login onCreate")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadingDialog = LoadingDialog(this)

        initCallbacks()

        initClickListeners()
    }

    private fun initClickListeners() {

        binding.buttonContinue.setOnClickListener {

            phoneNumber = binding.editPhone.text.toString().trim()

            if (phoneNumber.isEmpty()) {
                binding.editPhone.error = "Enter phone number"
                binding.editPhone.requestFocus()
                return@setOnClickListener
            }

            if (phoneNumber.length != 10) {
                binding.editPhone.error = "Enter a valid 10-digit phone number"
                binding.editPhone.requestFocus()
                return@setOnClickListener
            }

            sendOtp("+91$phoneNumber")
        }



    }

    private fun initCallbacks() {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(
                credential: PhoneAuthCredential
            ) {
                // loadingDialog.dismiss()
            }

            override fun onVerificationFailed(
                e: FirebaseException
            ) {
                loadingDialog.dismiss()
                binding.buttonContinue.isEnabled = true
                Toast.makeText(
                    this@LoginActivity,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)

                loadingDialog.dismiss()

                binding.buttonContinue.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    "OTP Sent...",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java)
                intent.putExtra("verificationId", verificationId)
                intent.putExtra("phoneNumber", phoneNumber)

                startActivity(intent)
            }
        }
    }


    private fun sendOtp(phoneNumber: String) {
        binding.buttonContinue.isEnabled = false
        loadingDialog.show()
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroy() {
        Log.d("LOGIN_DEBUG", "Login onDestroy")
        super.onDestroy()
    }

}