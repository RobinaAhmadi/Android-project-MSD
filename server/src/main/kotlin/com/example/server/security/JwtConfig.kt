package com.example.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.util.UUID

object JwtConfig {
    private const val secret = "change-me-super-secret"
    private const val issuer = "EvenlyAuthServer"
    private const val audience = "EvenlyClients"
    const val realm = "EvenlyAuth"

    private val algorithm = Algorithm.HMAC512(secret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun makeToken(userId: UUID, expiresInMillis: Long = 1000L * 60 * 60 * 24): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId.toString())
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + expiresInMillis))
            .sign(algorithm)
    }
}
