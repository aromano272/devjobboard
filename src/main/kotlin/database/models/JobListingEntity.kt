package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.models.Cents
import com.andreromano.devjobboard.models.JobListing
import java.time.Instant

data class JobListingEntity(
    val id: Int,
    val title: String,
    val experience: ExperienceLevelEntity,
    val company: String,
    val remote: RemoteOptionEntity,
    val type: JobTypeEntity,
    val location: String,
    val minSalary: Cents?,
    val maxSalary: Cents?,
    val createdAt: Instant,
    val createdByUserId: Int,
)

fun JobListingEntity.toDomain(): JobListing = JobListing(
    id = id,
    title = title,
    experience = experience.toDomain(),
    company = company,
    remote = remote.toDomain(),
    type = type.toDomain(),
    location = location,
    minSalary = minSalary,
    maxSalary = maxSalary,
    createdAt = createdAt.toEpochMilli(),
    saved = false,
    applied = false,
)

