package com.example.android_project_msd.auth

import android.content.Context
import com.example.android_project_msd.data.SessionManager
import com.example.android_project_msd.network.AuthApi
import com.example.android_project_msd.network.LoginRequest
import com.example.android_project_msd.network.LoginResponse
import com.example.android_project_msd.network.NetworkModule
import com.example.android_project_msd.network.RegisterRequest
import com.example.android_project_msd.network.RemoteUser
import com.example.android_project_msd.utils.DeviceIdProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(context: Context) {
    private val api: AuthApi = NetworkModule.authApi
    private val sessionManager = SessionManager(context)
    private val deviceIdProvider = DeviceIdProvider(context)

    suspend fun login(email: String, password: String): LoginResponse = withContext(Dispatchers.IO) {
        val response = api.login(
            LoginRequest(
                email = email,
                password = password,
                deviceId = deviceIdProvider.getDeviceId()
            )
        )
        sessionManager.saveSession(
            token = response.token,
            sessionId = response.sessionId,
            userId = response.user.id,
            email = response.user.email
        )
        response
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val token = sessionManager.getToken() ?: return@withContext
        val bearer = "Bearer $token"
        try {
            api.logout(bearer)
        } catch (ex: HttpException) {
            if (ex.code() != 401) throw ex
        } catch (_: IOException) {
            // ignore connectivity errors on logout
        } finally {
            sessionManager.clear()
        }
    }

    fun cachedToken(): String? = sessionManager.getToken()

    suspend fun register(email: String, password: String, displayName: String?): RemoteUser =
        withContext(Dispatchers.IO) {
            api.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    displayName = displayName
                )
            )
        }

    suspend fun verifySession(): Boolean = withContext(Dispatchers.IO) {
        val token = sessionManager.getToken() ?: return@withContext false
        val bearer = "Bearer $token"
        return@withContext try {
            api.session(bearer)
            true
        } catch (ex: HttpException) {
            if (ex.code() == 401) {
                sessionManager.clear()
                false
            } else {
                throw ex
            }
        } catch (_: IOException) {
            true // treat transient network errors as valid to avoid booting users offline unnecessarily
        }
    }
}
