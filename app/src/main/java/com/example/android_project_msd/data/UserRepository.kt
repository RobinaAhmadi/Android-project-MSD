package com.example.android_project_msd.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Create a new user account with email and password
     */
    suspend fun createUser(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cvv: String
    ): Result<User> {
        return try {
            Log.d("UserRepository", "Starting user creation for: $email")

            // Create Firebase Auth user
            Log.d("UserRepository", "Creating Firebase Auth user...")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user")
            Log.d("UserRepository", "Firebase Auth user created with ID: $userId")

            // Create user document in Firestore
            val user = User(
                id = userId,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                cardHolderName = cardHolderName,
                cardNumber = cardNumber,
                expiryDate = expiryDate,
                cvv = cvv
            )

            Log.d("UserRepository", "Saving user data to Firestore...")
            usersCollection.document(userId).set(user).await()
            Log.d("UserRepository", "User created successfully!")

            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating user: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User not found")

            val user = getUserById(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get user by email
     */
    suspend fun getUserByEmail(email: String): User? {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.first().toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update user data
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Get current Firebase Auth user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}

