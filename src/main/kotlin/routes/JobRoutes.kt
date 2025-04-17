package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.requester
import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest
import com.andreromano.devjobboard.routes.models.toGetJobListingsRequest
import com.andreromano.devjobboard.service.JobService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.jobRoutes(
    jobService: JobService,
) {
    route("/jobs") {
        authenticate(optional = true) {
            get {
                val requester = call.requester()
                val request = call.toGetJobListingsRequest()
                val jobs = jobService.getAll(requester, request)
                call.respond(jobs)
            }

            get("/{id}") {
                val requester = call.requester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                val job = jobService.getById(requester, jobId)
                if (job != null) {
                    call.respond(job)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Job not found")
                }
            }
        }

        authenticate {
            post("/insert") {
                val requester = call.requireRequester()
                val request = call.receive<JobListingInsertRequest>()
                jobService.insert(requester, request)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}") {
                val requester = call.requireRequester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                jobService.delete(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}