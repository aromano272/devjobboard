package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.service.AuthService
import io.github.smiley4.ktoropenapi.post
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
        post("/register", {
            description = "Create a new user account and return authentication tokens"
            request {
                body<RegisterRequest> {
                    description = "User registration details"
                    required = true
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "Successfully registered"
                }
                HttpStatusCode.BadRequest to {
                    description = "Invalid registration data"
                }
            }
        }) {
            val request = call.receive<RegisterRequest>()

            val tokens = authService.registerAndLogin(
                request.username,
                request.email,
                request.password,
                request.isAdmin,
            )
            call.respond(tokens)
        }

        post("/login", {
            summary = "Login user"
            description = "Authenticate user and return tokens"
            request {
                body<LoginRequest> {
                    description = "Login credentials"
                    required = true
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "Successfully logged in"
                }
                HttpStatusCode.Unauthorized to {
                    description = "Invalid credentials"
                }
            }
        }) {
            val user = call.receive<LoginRequest>()
            val tokens = authService.login(user.username, user.password)
            call.respond(tokens)
        }

        post("/refresh", {
            summary = "Refresh token"
            description = "Get new access token using refresh token"
            request {
                body<RefreshTokenRequest> {
                    description = "Refresh token"
                    required = true
                }
            }
            response {
                HttpStatusCode.OK to {
                    description = "New tokens generated"
                }
                HttpStatusCode.Unauthorized to {
                    description = "Invalid refresh token"
                }
            }
        }) {
            val request = call.receive<RefreshTokenRequest>()

            val newTokens = authService.refreshToken(request.refreshToken)

            call.respond(newTokens)
        }

        authenticate {
            post("/logout", {
                summary = "Logout user"
                description = "Invalidate refresh token"
                request {
                    body<LogoutRequest> {
                        description = "Refresh token to invalidate"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Successfully logged out"
                    }
                }
            }) {
                val request = call.receive<LogoutRequest>()
                val principal = call.requireRequester()

                authService.logout(principal.username, request.refreshToken)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}