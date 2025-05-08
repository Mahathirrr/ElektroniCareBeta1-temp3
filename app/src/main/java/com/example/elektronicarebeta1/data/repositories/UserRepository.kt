package com.example.elektronicarebeta1.data.repositories

import android.net.Uri
import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
                
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)?.copy(id = userId)
                    ?: throw Exception("Failed to parse user data")
                Result.Success(user)
            } else {
                throw Exception("User not found")
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateUserProfile(
        fullName: String,
        mobile: String,
        address: String,
        city: String,
        profileImage: Uri? = null
    ): Result<User> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            // Upload profile image if provided
            val imageUrl = profileImage?.let { uri ->
                uploadProfileImage(uri)
            }
            
            val updates = mutableMapOf(
                "fullName" to fullName,
                "mobile" to mobile,
                "address" to address,
                "city" to city,
                "updatedAt" to System.currentTimeMillis()
            )
            
            imageUrl?.let { url ->
                updates["profileImage"] = url
            }
            
            // Update Firestore
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
                
            // Get updated user data
            val updatedDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
                
            val updatedUser = updatedDoc.toObject(User::class.java)?.copy(id = userId)
                ?: throw Exception("Failed to get updated user data")
                
            Result.Success(updatedUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun uploadProfileImage(uri: Uri): String {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        
        val ref = storage.reference
            .child("profile_images")
            .child(userId)
            .child("${UUID.randomUUID()}.jpg")
            
        val uploadTask = ref.putFile(uri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }
}