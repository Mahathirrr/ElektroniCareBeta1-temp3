package com.example.elektronicarebeta1.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val id: String = "",
    val userId: String = "",
    val technicianId: String = "",
    val repairRequestId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable