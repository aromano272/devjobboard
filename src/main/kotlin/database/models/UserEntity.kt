package com.andreromano.devjobboard.database.models

data class UserEntity(
    val id: Int,
    val username: String,
    val isAdmin: Boolean,
    val passwordHash: String,
)
