package com.andreromano.devjobboard.routes.models

import com.andreromano.devjobboard.models.Cents
import com.andreromano.devjobboard.models.ExperienceLevel
import com.andreromano.devjobboard.models.JobType
import com.andreromano.devjobboard.models.RemoteOption
import kotlinx.serialization.Serializable

@Serializable
data class JobListingInsertRequest(
    val title: String,
    val experience: ExperienceLevel,
    val company: String,
    val remote: RemoteOption,
    val type: JobType,
    val location: String,
    val minSalary: Cents?,
    val maxSalary: Cents?,
)
