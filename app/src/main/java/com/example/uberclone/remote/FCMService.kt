package com.example.uberclone.remote

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface FCMService {

    @POST("v1/projects/uberclone-f8fdf/messages:send")
    fun sendNotification(
        @Header("Authorization") accessToken: String,
        @Body body: FCMSendData
    ): Observable<FCMResponse>
}