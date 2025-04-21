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
import com.andreromano.devjobboard.service.templates.JobApplicationApproved
import com.andreromano.devjobboard.service.templates.JobApplicationRejected
import com.andreromano.devjobboard.service.templates.NewJobApplicationReceivedNotificationForApplicant
import com.andreromano.devjobboard.service.templates.NewJobApplicationReceivedNotificationForRecruiter
import io.ktor.util.logging.Logger
import io.ktor.util.logging.error
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface JobApplicationService {
    suspend fun create(requester: Requester, jobId: Int)
    suspend fun approve(requester: Requester, jobApplicationId: Int)
    suspend fun reject(requester: Requester, jobApplicationId: Int)
    suspend fun getAllForRequester(requester: Requester, state: JobApplicationState?): List<JobApplication>
    suspend fun getAllByJobId(requester: Requester, jobId: Int, state: JobApplicationState?): List<JobApplication>
}

class DefaultJobApplicationService(
    private val logger: Logger,
    private val jobDao: JobDao,
    private val jobApplicationDao: JobApplicationDao,
    private val jobFavoriteDao: JobFavoriteDao,
    private val userDao: UserDao,
    private val emailService: EmailService,
) : JobApplicationService {
    override suspend fun create(requester: Requester, jobId: Int) {
        if (requester.role != UserRole.USER) throw ForbiddenException("Admins can't apply for jobs")
        val job = jobDao.getById(jobId) ?: throw NotFoundException("Job not found")
        val jobApplications = jobApplicationDao.getAll(requester.userId, jobId)
        if (jobApplications.any { it.state == JobApplicationStateEntity.PENDING })
            throw ConflictException("Job already applied")

        jobApplicationDao.insert(requester.userId, jobId, JobApplicationStateEntity.PENDING)

        val adminUser = userDao.findById(job.createdByUserId)
        val nowFormatted = DateTimeFormatter.ISO_LOCAL_DATE
            .withZone(ZoneId.of("UTC"))
            .format(Instant.now())
        val templateRecruiter = NewJobApplicationReceivedNotificationForRecruiter(
            recruiterName = adminUser?.username.orEmpty(),
            applicantName = requester.username,
            jobTitle = job.title,
            applicationDate = nowFormatted,
            jobLink = "TODO",
            applicantEmail = "TODO",
            resumeLink = "TODO"
        )
        val templateApplicant = NewJobApplicationReceivedNotificationForApplicant(
            applicantName = requester.username,
            jobTitle = job.title,
            company = job.company,
            applicationDate = nowFormatted,
            jobLink = "TODO",
            companyLogoUrl = "TODO"
        )
        try {
            emailService.sendEmail("some@email.com", "Test", templateRecruiter)
        } catch (ex: Exception) {
            logger.error(ex)
        }
        try {
            emailService.sendEmail("some@email.com", "Test", templateApplicant)
        } catch (ex: Exception) {
            logger.error(ex)
        }
    }

    override suspend fun approve(requester: Requester, jobApplicationId: Int) {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("Only admins can approve applications")
        val jobApplication = jobApplicationDao.getById(jobApplicationId) ?: throw NotFoundException("Job application not found")
        val job = jobDao.getById(jobApplication.jobId) ?: throw NotFoundException("Job not found")

        val updated = jobApplicationDao.updateState(jobApplicationId, JobApplicationStateEntity.APPROVED)
        if (updated == 0) return

        val template = JobApplicationApproved(
            applicantName = requester.username,
            jobTitle = job.title,
            company = job.company,
            approvalDate = DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneId.of("UTC"))
                .format(Instant.now()),
            nextStepInfo = "You'll be contacted in the next few days",
            jobLink = "TODO",
            companyLogoUrl = "TODO",
        )
        try {
            emailService.sendEmail("some@email.com", "Test", template)
        } catch (ex: Exception) {
            logger.error(ex)
        }
    }

    override suspend fun reject(requester: Requester, jobApplicationId: Int) {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("Only admins can approve applications")
        val jobApplication = jobApplicationDao.getById(jobApplicationId) ?: throw NotFoundException("Job application not found")
        val job = jobDao.getById(jobApplication.jobId) ?: throw NotFoundException("Job not found")

        val updated = jobApplicationDao.updateState(jobApplicationId, JobApplicationStateEntity.REJECTED)
        if (updated == 0) return

        val template = JobApplicationRejected(
            applicantName = requester.username,
            jobTitle = job.title,
            company = job.company,
            rejectionDate = DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneId.of("UTC"))
                .format(Instant.now()),
            jobLink = "TODO",
            companyLogoUrl = "TODO",
        )
        try {
            emailService.sendEmail("some@email.com", "Test", template)
        } catch (ex: Exception) {
            logger.error(ex)
        }
    }

    override suspend fun getAllForRequester(requester: Requester, state: JobApplicationState?): List<JobApplication> {
        if (requester.role != UserRole.USER) throw ForbiddenException("Admins can't apply for jobs")
        val applications = jobApplicationDao.getAll(requester.userId, state = state?.toEntity())

        val user = userDao.findById(requester.userId)?.toDomain() ?: throw NotFoundException("user not found")
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
                    user = user,
                )
            }
        }
    }

    override suspend fun getAllByJobId(
        requester: Requester,
        jobId: Int,
        state: JobApplicationState?
    ): List<JobApplication> {
        if (requester.role != UserRole.ADMIN) throw ForbiddenException("Only admins can access this")
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