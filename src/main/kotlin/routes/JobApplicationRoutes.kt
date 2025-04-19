package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.BadRequestException
import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.routes.models.getAndValidateEnum
import com.andreromano.devjobboard.service.JobApplicationService
import com.andreromano.devjobboard.service.JobApplicationState
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.jobApplicationRoutes(
    jobApplicationService: JobApplicationService,
) {
    route("/jobapplications") {
        authenticate {
            post("/apply/{jobId}") {
                val requester = call.requireRequester()
                val jobId = call.parameters["jobId"]?.toIntOrNull() ?: throw BadRequestException("Missing job id")
                jobApplicationService.create(requester, jobId)
                call.respond(HttpStatusCode.OK)
            }

            get {
                val requester = call.requireRequester()
                val state = call.queryParameters.getAndValidateEnum<JobApplicationState>("state")
                val jobs = jobApplicationService.getAllForRequester(requester, state)
                call.respond(jobs)
            }

            get("/job/{jobId}") {
                val requester = call.requireRequester()
                val jobId = call.parameters["jobId"]?.toIntOrNull() ?: throw BadRequestException("Missing job id")
                val state = call.queryParameters.getAndValidateEnum<JobApplicationState>("state")
                val jobs = jobApplicationService.getAllByJobId(requester, jobId, state)
                call.respond(jobs)
            }

            post("/approve/{jobApplicationId}") {
                val requester = call.requireRequester()
                val jobApplicationId = call.parameters["jobApplicationId"]?.toIntOrNull()
                    ?: throw BadRequestException("Missing job application id")
                jobApplicationService.approve(requester, jobApplicationId)
                call.respond(HttpStatusCode.OK)
            }

            post("/reject/{jobApplicationId}") {
                val requester = call.requireRequester()
                val jobApplicationId = call.parameters["jobApplicationId"]?.toIntOrNull()
                    ?: throw BadRequestException("Missing job application id")
                jobApplicationService.reject(requester, jobApplicationId)
                call.respond(HttpStatusCode.OK)
            }

        }
    }
}