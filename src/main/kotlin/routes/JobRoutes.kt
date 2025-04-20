package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.BadRequestException
import com.andreromano.devjobboard.models.JobListing
import com.andreromano.devjobboard.models.requester
import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.routes.models.GetJobListingsRequest
import com.andreromano.devjobboard.routes.models.JobListingInsertRequest
import com.andreromano.devjobboard.routes.models.toGetJobListingsRequest
import com.andreromano.devjobboard.service.JobService
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.jobRoutes(
    jobService: JobService,
) {
    route("/jobs") {
        authenticate(optional = true) {
            get({
                description = "Get filtered job listings"
                request {
                    body<GetJobListingsRequest> {
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "List of job listings matching the criteria"
                        body<List<JobListing>>()
                    }
                }
            }) {
                val requester = call.requester()
                val request = call.toGetJobListingsRequest()
                val jobs = jobService.getAll(requester, request)
                call.respond(jobs)
            }

            get("/{id}", {
                description = "Get job listing by ID"
                request {
                    pathParameter<Int>("id") {
                        description = "ID of the job listing"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Job listing details"
                        body<JobListing>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Job listing not found"
                    }
                }
            }) {
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
            post("/insert", {
                description = "Insert a new job listing"
                request {
                    body<JobListingInsertRequest> {
                        description = "Job listing details to insert"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Job listing created successfully"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid request data"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val request = call.receive<JobListingInsertRequest>()
                jobService.insert(requester, request)
                call.respond(HttpStatusCode.OK)
            }

            post("/favorite/{id}", {
                description = "Favorite a job listing"
                request {
                    pathParameter<Int>("id") {
                        description = "ID of the job listing to favorite"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Job listing favorited successfully"
                    }
                    HttpStatusCode.NotFound to {
                        description = "Job listing not found"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                jobService.favorite(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/favorite/{id}", {
                description = "Remove job listing from favorites"
                request {
                    pathParameter<Int>("id") {
                        description = "ID of the job listing to unfavorite"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Job listing removed from favorites"
                    }
                    HttpStatusCode.NotFound to {
                        description = "Job listing not found"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                jobService.unfavorite(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }

            delete("/{id}", {
                description = "Delete a job listing"
                request {
                    pathParameter<Int>("id") {
                        description = "ID of the job listing to delete"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Job listing deleted successfully"
                    }
                    HttpStatusCode.NotFound to {
                        description = "Job listing not found"
                    }
                    HttpStatusCode.Forbidden to {
                        description = "Not authorized to delete this job listing"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobId = call.parameters["id"]?.toIntOrNull()
                    ?: throw BadRequestException("Invalid or missing job ID")
                jobService.delete(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}