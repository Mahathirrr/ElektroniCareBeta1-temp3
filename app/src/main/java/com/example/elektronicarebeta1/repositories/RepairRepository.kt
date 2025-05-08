package com.example.elektronicarebeta1.repositories

import com.example.elektronicarebeta1.models.RepairRequest
import com.example.elektronicarebeta1.models.RepairStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

interface RepairRepository {
    suspend fun createRepairRequest(request: RepairRequest): Result<RepairRequest>
    suspend fun getRepairRequests(): Flow<List<RepairRequest>>
    suspend fun getRepairRequestById(requestId: String): RepairRequest?
    suspend fun updateRepairStatus(requestId: String, status: RepairStatus): Result<Unit>
    suspend fun uploadRepairImage(imageBytes: ByteArray): Result<String>
}

class FirebaseRepairRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : RepairRepository {

    override suspend fun createRepairRequest(request: RepairRequest): Result<RepairRequest> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val requestId = UUID.randomUUID().toString()
        
        val requestWithIds = request.copy(
            id = requestId,
            userId = userId
        )
        
        firestore.collection("repair_requests")
            .document(requestId)
            .set(requestWithIds)
            .await()
            
        Result.success(requestWithIds)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getRepairRequests(): Flow<List<RepairRequest>> = flow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        val snapshot = firestore.collection("repair_requests")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            
        val requests = snapshot.documents.mapNotNull { doc ->
            doc.toObject(RepairRequest::class.java)?.copy(id = doc.id)
        }
        
        emit(requests)
    }

    override suspend fun getRepairRequestById(requestId: String): RepairRequest? = try {
        val snapshot = firestore.collection("repair_requests")
            .document(requestId)
            .get()
            .await()
            
        if (snapshot.exists()) {
            snapshot.toObject(RepairRequest::class.java)?.copy(id = requestId)
        } else null
    } catch (e: Exception) {
        null
    }

    override suspend fun updateRepairStatus(
        requestId: String,
        status: RepairStatus
    ): Result<Unit> = try {
        firestore.collection("repair_requests")
            .document(requestId)
            .update("status", status)
            .await()
            
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun uploadRepairImage(imageBytes: ByteArray): Result<String> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val imageRef = storage.reference
            .child("repair_images")
            .child(userId)
            .child("${UUID.randomUUID()}.jpg")
            
        val uploadTask = imageRef.putBytes(imageBytes).await()
        val downloadUrl = uploadTask.storage.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}