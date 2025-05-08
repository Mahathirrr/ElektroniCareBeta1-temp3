package com.example.elektronicarebeta1.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Technician(
    val id: String = "",
    val name: String = "",
    val profileImage: String = "",
    val specializations: List<String> = emptyList(),
    val experience: Int = 0,
    val completedJobs: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val location: String = "",
    val workingHours: WorkingHours = WorkingHours(),
    val services: List<Service> = emptyList(),
    val priceRange: PriceRange = PriceRange()
) : Parcelable

@Parcelize
data class WorkingHours(
    val start: String = "08:00",
    val end: String = "20:00"
) : Parcelable

@Parcelize
data class Service(
    val name: String = "",
    val description: String = "",
    val basePrice: Double = 0.0
) : Parcelable

@Parcelize
data class PriceRange(
    val min: Double = 200000.0, // In IDR
    val max: Double = 2000000.0
) : Parcelable