package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.JobApplicationDao
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.JobFavoriteDao
import com.andreromano.devjobboard.database.models.ExperienceLevelEntity
import com.andreromano.devjobboard.database.models.JobApplicationStateEntity
import com.andreromano.devjobboard.database.models.JobListingEntity
import com.andreromano.devjobboard.database.models.JobTypeEntity
import com.andreromano.devjobboard.database.models.RemoteOptionEntity
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.ExperienceLevel
import com.andreromano.devjobboard.models.ForbiddenException
import com.andreromano.devjobboard.models.JobType
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.RemoteOption
import com.andreromano.devjobboard.models.Requester
import com.andreromano.devjobboard.models.UserRole
import com.andreromano.devjobboard.routes.models.DatePostedFilter
import com.andreromano.devjobboard.routes.models.GetJobListingsRequest
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JobServiceTest {
    private val jobDao: JobDao = mockk()
    private val jobFavoriteDao: JobFavoriteDao = mockk()
    private val jobApplicationDao: JobApplicationDao = mockk()
    private val jobService: DefaultJobService = DefaultJobService(jobDao, jobFavoriteDao, jobApplicationDao)

    @Test
    fun `insert should throw ForbiddenException when user is not admin`(): Unit = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val request = JobListingInsertRequest(
            title = "Software Engineer",
            experience = ExperienceLevel.MID_LEVEL,
            company = "Test Company",
            remote = RemoteOption.REMOTE,
            type = JobType.FULL_TIME,
            location = "Remote",
            minSalary = 50000,
            maxSalary = 100000
        )

        assertFailsWith<ForbiddenException> {
            jobService.insert(requester, request)
        }
    }

    @Test
    fun `insert should call jobDao insert when user is admin`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val request = JobListingInsertRequest(
            title = "Software Engineer",
            experience = ExperienceLevel.MID_LEVEL,
            company = "Test Company",
            remote = RemoteOption.REMOTE,
            type = JobType.FULL_TIME,
            location = "Remote",
            minSalary = 50000,
            maxSalary = 100000
        )

        coEvery { jobDao.insert(any()) } returns 1

        jobService.insert(requester, request)

        coVerify { jobDao.insert(any()) }
    }

    @Test
    fun `favorite should throw ForbiddenException when user is not regular user`(): Unit = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns mockk()

        assertFailsWith<ForbiddenException> {
            jobService.favorite(requester, jobId)
        }
    }

    @Test
    fun `favorite should throw NotFoundException when job does not exist`(): Unit = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns null

        assertFailsWith<NotFoundException> {
            jobService.favorite(requester, jobId)
        }
    }

    @Test
    fun `favorite should throw ConflictException when job is already favorited`(): Unit = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns mockk()
        coEvery { jobFavoriteDao.insert(requester.userId, jobId) } returns 0

        assertFailsWith<ConflictException> {
            jobService.favorite(requester, jobId)
        }
    }

    @Test
    fun `getAll should return empty list when no jobs match filters`() = runTest {
        val request = GetJobListingsRequest(
            query = "nonexistent",
            datePosted = DatePostedFilter.PAST_24H,
            experience = listOf(ExperienceLevel.SENIOR),
            company = listOf("Test Company"),
            remote = listOf(RemoteOption.REMOTE),
            jobType = listOf(JobType.FULL_TIME),
            location = listOf("Remote"),
            minSalary = 100000,
            maxSalary = 200000,
            page = 0,
            size = 10
        )

        coEvery {
            jobDao.getFiltered(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns emptyList()
        coEvery { jobFavoriteDao.getAllByUserId(any()) } returns emptyList()
        coEvery { jobApplicationDao.getAllByUserId(any()) } returns emptyList()

        val result = jobService.getAll(null, request)

        assertEquals(emptyList(), result)
    }

    @Test
    fun `getAll should apply all filters correctly`() = runTest {
        val request = GetJobListingsRequest(
            query = "Software Engineer",
            datePosted = DatePostedFilter.PAST_24H,
            experience = listOf(ExperienceLevel.SENIOR),
            company = listOf("Test Company"),
            remote = listOf(RemoteOption.REMOTE),
            jobType = listOf(JobType.FULL_TIME),
            location = listOf("Remote"),
            minSalary = 100000,
            maxSalary = 200000,
            page = 0,
            size = 10
        )

        val jobEntity = TEST_JOB_ENTITY

        coEvery {
            jobDao.getFiltered(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(jobEntity)
        coEvery { jobFavoriteDao.getAllByUserId(any()) } returns emptyList()
        coEvery { jobApplicationDao.getAllByUserId(any()) } returns emptyList()

        val result = jobService.getAll(null, request)

        assertEquals(1, result.size)
        coVerify {
            jobDao.getFiltered(
                query = "Software Engineer",
                createdAt = any(),
                experience = listOf(ExperienceLevel.SENIOR),
                company = listOf("Test Company"),
                remote = listOf(RemoteOption.REMOTE),
                jobType = listOf(JobType.FULL_TIME),
                location = listOf("Remote"),
                minSalary = 100000,
                maxSalary = 200000,
                limit = 10,
                offset = 0
            )
        }
    }

    @Test
    fun `getAll should handle user favorites and applications`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val request = GetJobListingsRequest(
            page = 0,
            size = 10
        )

        val jobEntity = TEST_JOB_ENTITY

        coEvery {
            jobDao.getFiltered(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(jobEntity)
        coEvery { jobFavoriteDao.getAllByUserId(requester.userId) } returns listOf(mockk { coEvery { jobId } returns 1 })
        coEvery { jobApplicationDao.getAllByUserId(requester.userId) } returns listOf(mockk { coEvery { jobId } returns 1 })

        val result = jobService.getAll(requester, request)

        assertEquals(1, result.size)
        coVerify {
            jobFavoriteDao.getAllByUserId(requester.userId)
            jobApplicationDao.getAllByUserId(requester.userId)
        }
    }

    @Test
    fun `getById should return null when job does not exist`() = runTest {
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns null

        val result = jobService.getById(null, jobId)

        assertNull(result)
    }

    @Test
    fun `getById should return job with favorite and application status`() = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1
        val jobEntity = TEST_JOB_ENTITY

        coEvery { jobDao.getById(jobId) } returns jobEntity
        coEvery { jobFavoriteDao.getById(requester.userId, jobId) } returns mockk()
        coEvery { jobApplicationDao.getAll(requester.userId, jobId, JobApplicationStateEntity.PENDING) } returns listOf(
            mockk()
        )

        val result = jobService.getById(requester, jobId)

        assertNotNull(result)
        coVerify {
            jobFavoriteDao.getById(requester.userId, jobId)
            jobApplicationDao.getAll(requester.userId, jobId, JobApplicationStateEntity.PENDING)
        }
    }

    @Test
    fun `delete should throw ForbiddenException when user is not admin`(): Unit = runTest {
        val requester = Requester(1, "user", UserRole.USER)
        val jobId = 1

        assertFailsWith<ForbiddenException> {
            jobService.delete(requester, jobId)
        }
    }

    @Test
    fun `delete should throw NotFoundException when job does not exist`(): Unit = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobId = 1

        coEvery { jobDao.getById(jobId) } returns null

        assertFailsWith<NotFoundException> {
            jobService.delete(requester, jobId)
        }
    }

    @Test
    fun `delete should throw ForbiddenException when user is not the creator`(): Unit = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobId = 1
        val jobEntity = TEST_JOB_ENTITY.copy(createdByUserId = 2)

        coEvery { jobDao.getById(jobId) } returns jobEntity

        assertFailsWith<ForbiddenException> {
            jobService.delete(requester, jobId)
        }
    }

    @Test
    fun `delete should call jobDao delete when all conditions are met`() = runTest {
        val requester = Requester(1, "admin", UserRole.ADMIN)
        val jobId = 1
        val jobEntity = TEST_JOB_ENTITY.copy(createdByUserId = requester.userId)

        coEvery { jobDao.getById(jobId) } returns jobEntity
        coEvery { jobDao.delete(jobId) } returns 1

        jobService.delete(requester, jobId)

        coVerify { jobDao.delete(jobId) }
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
            createdByUserId = 1
        )

    }
} 