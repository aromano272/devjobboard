package com.andreromano.devjobboard.models

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Requester(
    val userId: Int,
    val username: String,
    val role: UserRole,
)

private fun JWTPrincipal.asRequester(): Requester = Requester(
    userId = getClaim("userId", Int::class)
        ?: throw UnauthorizedException("userId missing from token"),
    username = getClaim("username", String::class)
        ?: throw UnauthorizedException("username missing from token"),
    role = UserRole.fromJwtClaim(getClaim("role", String::class))
        ?: throw UnauthorizedException("role missing from token"),
)

fun RoutingCall.requireRequester(): Requester =  principal<JWTPrincipal>()?.asRequester()
    ?: throw UnauthorizedException("missing jwt")
fun RoutingCall.requester(): Requester? =  principal<JWTPrincipal>()?.asRequester()