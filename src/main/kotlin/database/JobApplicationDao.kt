package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobApplicationEntity
import com.andreromano.devjobboard.database.models.JobApplicationStateEntity
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.kotlin.RegisterKotlinMapper
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.jetbrains.annotations.Blocking

@RegisterKotlinMapper(JobApplicationEntity::class)
interface JobApplicationDao {

    @Blocking
    @SqlQuery("SELECT * FROM job_applications WHERE id = :id")
    fun getById(@Bind("id") id: Int): JobApplicationEntity?

    @Blocking
    @SqlQuery("SELECT * FROM job_applications WHERE user_id = :userId")
    fun getAllByUserId(@Bind("userId") userId: Int): List<JobApplicationEntity>

    @Blocking
    @SqlQuery(
        """
            SELECT * FROM job_applications 
            WHERE (:userId IS NULL OR user_id = :userId)
            AND (:jobId IS NULL OR job_id = :jobId)
            AND (:state IS NULL OR state = :state)
        """
    )
    fun getAll(
        @Bind("userId") userId: Int? = null,
        @Bind("jobId") jobId: Int? = null,
        @Bind("state") state: JobApplicationStateEntity? = null,
    ): List<JobApplicationEntity>

    @Blocking
    @SqlUpdate(
        """
        INSERT INTO job_applications (user_id, job_id, state)
        VALUES (:userId, :jobId, :state)
    """
    )
    @GetGeneratedKeys
    fun insert(
        @Bind("userId") userId: Int,
        @Bind("jobId") jobId: Int,
        @Bind("state") state: JobApplicationStateEntity,
    ): Int

    @Blocking
    @SqlUpdate("DELETE FROM job_applications WHERE id = :id")
    fun delete(@Bind("id") id: Int): Int

}