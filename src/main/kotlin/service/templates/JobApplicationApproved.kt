package com.andreromano.devjobboard.service.templates

data class JobApplicationApproved(
    val applicantName: String,
    val jobTitle: String,
    val company: String,
    val approvalDate: String,        // e.g., "April 16, 2025"
    val nextStepInfo: String,        // e.g., "We’ll contact you soon"
    val jobLink: String,
    val companyLogoUrl: String? = null
) : Template {
    override val _filename: String = "job_application_approved.html"
}
