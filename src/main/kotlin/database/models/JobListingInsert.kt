package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.models.Cents

data class JobListingInsert(
    val title: String,
    val company: String,
    val location: String,
    val remote: Boolean,
    val salary: Cents?
)