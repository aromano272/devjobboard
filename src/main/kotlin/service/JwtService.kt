package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.models.UserRole
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

interface JwtService {
    fun create(username: String, isAdmin: Boolean): String
}

class DefaultJwtService(
    private val jwtAudience: String,
    private val jwtDomain: String,
    private val jwtSecret: String,
) : JwtService {

    constructor(config: ApplicationConfig) : this(
        jwtAudience = config.property("jwt.audience").getString(),
        jwtDomain = config.property("jwt.domain").getString(),
        jwtSecret = config.property("jwt.secret").getString(),
    )

    override fun create(username: String, isAdmin: Boolean): String = JWT.create()
        .withAudience(jwtAudience)
        .withIssuer(jwtDomain)
        .withClaim("username", username)
        .withClaim("role", (if (isAdmin) UserRole.ADMIN else UserRole.USER).asJwtClaim())
        .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 hour expiration
        .sign(Algorithm.HMAC256(jwtSecret))

}
