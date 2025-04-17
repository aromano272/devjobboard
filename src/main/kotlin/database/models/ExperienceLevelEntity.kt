package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.models.ExperienceLevel
import org.jdbi.v3.core.enums.EnumByName

@EnumByName
enum class ExperienceLevelEntity {
    JUNIOR, MID_LEVEL, SENIOR, STAFF
}

fun ExperienceLevelEntity.toDomain(): ExperienceLevel = when (this) {
    ExperienceLevelEntity.JUNIOR -> ExperienceLevel.JUNIOR
    ExperienceLevelEntity.MID_LEVEL -> ExperienceLevel.MID_LEVEL
    ExperienceLevelEntity.SENIOR -> ExperienceLevel.SENIOR
    ExperienceLevelEntity.STAFF -> ExperienceLevel.STAFF
}

