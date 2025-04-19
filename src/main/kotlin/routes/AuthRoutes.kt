package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
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
    authService: AuthService,
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()

            val tokens = authService.registerAndLogin(
                request.username,
                request.email,
                request.password,
                request.isAdmin,
            )
            call.respond(tokens)
        }

        post("/login") {
            val user = call.receive<LoginRequest>()
            val tokens = authService.login(user.username, user.password)
            call.respond(tokens)
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()

            val newTokens = authService.refreshToken(request.refreshToken)

            call.respond(newTokens)
        }

        authenticate {
            post("/logout") {
                val request = call.receive<LogoutRequest>()
                val principal = call.requireRequester()

                authService.logout(principal.username, request.refreshToken)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}