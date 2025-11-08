package com.example.server.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.server.db.UserDao
import com.example.server.security.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.post
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val email: String,
    val displayName: String? = null
)

@Serializable
data class ErrorResponse(val message: String)

fun Route.authRoutes(userDao: UserDao) {
    route("/api/auth") {
        post("/register") {
            val payload = runCatching { call.receive<RegisterRequest>() }.getOrElse {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
            }

            val email = payload.email.trim().lowercase()
            if (!email.contains("@") || payload.password.length < 6) {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Email or password invalid"))
            }

            val hashed = BCrypt.withDefaults().hashToString(12, payload.password.toCharArray())
            val created = userDao.createUser(email, hashed, payload.displayName)
                ?: return@post call.respond(HttpStatusCode.Conflict, ErrorResponse("Email already registered"))

            val token = JwtConfig.makeToken(created.id)
            call.respond(AuthResponse(token = token, email = created.email, displayName = created.displayName))
        }

        post("/login") {
            val payload = runCatching { call.receive<LoginRequest>() }.getOrElse {
                return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body"))
            }

            val email = payload.email.trim().lowercase()
            val user = userDao.findByEmail(email)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))

            val verified = BCrypt.verifyer().verify(payload.password.toCharArray(), user.passwordHash).verified
            if (!verified) {
                return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
            }

            val token = JwtConfig.makeToken(user.id)
            call.respond(AuthResponse(token = token, email = user.email, displayName = user.displayName))
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Token missing"))
                call.respond(mapOf("userId" to userId))
            }
        }
    }
}
