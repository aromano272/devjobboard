package com.andreromano.devjobboard.models

import kotlinx.serialization.Serializable

@Serializable
data class JobListing(
    val id: Long,
    val title: String,
    val company: String,
    val location: String,
    val remote: Boolean,
    val salary: Cents?,
)
