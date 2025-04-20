package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobListingEntity
import com.andreromano.devjobboard.database.models.JobListingInsert
import com.andreromano.devjobboard.models.ExperienceLevel
import com.andreromano.devjobboard.models.JobType
import com.andreromano.devjobboard.models.RemoteOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

interface JobDao {

    suspend fun getAll(): List<JobListingEntity>

    suspend fun getAllByIds(ids: List<Int>): List<JobListingEntity>

    suspend fun getById(id: Int): JobListingEntity?

    suspend fun insert(job: JobListingInsert): Int

    suspend fun update(job: JobListingEntity): Int

    suspend fun delete(id: Int): Int

    suspend fun getFiltered(
        query: String?,
        createdAt: Instant?,
        experience: List<ExperienceLevel>,
        company: List<String>,
        remote: List<RemoteOption>,
        jobType: List<JobType>,
        location: List<String>,
        minSalary: Int?,
        maxSalary: Int?,
        limit: Int,
        offset: Int
    ): List<JobListingEntity>
}

class DefaultJobDao(
    private val db: JobDb
) : JobDao {
    override suspend fun getAll(): List<JobListingEntity> = withContext(Dispatchers.IO) {
       db.getAll()
    }

    override suspend fun getAllByIds(ids: List<Int>): List<JobListingEntity> = withContext(Dispatchers.IO) {
       db.getAllByIds(ids)
    }

    override suspend fun getById(id: Int): JobListingEntity? = withContext(Dispatchers.IO) {
       db.getById(id)
    }

    override suspend fun insert(job: JobListingInsert): Int = withContext(Dispatchers.IO) {
       db.insert(job)
    }

    override suspend fun update(job: JobListingEntity): Int = withContext(Dispatchers.IO) {
       db.update(job)
    }

    override suspend fun delete(id: Int): Int = withContext(Dispatchers.IO) {
       db.delete(id)
    }

    override suspend fun getFiltered(
        query: String?,
        createdAt: Instant?,
        experience: List<ExperienceLevel>,
        company: List<String>,
        remote: List<RemoteOption>,
        jobType: List<JobType>,
        location: List<String>,
        minSalary: Int?,
        maxSalary: Int?,
        limit: Int,
        offset: Int,
    ): List<JobListingEntity> = withContext(Dispatchers.IO) {
        db.getFiltered(
            query = query,
            createdAt = createdAt,
            experience = experience,
            company = company,
            remote = remote,
            jobType = jobType,
            location = location,
            minSalary = minSalary,
            maxSalary = maxSalary,
            limit = limit,
            offset = offset,
        )
    }
}
