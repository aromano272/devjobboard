package com.andreromano.devjobboard.database.models

data class UserEntity(
    val id: Int,
    val username: String,
    val is_admin: Boolean,
    val password_hash: String,
)
