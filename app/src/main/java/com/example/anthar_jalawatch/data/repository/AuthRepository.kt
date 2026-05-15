package com.example.anthar_jalawatch.data.repository

import com.example.anthar_jalawatch.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login failed: User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String,
        village: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                // Save user profile to Firestore
                val userModel = User(
                    uid = user.uid,
                    email = email,
                    fullName = fullName,
                    village = village
                )
                db.collection("users").document(user.uid).set(userModel).await()
                Result.success(user)
            } else {
                Result.failure(Exception("Signup failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveGoogleUserToFirestoreIfNew(
        uid: String,
        email: String,
        fullName: String
    ): Result<Unit> {
        return try {
            val docRef = db.collection("users").document(uid)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                val userModel = User(
                    uid = uid,
                    email = email,
                    fullName = fullName,
                    village = "Not specified" // Or prompt later
                )
                docRef.set(userModel).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
