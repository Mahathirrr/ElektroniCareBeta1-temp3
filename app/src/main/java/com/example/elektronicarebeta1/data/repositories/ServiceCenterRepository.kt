package com.example.elektronicarebeta1.data.repositories

import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.models.ServiceCenter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ServiceCenterRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getServiceCenters(): Flow<Result<List<ServiceCenter>>> = flow {
        try {
            emit(Result.Loading)
            
            val snapshot = firestore.collection("service_centers")
                .get()
                .await()
                
            val centers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ServiceCenter::class.java)?.copy(id = doc.id)
            }
            
            emit(Result.Success(centers))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    suspend fun getServiceCenterById(centerId: String): Result<ServiceCenter> {
        return try {
            val doc = firestore.collection("service_centers")
                .document(centerId)
                .get()
                .await()
                
            if (doc.exists()) {
                val center = doc.toObject(ServiceCenter::class.java)?.copy(id = doc.id)
                    ?: throw Exception("Failed to parse service center data")
                Result.Success(center)
            } else {
                throw Exception("Service center not found")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getServiceCentersByCategory(category: String): Flow<Result<List<ServiceCenter>>> = flow {
        try {
            emit(Result.Loading)
            
            val snapshot = firestore.collection("service_centers")
                .whereArrayContains("categories", category)
                .get()
                .await()
                
            val centers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ServiceCenter::class.java)?.copy(id = doc.id)
            }
            
            emit(Result.Success(centers))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}