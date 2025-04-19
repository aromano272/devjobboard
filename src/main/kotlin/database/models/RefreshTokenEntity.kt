package com.andreromano.devjobboard.database.models

import java.time.Instant

data class RefreshTokenEntity(
    val id: Int,
    val userId: Int,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant,
)
