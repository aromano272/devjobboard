package com.andreromano.devjobboard.database.models

data class JobFavoriteEntity(
    val userId: Int,
    val jobId: Int,
)