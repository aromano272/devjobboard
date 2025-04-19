package com.andreromano.devjobboard.service.templates

data class NewJobApplicationReceivedNotificationForRecruiter(
    val recruiterName: String,
    val applicantName: String,
    val jobTitle: String,
    val applicationDate: String, // formatted e.g. "April 16, 2025"
    val jobLink: String,
    val applicantEmail: String,
    val resumeLink: String
) : Template {
    override val _filename: String = "new_job_application_received_notification_for_recruiter.html"
}