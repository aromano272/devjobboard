package com.andreromano.devjobboard.models

import kotlinx.serialization.Serializable

sealed class ApiException(message: String) : RuntimeException(message)

class NotFoundException(message: String) : ApiException(message)
class BadRequestException(message: String) : ApiException(message)
class UnauthorizedException(message: String) : ApiException(message)
class ConflictException(message: String) : ApiException(message)

@Serializable
data class ErrorResponse(
    val error: String,
)
