package com.andreromano.devjobboard

import com.andreromano.devjobboard.models.*
import com.andreromano.devjobboard.service.AuthService
import com.andreromano.devjobboard.service.JobService
import com.andreromano.devjobboard.routes.authRoutes
import com.andreromano.devjobboard.routes.jobRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            val (status, message) = when (cause) {
                is NotFoundException -> HttpStatusCode.NotFound to cause.message
                is UnauthorizedException -> HttpStatusCode.Unauthorized to cause.message
                is ConflictException -> HttpStatusCode.Conflict to cause.message
                is BadRequestException -> HttpStatusCode.BadRequest to cause.message
            }

            call.application.environment.log.warn("Handled error", cause)
            val error = ErrorResponse(message ?: "Unknown error")
            call.respond(status, error)
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)

            val error = ErrorResponse("Unexpected error")
            call.respond(HttpStatusCode.InternalServerError, error)
        }
    }
    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    val jobService: JobService by inject()
    val authService: AuthService by inject()

    routing {
        jobRoutes(jobService)
        authRoutes(authService)
    }
}
