package com.example.uberclone.remote

import com.example.uberclone.model.AcceptNotificationRequest
import com.example.uberclone.model.CancellationNotificationRequest
import com.example.uberclone.model.TripCompletedNotificationRequest
import com.example.uberclone.model.TripStartedNotificationRequest
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface CloudFunctionService {

    @POST("sendAcceptNotification")
    fun sendAcceptNotification(
        @Body body: AcceptNotificationRequest
    ): Observable<CloudFunctionResponse>

    @POST("sendRideCancellationNotification")
    fun sendRideCancellationNotification(
        @Body body: CancellationNotificationRequest
    ): Observable<CloudFunctionResponse>

    @POST("sendTripStartedNotification")
    fun sendTripStartedNotification(
        @Body body: TripStartedNotificationRequest
    ): Observable<CloudFunctionResponse>

    @POST("sendTripCompletedNotification")
    fun sendTripCompletedNotification(
        @Body body: TripCompletedNotificationRequest
    ): Observable<CloudFunctionResponse>

}