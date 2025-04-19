package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.JobApplicationDao
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.JobFavoriteDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.database.models.JobApplicationStateEntity
import com.andreromano.devjobboard.database.models.toDomain
import com.andreromano.devjobboard.database.models.toEntity
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.ForbiddenException
import com.andreromano.devjobboard.models.JobApplication
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.Requester
import com.andreromano.devjobboard.models.UserRole
import com.andreromano.devjobboard.models.toUser
import io.ktor.util.logging.Logger
import io.ktor.util.logging.error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface JobApplicationService {
    suspend fun create(requester: Requester, jobId: Int)
    fun getAllForRequester(requester: Requester, state: JobApplicationState?): List<JobApplication>
    fun getAllByJobId(requester: Requester, jobId: Int, state: JobApplicationState?): List<JobApplication>
}

class DefaultJobApplicationService(
    private val logger: Logger,
    private val jobDao: JobDao,
    private val jobApplicationDao: JobApplicationDao,
    private val jobFavoriteDao: JobFavoriteDao,
    private val userDao: UserDao,
    private val emailService: EmailService,
) : JobApplicationService {
    override suspend fun create(requester: Requester, jobId: Int) = withContext(Dispatchers.IO) {
        if (requester.role != UserRole.USER) throw ForbiddenException("Admins can't apply for jobs")
        val job = jobDao.getById(jobId) ?: throw NotFoundException("Job not found")
        val jobApplications = jobApplicationDao.getAll(requester.userId, jobId)
        if (jobApplications.any { it.state == JobApplicationStateEntity.PENDING })
            throw ConflictException("Job already applied")

        jobApplicationDao.insert(requester.userId, jobId, JobApplicationStateEntity.PENDING)

        try {
            emailService.sendEmail("andre.romano272@hotmail.com", "Test", "<h1>Test</h1>")
        } catch (ex: Exception) {
            logger.error(ex)
        }
    }

    override fun getAllForRequester(requester: Requester, state: JobApplicationState?): List<JobApplication> {
        if (requester.role != UserRole.USER) throw ForbiddenException("Admins can't apply for jobs")
        val applications = jobApplicationDao.getAll(requester.userId, state = state?.toEntity())

        val userFavoriteJobIds = jobFavoriteDao.getAllByUserId(requester.userId)
            .map { it.jobId }
            .toSet()

        val jobs = jobDao.getAllByIds(applications.map { it.jobId })
            .map { it.toDomain(favorite = userFavoriteJobIds.contains(it.id), applied = true) }
            .associateBy { it.id }

        return applications.mapNotNull { application ->
            jobs[application.jobId]?.let { job ->
                application.toDomain(
                    job = job,
                    user = requester.toUser(),
                )
            }
        }
    }

    override fun getAllByJobId(
        requester: Requester,
        jobId: Int,
        state: JobApplicationState?
    ): List<JobApplication> {
        if (requester.role != UserRole.USER) throw ForbiddenException("Only admins can access this")
        val job = jobDao.getById(jobId)?.toDomain(favorite = false, applied = false) ?: throw NotFoundException("Job not found")
        val applications = jobApplicationDao.getAll(jobId = jobId, state = state?.toEntity())
        val users = userDao.getAllByIds(applications.map { it.userId })
            .map { it.toDomain() }
            .associateBy { it.id }

        return applications.mapNotNull { application ->
            users[application.userId]?.let { user ->
                application.toDomain(job, user)
            }
        }
    }
}

enum class JobApplicationState {
    PENDING,
    APPROVED,
    REJECTED,
}