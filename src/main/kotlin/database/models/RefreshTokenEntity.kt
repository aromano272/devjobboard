package com.andreromano.devjobboard.database.models

import java.time.Instant
import java.util.UUID

data class RefreshTokenEntity(
    val id: UUID,
    val userId: Int,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant
)
