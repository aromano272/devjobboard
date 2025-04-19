package com.andreromano.devjobboard.routes.models

import com.andreromano.devjobboard.models.BadRequestException
import io.ktor.http.*

inline fun <reified T : Enum<T>> Parameters.getAndValidateEnum(name: String): T? =
    get(name)?.let {
        enumValueOfOrThrowBadRequest<T>(it, name)
    }

inline fun <reified T : Enum<T>> Parameters.getAllAndValidateEnum(name: String): List<T> =
    getAll(name)?.map {
        enumValueOfOrThrowBadRequest(it, name)
    } ?: emptyList()

inline fun <reified T : Enum<T>> enumValueOfOrThrowBadRequest(name: String, field: String): T = try {
    enumValueOf<T>(name.uppercase())
} catch (ex: Exception) {
    throw BadRequestException("Invalid $field value, expected: ${T::class.java.enumConstants.toList()}, got: ${name.uppercase()}")
}

