package com.example.uberclone.remote

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitCloudFunction {

    val instance: Retrofit? = null
        get() = if (field == null)
            Retrofit.Builder()
                .baseUrl("https://us-central1-uberclone-f8fdf.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
        else
            field
}