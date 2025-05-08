package com.example.elektronicarebeta1.repositories

import com.example.elektronicarebeta1.models.Technician
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface TechnicianRepository {
    suspend fun getTechnicians(): Flow<List<Technician>>
    suspend fun getTechnicianById(technicianId: String): Technician?
    suspend fun getTechniciansBySpecialization(specialization: String): Flow<List<Technician>>
}

class FirebaseTechnicianRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TechnicianRepository {

    override suspend fun getTechnicians(): Flow<List<Technician>> = flow {
        val snapshot = firestore.collection("technicians")
            .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            
        val technicians = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Technician::class.java)?.copy(id = doc.id)
        }
        
        emit(technicians)
    }

    override suspend fun getTechnicianById(technicianId: String): Technician? = try {
        val snapshot = firestore.collection("technicians")
            .document(technicianId)
            .get()
            .await()
            
        if (snapshot.exists()) {
            snapshot.toObject(Technician::class.java)?.copy(id = technicianId)
        } else null
    } catch (e: Exception) {
        null
    }

    override suspend fun getTechniciansBySpecialization(
        specialization: String
    ): Flow<List<Technician>> = flow {
        val snapshot = firestore.collection("technicians")
            .whereArrayContains("specializations", specialization)
            .orderBy("rating", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
            
        val technicians = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Technician::class.java)?.copy(id = doc.id)
        }
        
        emit(technicians)
    }
}