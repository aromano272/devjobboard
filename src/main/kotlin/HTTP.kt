package com.andreromano.devjobboard

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.github.smiley4.schemakenerator.swagger.data.RefType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureHTTP() {
    install(OpenApi) {
        spec("3.0.0") {}
        outputFormat = OutputFormat.YAML
        schemas {
            generator = SchemaGenerator.reflection {
                referencePath = RefType.OPENAPI_SIMPLE
            }
        }
    }
    routing {
        route("/openapi/api.yaml") {
            openApi()
        }
        route("/swagger") {
            swaggerUI("/openapi/api.yaml")
        }
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}
