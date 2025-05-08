package com.example.elektronicarebeta1.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val mobile: String = "",
    val address: String = "",
    val city: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable