interface ServiceRepository {
    suspend fun createServiceRequest(request: ServiceRequest): Result<ServiceRequest>
    suspend fun getServiceRequests(userId: String): Flow<List<ServiceRequest>>
    suspend fun getServiceRequestById(requestId: String): ServiceRequest?
    suspend fun updateServiceStatus(requestId: String, status: ServiceStatus): Result<Unit>
    suspend fun uploadServiceImage(imageBytes: ByteArray): Result<String>
    suspend fun getServiceCenters(): Flow<List<ServiceCenter>>
    suspend fun getServiceCenterById(centerId: String): ServiceCenter?
}

class FirebaseServiceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : ServiceRepository {
    
    override suspend fun createServiceRequest(request: ServiceRequest): Result<ServiceRequest> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val requestId = UUID.randomUUID().toString()
        
        val requestWithIds = request.copy(
            id = requestId,
            userId = userId
        )
        
        firestore.collection("service_requests")
            .document(requestId)
            .set(requestWithIds)
            .await()
            
        Result.success(requestWithIds)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getServiceRequests(userId: String): Flow<List<ServiceRequest>> = flow {
        val snapshot = firestore.collection("service_requests")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            
        val requests = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ServiceRequest::class.java)?.copy(id = doc.id)
        }
        
        emit(requests)
    }

    override suspend fun getServiceRequestById(requestId: String): ServiceRequest? = try {
        val doc = firestore.collection("service_requests")
            .document(requestId)
            .get()
            .await()
            
        if (doc.exists()) {
            doc.toObject(ServiceRequest::class.java)?.copy(id = doc.id)
        } else null
    } catch (e: Exception) {
        null
    }

    override suspend fun updateServiceStatus(
        requestId: String, 
        status: ServiceStatus
    ): Result<Unit> = try {
        firestore.collection("service_requests")
            .document(requestId)
            .update("status", status)
            .await()
            
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadServiceImage(imageBytes: ByteArray): Result<String> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val imageRef = storage.reference
            .child("service_images")
            .child(userId)
            .child("${UUID.randomUUID()}.jpg")
            
        val uploadTask = imageRef.putBytes(imageBytes).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getServiceCenters(): Flow<List<ServiceCenter>> = flow {
        val snapshot = firestore.collection("service_centers")
            .orderBy("rating", Query.Direction.DESCENDING)
            .get()
            .await()
            
        val centers = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ServiceCenter::class.java)?.copy(id = doc.id)
        }
        
        emit(centers)
    }

    override suspend fun getServiceCenterById(centerId: String): ServiceCenter? = try {
        val doc = firestore.collection("service_centers")
            .document(centerId)
            .get()
            .await()
            
        if (doc.exists()) {
            doc.toObject(ServiceCenter::class.java)?.copy(id = doc.id)
        } else null
    } catch (e: Exception) {
        null
    }
}