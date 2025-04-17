package com.andreromano.devjobboard.database.models

import com.andreromano.devjobboard.models.RemoteOption
import org.jdbi.v3.core.enums.EnumByName

@EnumByName
enum class RemoteOptionEntity {
    ON_SITE, HYBRID, REMOTE
}

fun RemoteOptionEntity.toDomain(): RemoteOption = when (this) {
    RemoteOptionEntity.REMOTE -> RemoteOption.REMOTE
    RemoteOptionEntity.HYBRID -> RemoteOption.HYBRID
    RemoteOptionEntity.ON_SITE -> RemoteOption.ON_SITE
}

