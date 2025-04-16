package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.repository.JobRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.jobRoutes(
    jobRepository: JobRepository,
) {
    route("/jobs") {
        get {
            val jobs = jobRepository.getAll()
            call.respond(jobs.dataOrNull().orEmpty())
        }
    }
    route("/jobs/insert") {
        get {
            jobRepository.insert()
            call.respond(HttpStatusCode.OK)
        }
    }
}