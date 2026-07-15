package com.example.uberclone.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.uberclone.model.DriverInfoModel
import com.example.uberclone.R
import com.google.android.gms.maps.model.LatLng

object Constants {

    val REQUEST_ID = "requestId"

    val RIDE_REQUEST_REFERENCE = "RideRequests"
    val NOTI_BODY: String = "body"
    val NOTI_TITLE = "title"
    val TOKEN_REFERENCE = "Token"
    var currentUser: DriverInfoModel? = null
    const val DRIVER_INFO_REFERENCE = "DriverInfo"
    const val DRIVER_LOCATION_REFERENCE = "DriverLocation"

    val RIDER_KEY: String = "RiderKey"
    val DRIVER_KEY: String = "DriverKey"
    val PICKUP_LOCATION: String = "PickUpLocation"
    val REQUEST_DRIVER_TITLE: String = "RequestDriver"
    val REQUEST_DRIVER_DECLINE: String = "Decline"
    const val REQUEST_DRIVER_ACCEPT = "REQUEST_DRIVER_ACCEPT"

    const val RIDE_CANCELLED = "RideCancelled"


    fun buildWelcomeMessage(): String {
        return StringBuilder("Welcome, ")
            .append(Constants.currentUser?.firstName)
            .append(" ")
            .append(Constants.currentUser?.lastName)
            .toString()
    }

    fun ShowNotification(
        context: Context,
        id: Int,
        title: String?,
        body: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            pendingIntent =
                PendingIntent.getActivity(
                    context, id, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
        }


        val NOTIFICATION_CHANNEL_ID = "krati_uber_clone"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Uber Clone",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.description = "Uber Clone"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)

        } else {
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setAutoCancel(true)
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
        builder.setDefaults(Notification.DEFAULT_VIBRATE)
        builder.setSmallIcon(R.drawable.ic_car)
        builder.setLargeIcon(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_car
            )
        )

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        val notification = builder.build()
        notificationManager.notify(id, notification)

    }


    fun decodePoly(encoded: String): MutableList<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {

            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if ((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                result shr 1
            }

            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if ((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                result shr 1
            }

            lng += dlng

            val point = LatLng(
                lat / 1E5,
                lng / 1E5
            )

            poly.add(point)
        }

        return poly
    }

    fun getBearing(begin: LatLng, end: LatLng): Float {

        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        return when {
            begin.latitude < end.latitude && begin.longitude < end.longitude ->
                Math.toDegrees(Math.atan(lng / lat)).toFloat()

            begin.latitude >= end.latitude && begin.longitude < end.longitude ->
                (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()

            begin.latitude >= end.latitude && begin.longitude >= end.longitude ->
                (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()

            begin.latitude < end.latitude && begin.longitude >= end.longitude ->
                (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()

            else -> -1f
        }
    }
}