package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.service.templates.Template
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.util.logging.Logger

interface EmailService {
    suspend fun sendEmail(to: String, subject: String, template: Template)
}

class MailgunEmailService(
    dotenv: Dotenv,
    private val httpClient: HttpClient,
    private val emailTemplateRenderer: EmailTemplateRenderer,
    private val logger: Logger,
) : EmailService {

    private val apiKey = dotenv["MAILGUN_API_KEY"]
    private val domain = dotenv["MAILGUN_DOMAIN"]
    private val fromEmail = "DevJobBoard <noreply@devjobboard.com>"

    override suspend fun sendEmail(to: String, subject: String, template: Template) {
        val url = "https://api.mailgun.net/v3/$domain/messages"

        val htmlBody = emailTemplateRenderer.render(template)

        val response = httpClient.submitForm(
            url = url,
            formParameters = Parameters.build {
                append("from", fromEmail)
                append("to", to)
                append("subject", subject)
                append("html", htmlBody)
            }
        ) {
            basicAuth("api", apiKey)
        }

        if (!response.status.isSuccess()) {
            logger.error(response.bodyAsText())
            throw RuntimeException("Failed to send email: ${response.status}")
        }
    }

}