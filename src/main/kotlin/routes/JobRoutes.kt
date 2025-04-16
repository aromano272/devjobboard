package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.service.JobService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.jobRoutes(
    jobService: JobService,
) {
    route("/jobs") {
        get {
            val jobs = jobService.getAll()
            call.respond(jobs.dataOrNull().orEmpty())
        }
    }
    route("/jobs/insert") {
        get {
            jobService.insert()
            call.respond(HttpStatusCode.OK)
        }
    }
}