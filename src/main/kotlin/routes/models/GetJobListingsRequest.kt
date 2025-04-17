package com.andreromano.devjobboard.routes.models

import com.andreromano.devjobboard.models.*
import io.ktor.server.application.*
import kotlinx.serialization.Serializable

@Serializable
data class GetJobListingsRequest(
    val query: String? = null,
    val datePosted: DatePostedFilter? = null,
    val experienceLevel: List<ExperienceLevel> = emptyList(),
    val company: List<String> = emptyList(),
    val remote: List<RemoteOption> = emptyList(),
    val jobType: List<JobType> = emptyList(),
    val location: List<String> = emptyList(),
    val minSalary: Cents? = null,
    val maxSalary: Cents? = null,

    val page: Int = 0,
    val size: Int = 20,
)

fun ApplicationCall.toGetJobListingsRequest(): GetJobListingsRequest = request.queryParameters.let { params ->
    // Validate page and limit
    val page = params["page"]?.toIntOrNull() ?: 0
    val limit = params["limit"]?.toIntOrNull() ?: 20

    if (page < 0) throw BadRequestException("Page must be greater than 0")
    if (limit <= 0 || limit > 100) throw BadRequestException("Limit must be between 1 and 100")

    val datePosted = params.getAndValidateEnum<DatePostedFilter>("datePosted")
    val experienceLevel = params.getAllAndValidateEnum<ExperienceLevel>("experienceLevel")
    val remote = params.getAllAndValidateEnum<RemoteOption>("remote")
    val jobType = params.getAllAndValidateEnum<JobType>("jobType")

    val minSalary = params["minSalary"]?.toIntOrNull()
    val maxSalary = params["maxSalary"]?.toIntOrNull()

    if (minSalary != null && maxSalary != null && minSalary > maxSalary) {
        throw BadRequestException("minSalary cannot be greater than maxSalary")
    }

    return GetJobListingsRequest(
        page = page,
        size = limit,
        datePosted = datePosted,
        experienceLevel = experienceLevel,
        company = params.getAll("company") ?: emptyList(),
        remote = remote,
        jobType = jobType,
        location = params.getAll("location") ?: emptyList(),
        query = params["query"],
        minSalary = minSalary,
        maxSalary = maxSalary,
    )
}

enum class DatePostedFilter {
    ANYTIME, PAST_WEEK, PAST_24H, PAST_MONTH
}
