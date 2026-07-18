package com.example.uberclone.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.uberclone.model.Token
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import com.example.uberclone.R
import com.example.uberclone.model.AcceptNotificationRequest
import com.example.uberclone.model.CancellationNotificationRequest
import com.example.uberclone.model.DeclineNotificationRequest
import com.example.uberclone.model.TripCompletedNotificationRequest
import com.example.uberclone.model.TripStartedNotificationRequest
import com.example.uberclone.remote.CloudFunctionService
import com.example.uberclone.remote.RetrofitCloudFunction


object UserUtils {
    fun updateuser(
        view: View?,
        updateData: Map<String, Any>
    ) {
        FirebaseDatabase.getInstance()
            .getReference(Constants.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .updateChildren(updateData)
            .addOnSuccessListener {
                Snackbar.make(view!!, "Data Updated Successfully!", Snackbar.LENGTH_LONG).show()
            }.addOnFailureListener {
                Snackbar.make(view!!, it.message!!, Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        Log.d("FCM_TOKEN", token)
        val tokenModel = Token(token)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(uid)
            .setValue(tokenModel)   //Save Token object
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {

            }
    }

    fun sendDeclineRequest(
        view: View,
        activity: Activity?,
        riderId: String,
        requestId: String
    ) {

        val compositeDisposable = CompositeDisposable()

        val cloudFunctionService =
            RetrofitCloudFunction.instance!!.create(CloudFunctionService::class.java)

        FirebaseDatabase.getInstance()
            .getReference("RideRequests")
            .child(requestId)
            .child("status")
            .setValue("DECLINED")

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(riderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val tokenModel = snapshot.getValue(Token::class.java)
                        val riderToken = tokenModel?.token

                        if (riderToken.isNullOrEmpty()) {

                            Snackbar.make(
                                view,
                                activity!!.getString(R.string.token_not_found),
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }

                        val request = DeclineNotificationRequest(
                            riderToken = riderToken,
                            driverKey = FirebaseAuth.getInstance().currentUser!!.uid,
                            requestId = requestId
                        )

                        compositeDisposable.add(
                            cloudFunctionService.sendDeclineNotification(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ response ->

                                    Log.d("DECLINE_CF", "success = ${response.success}")
                                    Log.d("DECLINE_CF", "message = ${response.message}")
                                    Log.d("DECLINE_CF", "response = ${response.response}")

                                    if (response.success) {

                                        Toast.makeText(
                                            activity,
                                            "Decline request sent to rider",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {

                                        Snackbar.make(
                                            view,
                                            response.message,
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }

                                    compositeDisposable.clear()

                                }, { t ->

                                    compositeDisposable.clear()

                                    Snackbar.make(
                                        view,
                                        t.message ?: "Unknown error",
                                        Snackbar.LENGTH_LONG
                                    ).show()

                                })
                        )

                    } else {

                        Snackbar.make(
                            view,
                            activity!!.getString(R.string.token_not_found),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Snackbar.make(
                        view,
                        "error->${error.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
    }


    fun sendAcceptRequest(
        view: View,
        activity: Activity?,
        key: String,
        requestId: String
    ) {

        val compositeDisposable = CompositeDisposable()
        val cloudFunctionService =
            RetrofitCloudFunction.instance!!.create(CloudFunctionService::class.java)

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        val tokenModel = snapshot.getValue(Token::class.java)
                        val riderToken = tokenModel?.token

                        if (riderToken.isNullOrEmpty()) {
                            Snackbar.make(
                                view,
                                activity!!.getString(R.string.token_not_found),
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }


                        val request = AcceptNotificationRequest(
                            riderToken = riderToken,
                            driverKey = FirebaseAuth.getInstance().currentUser!!.uid,
                            requestId = requestId
                        )

                        compositeDisposable.add(
                            cloudFunctionService.sendAcceptNotification(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ response ->

                                    Log.d("ACCEPT_CF", "success = ${response.success}")
                                    Log.d("ACCEPT_CF", "message = ${response.message}")
                                    Log.d("ACCEPT_CF", "response = ${response.response}")

                                    if (response.success) {

                                        Toast.makeText(
                                            activity,
                                            "Accept request sent to rider",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {

                                        Snackbar.make(
                                            view,
                                            response.message,
                                            Snackbar.LENGTH_LONG
                                        ).show()

                                    }

                                    compositeDisposable.clear()

                                }, { t ->

                                    compositeDisposable.clear()

                                    Snackbar.make(
                                        view,
                                        t.message ?: "Unknown error",
                                        Snackbar.LENGTH_LONG
                                    ).show()

                                })
                        )


                    } else {

                        Snackbar.make(
                            view,
                            activity!!.getString(R.string.token_not_found),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(view, "error->" + error.message, Snackbar.LENGTH_LONG).show()
                }
            })
    }


    fun sendRideCancellationRequest(
        context: Context,
        requestId: String,
        cancelledBy: String
    ) {

        val compositeDisposable = CompositeDisposable()

        val cloudFunctionService =
            RetrofitCloudFunction.instance!!
                .create(CloudFunctionService::class.java)

        val request = CancellationNotificationRequest(
            requestId = requestId,
            cancelledBy = cancelledBy
        )

        Log.d("CANCEL_CF", "Request = $request")

        compositeDisposable.add(

            cloudFunctionService
                .sendRideCancellationNotification(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->

                    Log.d("CANCEL_CF", "Success = ${response.success}")
                    Log.d("CANCEL_CF", "Message = ${response.message}")

                    if (response.success) {

                        Toast.makeText(
                            context,
                            "Cancellation notification sent",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            context,
                            response.message,
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    compositeDisposable.clear()

                }, { t ->

                    compositeDisposable.clear()

                    Log.e("CANCEL_CF", "Error", t)

                    Toast.makeText(
                        context,
                        t.message ?: "Unknown error",
                        Toast.LENGTH_SHORT
                    ).show()

                })
        )
    }

    fun sendTripStartedRequest(
        view: View,
        activity: Activity?,
        riderKey: String,
        requestId: String
    ) {

        val compositeDisposable = CompositeDisposable()

        val cloudFunctionService =
            RetrofitCloudFunction.instance!!
                .create(CloudFunctionService::class.java)

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(riderKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val tokenModel = snapshot.getValue(Token::class.java)
                        val riderToken = tokenModel?.token

                        if (riderToken.isNullOrEmpty()) {

                            Snackbar.make(
                                view,
                                activity!!.getString(R.string.token_not_found),
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }

                        val request = TripStartedNotificationRequest(
                            riderToken = riderToken,
                            requestId = requestId
                        )

                        compositeDisposable.add(
                            cloudFunctionService
                                .sendTripStartedNotification(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ response ->

                                    Log.d("TRIP_STARTED_CF", "success = ${response.success}")
                                    Log.d("TRIP_STARTED_CF", "message = ${response.message}")
                                    Log.d("TRIP_STARTED_CF", "response = ${response.response}")

                                    if (response.success) {

                                        Toast.makeText(
                                            activity,
                                            "Trip Started notification sent",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {

                                        Snackbar.make(
                                            view,
                                            response.message,
                                            Snackbar.LENGTH_LONG
                                        ).show()

                                    }

                                    compositeDisposable.clear()

                                }, { t ->

                                    compositeDisposable.clear()

                                    Snackbar.make(
                                        view,
                                        t.message ?: "Unknown error",
                                        Snackbar.LENGTH_LONG
                                    ).show()

                                })
                        )

                    } else {

                        Snackbar.make(
                            view,
                            activity!!.getString(R.string.token_not_found),
                            Snackbar.LENGTH_LONG
                        ).show()

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Snackbar.make(
                        view,
                        error.message,
                        Snackbar.LENGTH_LONG
                    ).show()

                }
            })
    }


    fun sendTripCompletedRequest(
        view: View,
        activity: Activity?,
        riderKey: String,
        requestId: String
    ) {

        val compositeDisposable = CompositeDisposable()

        val cloudFunctionService =
            RetrofitCloudFunction.instance!!
                .create(CloudFunctionService::class.java)

        FirebaseDatabase.getInstance()
            .getReference(Constants.TOKEN_REFERENCE)
            .child(riderKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val tokenModel = snapshot.getValue(Token::class.java)
                        val riderToken = tokenModel?.token

                        if (riderToken.isNullOrEmpty()) {

                            Snackbar.make(
                                view,
                                activity!!.getString(R.string.token_not_found),
                                Snackbar.LENGTH_LONG
                            ).show()
                            return
                        }

                        val request = TripCompletedNotificationRequest(
                            riderToken = riderToken,
                            requestId = requestId
                        )

                        compositeDisposable.add(
                            cloudFunctionService
                                .sendTripCompletedNotification(request)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ response ->

                                    Log.d("TRIP_COMPLETED_CF", "success = ${response.success}")
                                    Log.d("TRIP_COMPLETED_CF", "message = ${response.message}")
                                    Log.d("TRIP_COMPLETED_CF", "response = ${response.response}")

                                    if (response.success) {

                                        Toast.makeText(
                                            activity,
                                            "Trip Completed notification sent",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {

                                        Snackbar.make(
                                            view,
                                            response.message,
                                            Snackbar.LENGTH_LONG
                                        ).show()

                                    }

                                    compositeDisposable.clear()

                                }, { t ->

                                    compositeDisposable.clear()

                                    Snackbar.make(
                                        view,
                                        t.message ?: "Unknown error",
                                        Snackbar.LENGTH_LONG
                                    ).show()

                                })
                        )

                    } else {

                        Snackbar.make(
                            view,
                            activity!!.getString(R.string.token_not_found),
                            Snackbar.LENGTH_LONG
                        ).show()

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Snackbar.make(
                        view,
                        error.message,
                        Snackbar.LENGTH_LONG
                    ).show()

                }
            })
    }

}

