package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.JobApplicationDao
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.JobFavoriteDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.database.models.ExperienceLevelEntity
import com.andreromano.devjobboard.database.models.JobApplicationEntity
import com.andreromano.devjobboard.database.models.JobApplicationStateEntity
import com.andreromano.devjobboard.database.models.JobListingEntity
import com.andreromano.devjobboard.database.models.JobTypeEntity
import com.andreromano.devjobboard.database.models.RemoteOptionEntity
import com.andreromano.devjobboard.database.models.UserEntity
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.ForbiddenException
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.Requester
import com.andreromano.devjobboard.models.UserRole
import io.ktor.util.logging.Logger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class JobApplicationServiceTest {
    private val jobDao: JobDao = mockk()
    private val jobApplicationDao: JobApplicationDao = mockk()
    private val jobFavoriteDao: JobFavoriteDao = mockk()
    private val userDao: UserDao = mockk()
    private val emailService: EmailService = mockk()
    private val logger: Logger = mockk()
    private val jobApplicationService = DefaultJobApplicationService(
        logger = logger,
        jobDao = jobDao,
        jobApplicationDao = jobApplicationDao,
        jobFavoriteDao = jobFavoriteDao,
        userDao = userDao,
        emailService = emailService
    )

    @Test
    fun `create should throw ForbiddenException when user is not regular user`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobId = 1

        assertFailsWith<ForbiddenException> {
            jobApplicationService.create(requester, jobId)
        }
    }

    @Test
    fun `create should throw NotFoundException when job does not exist`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns null

        assertFailsWith<NotFoundException> {
            jobApplicationService.create(requester, jobId)
        }
    }

    @Test
    fun `create should throw ConflictException when job is already applied`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1
        val job = TEST_JOB_ENTITY

        coEvery { jobDao.getById(jobId) } returns job
        coEvery { jobApplicationDao.getAll(requester.userId, jobId) } returns listOf(
            JobApplicationEntity(
                id = 1,
                userId = requester.userId,
                jobId = jobId,
                state = JobApplicationStateEntity.PENDING,
                createdAt = Instant.now()
            )
        )

        assertFailsWith<ConflictException> {
            jobApplicationService.create(requester, jobId)
        }
    }

    @Test
    fun `create should insert application and send emails when successful`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1
        val job = TEST_JOB_ENTITY
        val adminUser = TEST_USER_ENTITY

        coEvery { jobDao.getById(jobId) } returns job
        coEvery { jobApplicationDao.getAll(requester.userId, jobId) } returns emptyList()
        coEvery { jobApplicationDao.insert(requester.userId, jobId, JobApplicationStateEntity.PENDING) } returns 1
        coEvery { userDao.findById(job.createdByUserId) } returns adminUser
        coEvery { emailService.sendEmail(any(), any(), any()) } returns Unit

        jobApplicationService.create(requester, jobId)

        coVerify {
            jobApplicationDao.insert(requester.userId, jobId, JobApplicationStateEntity.PENDING)
            emailService.sendEmail(any(), any(), any())
        }
    }

    @Test
    fun `approve should throw ForbiddenException when user is not admin`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobApplicationId = 1

        assertFailsWith<ForbiddenException> {
            jobApplicationService.approve(requester, jobApplicationId)
        }
    }

    @Test
    fun `approve should throw NotFoundException when application does not exist`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobApplicationId = 1

        coEvery { jobApplicationDao.getById(jobApplicationId) } returns null

        assertFailsWith<NotFoundException> {
            jobApplicationService.approve(requester, jobApplicationId)
        }
    }

    @Test
    fun `approve should update state and send email when successful`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobApplicationId = 1
        val jobApplication = TEST_JOB_APPLICATION_ENTITY
        val job = TEST_JOB_ENTITY

        coEvery { jobApplicationDao.getById(jobApplicationId) } returns jobApplication
        coEvery { jobDao.getById(jobApplication.jobId) } returns job
        coEvery { jobApplicationDao.updateState(jobApplicationId, JobApplicationStateEntity.APPROVED) } returns 1
        coEvery { emailService.sendEmail(any(), any(), any()) } returns Unit

        jobApplicationService.approve(requester, jobApplicationId)

        coVerify {
            jobApplicationDao.updateState(jobApplicationId, JobApplicationStateEntity.APPROVED)
            emailService.sendEmail(any(), any(), any())
        }
    }

    @Test
    fun `getAllForRequester should throw ForbiddenException when user is not regular user`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)

        assertFailsWith<ForbiddenException> {
            jobApplicationService.getAllForRequester(requester, null)
        }
    }

    @Test
    fun `getAllForRequester should return applications with job and user details`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobApplication = TEST_JOB_APPLICATION_ENTITY
        val job = TEST_JOB_ENTITY
        val user = TEST_USER_ENTITY

        coEvery { jobApplicationDao.getAll(requester.userId, state = null) } returns listOf(jobApplication)
        coEvery { userDao.findById(requester.userId) } returns user
        coEvery { jobFavoriteDao.getAllByUserId(requester.userId) } returns emptyList()
        coEvery { jobDao.getAllByIds(listOf(jobApplication.jobId)) } returns listOf(job)

        val result = jobApplicationService.getAllForRequester(requester, null)

        assertEquals(1, result.size)
        assertNotNull(result.first().job)
        assertNotNull(result.first().user)
    }

    @Test
    fun `getAllByJobId should throw ForbiddenException when user is not admin`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns TEST_JOB_ENTITY
        coEvery { jobApplicationDao.getAll(jobId = jobId, state = null) } returns emptyList()

        assertFailsWith<ForbiddenException> {
            jobApplicationService.getAllByJobId(requester, jobId, null)
        }
    }

    @Test
    fun `getAllByJobId should return applications with user details`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobId = 1
        val jobApplication = TEST_JOB_APPLICATION_ENTITY
        val job = TEST_JOB_ENTITY
        val user = TEST_USER_ENTITY

        coEvery { jobDao.getById(jobId) } returns job
        coEvery { jobApplicationDao.getAll(jobId = jobId, state = null) } returns listOf(jobApplication)
        coEvery { userDao.getAllByIds(listOf(jobApplication.userId)) } returns listOf(user)

        val result = jobApplicationService.getAllByJobId(requester, jobId, null)

        assertEquals(1, result.size)
        assertNotNull(result.first().job)
        assertNotNull(result.first().user)
    }

    companion object {
        private val TEST_JOB_ENTITY = JobListingEntity(
            id = 1,
            title = "Software Engineer",
            company = "Test Company",
            location = "Remote",
            remote = RemoteOptionEntity.REMOTE,
            type = JobTypeEntity.FULL_TIME,
            experience = ExperienceLevelEntity.SENIOR,
            minSalary = 100000,
            maxSalary = 200000,
            createdAt = Instant.now(),
            createdByUserId = 2
        )

        private val TEST_USER_ENTITY = UserEntity(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "hashed_password",
            isAdmin = false,
        )

        private val TEST_JOB_APPLICATION_ENTITY = JobApplicationEntity(
            id = 1,
            userId = 1,
            jobId = 1,
            state = JobApplicationStateEntity.PENDING,
            createdAt = Instant.now()
        )
    }
} 