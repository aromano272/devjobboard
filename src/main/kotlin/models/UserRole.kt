package com.andreromano.devjobboard.models

enum class UserRole {
    USER,
    ADMIN;

    fun asJwtClaim(): String = name.lowercase()

    companion object {
        fun fromJwtClaim(role: String?): UserRole? = role?.let { UserRole.valueOf(it) }
    }
}
