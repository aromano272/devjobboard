package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.service.JobApplicationState
import org.jdbi.v3.core.enums.EnumByName

@EnumByName
enum class JobApplicationStateEntity {
    PENDING,
    APPROVED,
    REJECTED,
}

fun JobApplicationStateEntity.toDomain(): JobApplicationState = when (this) {
    JobApplicationStateEntity.PENDING -> JobApplicationState.PENDING
    JobApplicationStateEntity.APPROVED -> JobApplicationState.APPROVED
    JobApplicationStateEntity.REJECTED -> JobApplicationState.REJECTED
}

fun JobApplicationState.toEntity(): JobApplicationStateEntity = when (this) {
    JobApplicationState.PENDING -> JobApplicationStateEntity.PENDING
    JobApplicationState.APPROVED -> JobApplicationStateEntity.APPROVED
    JobApplicationState.REJECTED -> JobApplicationStateEntity.REJECTED
}