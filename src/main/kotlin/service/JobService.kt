package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.core.Outcome
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.models.JobListingInsert
import com.andreromano.devjobboard.models.JobListing

interface JobService {
    fun insert()
    fun getAll(): Outcome<List<JobListing>>
}

class DefaultJobService(
    private val jobDao: JobDao,
) : JobService {
    override fun insert() {
        jobDao.insert(
            JobListingInsert(
                title = "TODO()",
                company = "TODO()",
                location = "TODO()",
                remote = true,
                salary = 100
            )
        )
    }

    override fun getAll(): Outcome<List<JobListing>> = Outcome.Success(jobDao.getAll())
}