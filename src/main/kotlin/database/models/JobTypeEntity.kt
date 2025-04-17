package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.models.JobType
import org.jdbi.v3.core.enums.EnumByName

@EnumByName
enum class JobTypeEntity {
    FULL_TIME, PART_TIME, CONTRACTOR, INTERNSHIP
}

fun JobTypeEntity.toDomain(): JobType = when (this) {
    JobTypeEntity.FULL_TIME -> JobType.FULL_TIME
    JobTypeEntity.PART_TIME -> JobType.PART_TIME
    JobTypeEntity.CONTRACTOR -> JobType.CONTRACTOR
    JobTypeEntity.INTERNSHIP -> JobType.INTERNSHIP
}
