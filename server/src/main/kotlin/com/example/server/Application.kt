package com.example.server

import com.example.server.db.DatabaseFactory
import com.example.server.db.UserDao
import com.example.server.routes.authRoutes
import com.example.server.security.JwtConfig
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.routing
import org.slf4j.event.Level

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init()

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
    }

    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
        allowHeader("Authorization")
        allowCredentials = true
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId.isNullOrBlank()) null else JWTPrincipal(credential.payload)
            }
        }
    }

    routing {
        authRoutes(UserDao())
    }
}
