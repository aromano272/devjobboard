package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobFavoriteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface JobFavoriteDao {

    suspend fun getById(
        userId: Int,
        jobId: Int,
    ): JobFavoriteEntity?

    suspend fun getAllByUserId(userId: Int): List<JobFavoriteEntity>

    suspend fun insert(
        userId: Int,
        jobId: Int,
    ): Int

    suspend fun delete(userId: Int, jobId: Int): Int

}

class DefaultJobFavoriteDao(
    private val db: JobFavoriteDb,
) : JobFavoriteDao {
    override suspend fun getById(userId: Int, jobId: Int): JobFavoriteEntity? = withContext(Dispatchers.IO) {
        db.getById(userId, jobId)
    }

    override suspend fun getAllByUserId(userId: Int): List<JobFavoriteEntity> = withContext(Dispatchers.IO) {
        db.getAllByUserId(userId)
    }

    override suspend fun insert(userId: Int, jobId: Int): Int = withContext(Dispatchers.IO) {
        db.insert(userId, jobId)
    }

    override suspend fun delete(userId: Int, jobId: Int): Int = withContext(Dispatchers.IO) {
        db.delete(userId, jobId)
    }
}