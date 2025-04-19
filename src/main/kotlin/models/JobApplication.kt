package com.andreromano.devjobboard.models

import kotlinx.serialization.Serializable

@Serializable
data class JobApplication(
    val job: JobListing,
    val user: User,
    val createdAt: Millis,
)