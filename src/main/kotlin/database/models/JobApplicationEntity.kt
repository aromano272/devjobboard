package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.models.JobApplication
import com.andreromano.devjobboard.models.JobListing
import com.andreromano.devjobboard.models.User
import java.time.Instant

data class JobApplicationEntity(
    val id: Int,
    val userId: Int,
    val jobId: Int,
    val state: JobApplicationStateEntity,
    val createdAt: Instant,
)

fun JobApplicationEntity.toDomain(job: JobListing, user: User): JobApplication = JobApplication(
    job = job,
    user = user,
    createdAt = createdAt.toEpochMilli()
)

