package com.example.uberclone.remote

data class Message(
    val token: String,
    val data: Map<String, String>
)