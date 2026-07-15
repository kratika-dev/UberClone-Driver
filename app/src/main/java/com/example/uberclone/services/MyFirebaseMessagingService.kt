package com.example.uberclone.services

import android.util.Log
import com.example.uberclone.model.DeclineRequestFromDriver
import com.example.uberclone.model.DriverRequestReceived
import com.example.uberclone.utils.Constants
import com.example.uberclone.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (FirebaseAuth.getInstance().currentUser != null) {
            UserUtils.updateToken(this, token)
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data

        Log.d("FCM_TEST", "Title = ${data[Constants.NOTI_TITLE]}")
        Log.d("FCM_TEST", "Body = ${data[Constants.NOTI_BODY]}")
        Log.d("FCM_TEST", "RiderKey = ${data[Constants.RIDER_KEY]}")
        Log.d("FCM_TEST", "Pickup = ${data[Constants.PICKUP_LOCATION]}")
        Log.d("FCM_TEST", "RequestId = ${data[Constants.REQUEST_ID]}")

        if (data.isNotEmpty()) {

            when (data[Constants.NOTI_TITLE]) {

                Constants.REQUEST_DRIVER_TITLE -> {

                    EventBus.getDefault().postSticky(
                        DriverRequestReceived(
                            data[Constants.RIDER_KEY] ?: "",
                          data[Constants.PICKUP_LOCATION] ?: "",
                            data[Constants.REQUEST_ID] ?: ""
                        )
                    )
                }

                Constants.REQUEST_DRIVER_DECLINE -> {
                    EventBus.getDefault().postSticky(DeclineRequestFromDriver())
                }

                else -> {
                    Constants.ShowNotification(
                        this,
                        kotlin.random.Random.nextInt(),
                        data[Constants.NOTI_TITLE],
                        data[Constants.NOTI_BODY],
                        null
                    )
                }
            }
        }
    }


}