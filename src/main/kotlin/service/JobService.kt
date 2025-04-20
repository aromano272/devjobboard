package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.JobApplicationDao
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.JobFavoriteDao
import com.andreromano.devjobboard.database.models.JobApplicationStateEntity
import com.andreromano.devjobboard.database.models.toDomain
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.ForbiddenException
import com.andreromano.devjobboard.models.JobListing
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.Requester
import com.andreromano.devjobboard.models.UserRole
import com.andreromano.devjobboard.routes.models.DatePostedFilter
import com.andreromano.devjobboard.routes.models.GetJobListingsRequest
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest
import com.andreromano.devjobboard.service.mappers.toEntityInsert
import java.time.Instant
import java.time.temporal.ChronoUnit

interface JobService {
    suspend fun insert(requester: Requester, request: JobListingInsertRequest)
    suspend fun favorite(requester: Requester, jobId: Int)
    suspend fun unfavorite(requester: Requester, jobId: Int)
    suspend fun getAll(requester: Requester?, request: GetJobListingsRequest): List<JobListing>
    suspend fun getById(requester: Requester?, id: Int): JobListing?
    suspend fun delete(requester: Requester, id: Int)
}

class DefaultJobService(
    private val jobDao: JobDao,
    private val jobFavoriteDao: JobFavoriteDao,
    private val jobApplicationDao: JobApplicationDao,
) : JobService {
    override suspend fun insert(requester: Requester, request: JobListingInsertRequest) {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("User has no permission to create a new job")
        jobDao.insert(request.toEntityInsert(requester.userId))
    }

    override suspend fun favorite(requester: Requester, jobId: Int) {
        if (requester.role != UserRole.USER) throw ForbiddenException("Admins can't favorite jobs")
        val job = jobDao.getById(jobId) ?: throw NotFoundException("Job not found")
        val inserted = jobFavoriteDao.insert(requester.userId, jobId)
        if (inserted == 0) throw ConflictException("Job already favorited")
    }

    override suspend fun unfavorite(requester: Requester, jobId: Int) {
        if (requester.role != UserRole.USER) throw ForbiddenException("Admins can't unfavorite jobs")
        val job = jobDao.getById(jobId) ?: throw NotFoundException("Job not found")
        val deleted = jobFavoriteDao.delete(requester.userId, jobId)
        if (deleted == 0) throw ConflictException("Job already unfavorited")
    }

    override suspend fun getAll(requester: Requester?, request: GetJobListingsRequest): List<JobListing> {
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

        val userFavoriteJobIds = requester?.userId?.let {
            jobFavoriteDao.getAllByUserId(it)
                .map { it.jobId }
                .toSet()
        }.orEmpty()
        val userApplicationJobIds = requester?.userId?.let {
            jobApplicationDao.getAllByUserId(it)
                .map { it.jobId }
                .toSet()
        }.orEmpty()

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
            it.toDomain(
                favorite = userFavoriteJobIds.contains(it.id),
                applied = userApplicationJobIds.contains(it.id),
            )
        }
    }

    override suspend fun getById(requester: Requester?, id: Int): JobListing? =
        jobDao.getById(id)?.toDomain(
            favorite = requester?.userId?.let {
                jobFavoriteDao.getById(it, id)
            } != null,
            applied = requester?.userId?.let {
                jobApplicationDao.getAll(it, id, JobApplicationStateEntity.PENDING).isNotEmpty()
            } ?: false,
        )

    override suspend fun delete(requester: Requester, id: Int) {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("User is not admin")
        val job = jobDao.getById(id) ?: throw NotFoundException("job not found")
        if (job.createdByUserId != requester.userId) throw ForbiddenException("User has no permission to delete this job")

        jobDao.delete(id)
    }
}