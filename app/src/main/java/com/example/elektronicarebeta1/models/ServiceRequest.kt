data class ServiceRequest(
    val id: String = "",
    val userId: String = "",
    val deviceType: String = "",
    val deviceModel: String = "",
    val issue: String = "",
    val images: List<String> = emptyList(),
    val scheduledDate: Long = 0,
    val scheduledTime: String = "",
    val status: ServiceStatus = ServiceStatus.PENDING,
    val technicianId: String = "",
    val serviceCenterId: String = "",
    val estimatedCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ServiceStatus {
    PENDING,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}