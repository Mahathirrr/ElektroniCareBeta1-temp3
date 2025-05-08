data class ServiceCenter(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val phone: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val services: List<Service> = emptyList(),
    val technicians: List<String> = emptyList(),
    val workingHours: WorkingHours = WorkingHours(),
    val images: List<String> = emptyList()
)

data class WorkingHours(
    val openTime: String = "08:00",
    val closeTime: String = "20:00",
    val daysOpen: List<Int> = listOf(1, 2, 3, 4, 5, 6) // Monday = 1, Sunday = 7
)

data class Service(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val basePrice: Double = 0.0,
    val deviceTypes: List<String> = emptyList()
)