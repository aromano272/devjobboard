package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.JobListingInsert
import com.andreromano.devjobboard.models.JobListing
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.kotlin.RegisterKotlinMapper
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@RegisterKotlinMapper(JobListing::class)
interface JobDao {

    @SqlQuery("SELECT * FROM job_listings")
    fun getAll(): List<JobListing>

    @SqlQuery("SELECT * FROM job_listings WHERE id = :id")
    fun getById(@Bind("id") id: Int): JobListing?

    @SqlUpdate(
        """
        INSERT INTO job_listings (title, company, location, remote, salary)
        VALUES (:title, :company, :location, :remote, :salary)
    """
    )
    @GetGeneratedKeys
    fun insert(@BindBean job: JobListingInsert): Int

    @SqlUpdate(
        """
        UPDATE job_listings
        SET title = :title, company = :company, location = :location, remote = :remote, salary = :salary
        WHERE id = :id
    """
    )
    fun update(@BindBean job: JobListing): Int

    @SqlUpdate("DELETE FROM job_listings WHERE id = :id")
    fun delete(@Bind("id") id: Int): Int
}