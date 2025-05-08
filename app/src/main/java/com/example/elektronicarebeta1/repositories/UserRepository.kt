package com.example.elektronicarebeta1.repositories

import com.example.elektronicarebeta1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface UserRepository {
    suspend fun getCurrentUser(): User?
    suspend fun updateUser(user: User): Result<User>
    suspend fun getUserById(userId: String): User?
}

class FirebaseUserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {
    
    override suspend fun getCurrentUser(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return getUserById(userId)
    }

    override suspend fun updateUser(user: User): Result<User> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val userMap = mapOf(
            "fullName" to user.fullName,
            "email" to user.email,
            "mobile" to user.mobile,
            "address" to user.address,
            "city" to user.city,
            "updatedAt" to System.currentTimeMillis()
        )
        
        firestore.collection("users")
            .document(userId)
            .update(userMap)
            .await()
            
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserById(userId: String): User? = try {
        val snapshot = firestore.collection("users")
            .document(userId)
            .get()
            .await()
            
        if (snapshot.exists()) {
            snapshot.toObject(User::class.java)?.copy(id = userId)
        } else null
    } catch (e: Exception) {
        null
    }
}