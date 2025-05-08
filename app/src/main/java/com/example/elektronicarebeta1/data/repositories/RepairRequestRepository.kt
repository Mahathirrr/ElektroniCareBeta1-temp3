package com.example.elektronicarebeta1.data.repositories

import android.net.Uri
import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.models.RepairRequest
import com.example.elektronicarebeta1.models.RepairStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RepairRequestRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun createRepairRequest(
        request: RepairRequest,
        images: List<Uri>
    ): Result<RepairRequest> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            // Upload images first
            val imageUrls = images.map { uri ->
                uploadImage(uri)
            }
            
            // Create request with image URLs
            val requestWithImages = request.copy(
                userId = userId,
                images = imageUrls,
                createdAt = System.currentTimeMillis()
            )
            
            // Save to Firestore
            firestore.collection("repair_requests")
                .add(requestWithImages)
                .await()
                
            Result.Success(requestWithImages)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val ref = storage.reference
            .child("repair_images")
            .child(UUID.randomUUID().toString())
            
        val uploadTask = ref.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    fun getRepairRequests(): Flow<Result<List<RepairRequest>>> = flow {
        try {
            emit(Result.Loading)
            
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            val snapshot = firestore.collection("repair_requests")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
                
            val requests = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RepairRequest::class.java)?.copy(id = doc.id)
            }
            
            emit(Result.Success(requests))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun getRepairRequestById(requestId: String): Result<RepairRequest> {
        return try {
            val doc = firestore.collection("repair_requests")
                .document(requestId)
                .get()
                .await()
                
            if (doc.exists()) {
                val request = doc.toObject(RepairRequest::class.java)?.copy(id = doc.id)
                    ?: throw Exception("Failed to parse repair request")
                Result.Success(request)
            } else {
                throw Exception("Repair request not found")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateRepairStatus(requestId: String, status: RepairStatus): Result<Unit> {
        return try {
            firestore.collection("repair_requests")
                .document(requestId)
                .update("status", status)
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun cancelRepairRequest(requestId: String): Result<Unit> {
        return try {
            firestore.collection("repair_requests")
                .document(requestId)
                .update(
                    mapOf(
                        "status" to RepairStatus.CANCELLED,
                        "cancelledAt" to System.currentTimeMillis()
                    )
                )
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}