package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobListingEntity
import com.andreromano.devjobboard.database.models.JobListingInsert
import com.andreromano.devjobboard.models.ExperienceLevel
import com.andreromano.devjobboard.models.JobType
import com.andreromano.devjobboard.models.RemoteOption
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.RegisterKotlinMapper
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.jetbrains.annotations.Blocking
import java.time.Instant

@RegisterKotlinMapper(JobListingEntity::class)
interface JobDb {

    @Blocking
    @SqlQuery("SELECT * FROM job_listings")
    fun getAll(): List<JobListingEntity>

    @Blocking
    @SqlQuery("SELECT * FROM job_listings WHERE id = ANY(:ids)")
    fun getAllByIds(@Bind("ids") ids: List<Int>): List<JobListingEntity>

    @Blocking
    @SqlQuery("SELECT * FROM job_listings WHERE id = :id")
    fun getById(@Bind("id") id: Int): JobListingEntity?

    @Blocking
    @SqlUpdate(
        """
        INSERT INTO job_listings (title, experience, company, remote, type, location, min_salary, max_salary, created_by_user_id)
        VALUES (:title, :experience, :company, :remote, :type, :location, :minSalary, :maxSalary, :createdByUserId)
    """
    )
    @GetGeneratedKeys
    fun insert(@BindBean job: JobListingInsert): Int

    @Blocking
    @SqlUpdate(
        """
        UPDATE job_listings
        SET title = :title, experience = :experience, company = :company, remote = :remote, type = :type, 
            location = :location, min_salary = :minSalary, max_salary = :maxSalary, created_at = :createdAt
        WHERE id = :id
    """
    )
    fun update(@BindBean job: JobListingEntity): Int

    @Blocking
    @SqlUpdate("DELETE FROM job_listings WHERE id = :id")
    fun delete(@Bind("id") id: Int): Int

    @Blocking
    @SqlQuery(
        """
        SELECT * FROM job_listings
        WHERE (:query IS NULL OR title ILIKE '%' || :query || '%')
        AND (:datePosted::timestamp IS NULL OR created_at >= :datePosted)
        AND (CARDINALITY(:experience) = 0 OR experience = ANY(:experience))
        AND (CARDINALITY(:company) = 0 OR company = ANY(:company))
        AND (CARDINALITY(:remote) = 0 OR remote = ANY(:remote))
        AND (CARDINALITY(:jobType) = 0 OR type = ANY(:jobType))
        AND (CARDINALITY(:location) = 0 OR location = ANY(:location))
        AND (:minSalary IS NULL OR min_salary >= :minSalary)
        AND (:maxSalary IS NULL OR max_salary <= :maxSalary)
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getFiltered(
        @Bind("query") query: String?,
        @Bind("datePosted") createdAt: Instant?,
        @Bind("experience") experience: List<ExperienceLevel>,
        @Bind("company") company: List<String>,
        @Bind("remote") remote: List<RemoteOption>,
        @Bind("jobType") jobType: List<JobType>,
        @Bind("location") location: List<String>,
        @Bind("minSalary") minSalary: Int?,
        @Bind("maxSalary") maxSalary: Int?,
        @Bind("limit") limit: Int,
        @Bind("offset") offset: Int
    ): List<JobListingEntity>
}
