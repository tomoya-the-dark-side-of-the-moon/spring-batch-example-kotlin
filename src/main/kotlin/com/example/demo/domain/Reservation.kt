package com.example.demo.domain

import java.io.Serializable

@JvmRecord
data class Reservation(
    val id: Int?,
    val fileName: String?,
    val host: String?,
    val userName: String?,
    val dbName: String?,
    val password: String?
) : Serializable