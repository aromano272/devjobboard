package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.models.toDomain
import com.andreromano.devjobboard.models.*
import com.andreromano.devjobboard.routes.models.DatePostedFilter
import com.andreromano.devjobboard.routes.models.GetJobListingsRequest
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest
import com.andreromano.devjobboard.service.mappers.toEntityInsert
import java.time.Instant
import java.time.temporal.ChronoUnit

interface JobService {
    fun insert(requester: Requester, request: JobListingInsertRequest)
    fun getAll(requester: Requester?, request: GetJobListingsRequest): List<JobListing>
    fun getById(requester: Requester?, id: Int): JobListing?
    fun delete(requester: Requester, id: Int)
}

class DefaultJobService(
    private val jobDao: JobDao,
) : JobService {
    override fun insert(requester: Requester, request: JobListingInsertRequest) {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("User has no permission to create a new job")
        jobDao.insert(request.toEntityInsert(requester.userId))
    }

    override fun getAll(requester: Requester?, request: GetJobListingsRequest): List<JobListing> {
        val createdAt = when (request.datePosted) {
            DatePostedFilter.ANYTIME -> null
            DatePostedFilter.PAST_24H -> Instant.now().minus(24, ChronoUnit.HOURS)
            DatePostedFilter.PAST_WEEK -> Instant.now()
                .minus(7, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS)
            DatePostedFilter.PAST_MONTH -> Instant.now()
                .minus(1, ChronoUnit.MONTHS)
                .truncatedTo(ChronoUnit.DAYS)
            null -> null
        }
        return jobDao.getFiltered(
            query = request.query,
            createdAt = createdAt,
            experience = request.experience,
            company = request.company,
            remote = request.remote,
            jobType = request.jobType,
            location = request.location,
            minSalary = request.minSalary,
            maxSalary = request.maxSalary,
            limit = request.size,
            offset = request.page * request.size
        ).map {
            it.toDomain()
        }
    }

    override fun getById(requester: Requester?, id: Int): JobListing? =
        jobDao.getById(id)?.toDomain()

    override fun delete(requester: Requester, id: Int) {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("User is not admin")
        val job = jobDao.getById(id) ?: throw NotFoundException("job not found")
        if (job.createdByUserId != requester.userId) throw ForbiddenException("User has no permission to delete this job")

        jobDao.delete(id)
    }
}