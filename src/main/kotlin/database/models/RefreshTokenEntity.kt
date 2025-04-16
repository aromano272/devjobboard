package com.andreromano.devjobboard.database.models

import java.time.Instant

data class RefreshTokenEntity(
    val id: Int,
    val user_id: Int,
    val token: String,
    val expires_at: Instant,
    val created_at: Instant,
)
