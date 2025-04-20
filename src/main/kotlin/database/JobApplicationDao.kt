package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobApplicationEntity
import com.andreromano.devjobboard.database.models.JobApplicationStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface JobApplicationDao {

    suspend fun getById(id: Int): JobApplicationEntity?

    suspend fun getAllByUserId(userId: Int): List<JobApplicationEntity>

    suspend fun getAll(
        userId: Int? = null,
        jobId: Int? = null,
        state: JobApplicationStateEntity? = null,
    ): List<JobApplicationEntity>

    suspend fun insert(
        userId: Int,
        jobId: Int,
        state: JobApplicationStateEntity,
    ): Int

    suspend fun updateState(id: Int, state: JobApplicationStateEntity): Int

    suspend fun delete(id: Int): Int

}

class DefaultJobApplicationDao(
    private val db: JobApplicationDb
) : JobApplicationDao {
    override suspend fun getById(id: Int): JobApplicationEntity? = withContext(Dispatchers.IO) {
        db.getById(id)
    }

    override suspend fun getAllByUserId(userId: Int): List<JobApplicationEntity> = withContext(Dispatchers.IO) {
        db.getAllByUserId(userId)
    }

    override suspend fun getAll(
        userId: Int?,
        jobId: Int?,
        state: JobApplicationStateEntity?
    ): List<JobApplicationEntity> = withContext(Dispatchers.IO) {
        db.getAll(userId, jobId, state)
    }

    override suspend fun insert(
        userId: Int,
        jobId: Int,
        state: JobApplicationStateEntity
    ): Int = withContext(Dispatchers.IO) {
        db.insert(userId, jobId, state)
    }

    override suspend fun updateState(id: Int, state: JobApplicationStateEntity): Int = withContext(Dispatchers.IO) {
        db.updateState(id, state)
    }

    override suspend fun delete(id: Int): Int = withContext(Dispatchers.IO) {
        db.delete(id)
    }
}