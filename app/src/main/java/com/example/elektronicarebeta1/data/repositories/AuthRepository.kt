package com.example.elektronicarebeta1.data.repositories

import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Authentication failed")
            
            val userData = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()
                .toObject(User::class.java)
                ?: throw Exception("User data not found")
                
            Result.Success(userData)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signUpWithEmail(
        email: String, 
        password: String,
        fullName: String,
        mobile: String
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")
            
            val newUser = User(
                id = user.uid,
                fullName = fullName,
                email = email,
                mobile = mobile
            )
            
            firestore.collection("users")
                .document(user.uid)
                .set(newUser)
                .await()
                
            Result.Success(newUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign in failed")
            
            // Check if user exists
            val userDoc = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()
            
            val userData = if (userDoc.exists()) {
                userDoc.toObject(User::class.java)!!
            } else {
                // Create new user
                val newUser = User(
                    id = user.uid,
                    fullName = user.displayName ?: "",
                    email = user.email ?: "",
                    mobile = ""
                )
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(newUser)
                    .await()
                    
                newUser
            }
            
            Result.Success(userData)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): User? {
        return auth.currentUser?.let { firebaseUser ->
            User(
                id = firebaseUser.uid,
                fullName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                mobile = ""
            )
        }
    }
}