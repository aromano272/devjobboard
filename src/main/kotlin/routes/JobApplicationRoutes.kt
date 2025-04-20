package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.BadRequestException
import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.routes.models.getAndValidateEnum
import com.andreromano.devjobboard.service.JobApplicationService
import com.andreromano.devjobboard.service.JobApplicationState
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.jobApplicationRoutes(
    jobApplicationService: JobApplicationService,
) {
    route("/jobapplications") {
        authenticate {
            post("/apply/{jobId}", {
                description = "Create a new job application for the specified job"
                request {
                    pathParameter<Int>("jobId") {
                        description = "ID of the job to apply for"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Application submitted successfully"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid job ID"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobId = call.parameters["jobId"]?.toIntOrNull() ?: throw BadRequestException("Missing job id")
                jobApplicationService.create(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }

            get({
                description = "Get all job applications for the authenticated user"
                request {
                    queryParameter<JobApplicationState>("state") {
                        description = "Filter applications by state"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "List of job applications"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val state = call.queryParameters.getAndValidateEnum<JobApplicationState>("state")
                val jobs = jobApplicationService.getAllForRequester(requester, state)
                call.respond(jobs)
            }

            get("/job/{jobId}", {
                description = "Get all applications for a specific job"
                request {
                    pathParameter<Int>("jobId") {
                        description = "ID of the job"
                        required = true
                    }
                    queryParameter<JobApplicationState>("state") {
                        description = "Filter applications by state"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "List of job applications"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid job ID"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobId = call.parameters["jobId"]?.toIntOrNull() ?: throw BadRequestException("Missing job id")
                val state = call.queryParameters.getAndValidateEnum<JobApplicationState>("state")
                val jobs = jobApplicationService.getAllByJobId(requester, jobId, state)
                call.respond(jobs)
            }

            post("/approve/{jobApplicationId}", {
                description = "Approve a pending job application"
                request {
                    pathParameter<Int>("jobApplicationId") {
                        description = "ID of the job application"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Application approved successfully"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid application ID"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobApplicationId = call.parameters["jobApplicationId"]?.toIntOrNull()
                    ?: throw BadRequestException("Missing job application id")
                jobApplicationService.approve(requester, jobApplicationId)
                call.respond(HttpStatusCode.OK)
            }

            post("/reject/{jobApplicationId}", {
                description = "Reject a pending job application"
                request {
                    pathParameter<Int>("jobApplicationId") {
                        description = "ID of the job application"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Application rejected successfully"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid application ID"
                    }
                }
            }) {
                val requester = call.requireRequester()
                val jobApplicationId = call.parameters["jobApplicationId"]?.toIntOrNull()
                    ?: throw BadRequestException("Missing job application id")
                jobApplicationService.reject(requester, jobApplicationId)
                call.respond(HttpStatusCode.OK)
            }

        }
    }
}