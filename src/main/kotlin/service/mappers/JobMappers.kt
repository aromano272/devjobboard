package com.andreromano.devjobboard.service.mappers

import com.andreromano.devjobboard.database.models.JobListingInsert
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest

fun JobListingInsertRequest.toEntityInsert(userId: Int): JobListingInsert = JobListingInsert(
    title = title,
    experience = experience,
    company = company,
    remote = remote,
    type = type,
    location = location,
    minSalary = minSalary,
    maxSalary = maxSalary,
    createdByUserId = userId
)