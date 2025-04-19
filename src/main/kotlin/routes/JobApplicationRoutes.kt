package com.andreromano.devjobboard.routes

import com.andreromano.devjobboard.models.BadRequestException
import com.andreromano.devjobboard.models.requireRequester
import com.andreromano.devjobboard.routes.models.getAndValidateEnum
import com.andreromano.devjobboard.service.JobApplicationService
import com.andreromano.devjobboard.service.JobApplicationState
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
        }
    }
}