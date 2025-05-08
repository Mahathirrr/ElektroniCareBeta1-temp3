package com.example.elektronicarebeta1.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RepairRequest(
    val id: String = "",
    val userId: String = "",
    val deviceType: String = "",
    val deviceModel: String = "",
    val issue: String = "",
    val images: List<String> = emptyList(),
    val scheduledDate: Long = 0,
    val scheduledTime: String = "",
    val status: RepairStatus = RepairStatus.PENDING,
    val technicianId: String = "",
    val serviceCenterId: String = "",
    val estimatedCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class RepairStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}