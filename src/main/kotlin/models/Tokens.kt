package com.andreromano.devjobboard.models

import kotlinx.serialization.Serializable

@Serializable
data class Tokens(
    val access: String,
    val refresh: String,
)