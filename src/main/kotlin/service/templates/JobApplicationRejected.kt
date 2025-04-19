package com.andreromano.devjobboard.service.templates

data class JobApplicationRejected(
    val applicantName: String,
    val jobTitle: String,
    val company: String,
    val rejectionDate: String,       // e.g., "April 16, 2025"
    val jobLink: String,
    val companyLogoUrl: String? = null
) : Template {
    override val _filename: String = "job_application_rejected.html"
}
