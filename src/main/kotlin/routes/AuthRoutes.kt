package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.UserRole
import com.andreromano.devjobboard.repository.AuthRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val isAdmin: Boolean,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    val refreshToken: String,
)

fun Route.authRoutes(
    authRepository: AuthRepository
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            val tokens = authRepository.registerAndLogin(request.username, request.password, request.isAdmin)
            call.respond(tokens)
        }

        post("/login") {
            val user = call.receive<LoginRequest>()
            val tokens = authRepository.login(user.username, user.password)
            call.respond(tokens)
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()

            val newTokens = authRepository.refreshToken(request.refreshToken)

            call.respond(newTokens)
        }

        authenticate("auth-jwt") {
            post("/logout") {
                val request = call.receive<LogoutRequest>()
                val principal = call.principal<JWTPrincipal>()!!
                val username = principal.getClaim("username", String::class)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                authRepository.logout(username, request.refreshToken)
                call.respondText("Goodbye, ${principal.getClaim("username", String::class)}")
            }
        }
    }

    fun JWTPrincipal.hasRole(role: UserRole): Boolean =
        UserRole.fromJwtClaim(getClaim("role", String::class)) == role
}