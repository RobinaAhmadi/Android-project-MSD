package com.example.android_project_msd.model.auth

import android.content.Context
import com.example.android_project_msd.model.auth.remote.AuthApiService
import com.example.android_project_msd.model.auth.remote.LoginRequest
import com.example.android_project_msd.model.auth.remote.NetworkModule
import com.example.android_project_msd.model.auth.remote.RegisterRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepository private constructor(
    private val api: AuthApiService,
    private val tokenStore: TokenStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun login(email: String, password: String): Result<Unit> = withContext(dispatcher) {
        authCall {
            val response = api.login(LoginRequest(email.trim(), password))
            tokenStore.save(response.token)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): Result<Unit> =
        withContext(dispatcher) {
            authCall {
                val response = api.register(
                    RegisterRequest(
                        email = email.trim(),
                        password = password,
                        displayName = displayName
                    )
                )
                tokenStore.save(response.token)
            }
        }

    suspend fun logout() = withContext(dispatcher) {
        tokenStore.clear()
    }

    private suspend fun <T> authCall(block: suspend () -> T): Result<T> =
        runCatching { block() }.recoverCatching { throwable ->
            when (throwable) {
                is HttpException -> {
                    val message = when (throwable.code()) {
                        401 -> "Wrong email or password"
                        409 -> "An account with this email already exists."
                        else -> "Server error (${throwable.code()})"
                    }
                    throw IllegalStateException(message, throwable)
                }

                else -> throw throwable
            }
        }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(
                    api = NetworkModule.authApi,
                    tokenStore = TokenStore.getInstance(context)
                ).also { instance = it }
            }
    }
}
