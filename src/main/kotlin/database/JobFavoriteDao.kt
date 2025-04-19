package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobFavoriteEntity
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.kotlin.RegisterKotlinMapper
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@RegisterKotlinMapper(JobFavoriteEntity::class)
interface JobFavoriteDao {

    @SqlQuery("SELECT * FROM job_favorites WHERE user_id = :userId AND job_id = :jobId")
    fun getById(
        @Bind("userId") userId: Int,
        @Bind("jobId") jobId: Int,
    ): JobFavoriteEntity?

    @SqlQuery("SELECT * FROM job_favorites WHERE user_id = :userId")
    fun getAllByUserId(@Bind("userId") userId: Int): List<JobFavoriteEntity>

    @SqlUpdate(
        """
        INSERT INTO job_favorites (user_id, job_id)
        VALUES (:userId, :jobId)
        ON CONFLICT DO NOTHING
        """
    )
    fun insert(
        @Bind("userId") userId: Int,
        @Bind("jobId") jobId: Int,
    ): Int

    @SqlUpdate("DELETE FROM job_favorites WHERE user_id = :userId AND job_id = :jobId")
    fun delete(@Bind("userId") userId: Int, @Bind("jobId") jobId: Int): Int

}