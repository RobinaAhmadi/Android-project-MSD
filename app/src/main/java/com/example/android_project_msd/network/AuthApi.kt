package com.example.android_project_msd.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String? = null
)

data class LoginResponse(
    val sessionId: String,
    val token: String,
    val user: RemoteUser
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String? = null
)

typealias RegisterResponse = RemoteUser

data class RemoteUser(
    val id: String,
    val email: String
)

data class SessionResponse(
    val sessionId: String,
    val userId: String,
    val deviceId: String?,
    val createdAt: Long,
    val lastSeen: Long
)

interface AuthApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("logout")
    suspend fun logout(@Header("Authorization") bearer: String)

    @GET("session")
    suspend fun session(@Header("Authorization") bearer: String): SessionResponse
}
