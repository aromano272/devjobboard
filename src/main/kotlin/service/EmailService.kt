package com.andreromano.devjobboard.service

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.util.logging.Logger

interface EmailService {
    suspend fun sendEmail(to: String, subject: String, htmlBody: String)
}

class MailgunEmailService(
    dotenv: Dotenv,
    private val httpClient: HttpClient,
    private val logger: Logger,
) : EmailService {

    private val apiKey = dotenv["MAILGUN_API_KEY"]
    private val domain = dotenv["MAILGUN_DOMAIN"]
    private val fromEmail = "DevJobBoard <noreply@devjobboard.com>"

    override suspend fun sendEmail(to: String, subject: String, htmlBody: String) {
        val url = "https://api.mailgun.net/v3/$domain/messages"

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