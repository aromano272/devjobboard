package com.andreromano.devjobboard.models

import kotlinx.serialization.Serializable

@Serializable
data class JobListing(
    val id: Int,
    val title: String,
    val experience: ExperienceLevel,
    val company: String,
    val remote: RemoteOption,
    val type: JobType,
    val location: String,
    val minSalary: Cents?,
    val maxSalary: Cents?,
    val createdAt: Millis,

    // Authenticated only fields
    val saved: Boolean,
    val applied: Boolean,
)
