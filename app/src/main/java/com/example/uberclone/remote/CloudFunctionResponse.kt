package com.example.uberclone.remote

data class CloudFunctionResponse(
    val success: Boolean,
    val message: String,
    val response: String?
)