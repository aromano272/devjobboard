package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.BadRequestException
import com.andreromano.devjobboard.models.JobListing
import com.andreromano.devjobboard.models.requester
import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.routes.models.GetJobListingsRequest
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest
import com.andreromano.devjobboard.routes.models.toGetJobListingsRequest
import com.andreromano.devjobboard.service.JobService
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.jobRoutes(
    jobService: JobService,
) {
    route("/jobs") {
        authenticate(optional = true) {
            get({
                description = "Get job listings"
                request {
                    body<GetJobListingsRequest>()
                }
                response {
                    HttpStatusCode.OK to {
                        body<List<JobListing>>()
                    }
                }
            }) {
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

            post("/favorite/{id}") {
                val requester = call.requireRequester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                jobService.favorite(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/favorite/{id}") {
                val requester = call.requireRequester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                jobService.unfavorite(requester, jobId)
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