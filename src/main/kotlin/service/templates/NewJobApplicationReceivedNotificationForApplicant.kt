package com.andreromano.devjobboard.service.templates

data class NewJobApplicationReceivedNotificationForApplicant(
    val applicantName: String,
    val jobTitle: String,
    val company: String,
    val applicationDate: String, // e.g., "April 16, 2025"
    val jobLink: String,
    val companyLogoUrl: String? = null // optional
) : Template {
    override val _filename: String = "new_job_application_received_notification_for_applicant.html"
}