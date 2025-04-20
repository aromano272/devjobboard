package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.RefreshTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

interface RefreshTokenDao {

    suspend fun insert(
        userId: Int,
        token: String,
        expiresAt: Instant,
    )

    suspend fun findByToken(token: String): RefreshTokenEntity?

    suspend fun delete(token: String)

    suspend fun deleteAllByUser(userId: Int)

    suspend fun cleanExpired()
}

class DefaultRefreshTokenDao(
    private val db: RefreshTokenDb,
) : RefreshTokenDao {
    override suspend fun insert(Id: Int, token: String, expiresAt: Instant) = withContext(Dispatchers.IO) {
        db.insert(Id, token, expiresAt)
    }

    override suspend fun findByToken(token: String): RefreshTokenEntity? = withContext(Dispatchers.IO) {
        db.findByToken(token)
    }

    override suspend fun delete(token: String) = withContext(Dispatchers.IO) {
        db.delete(token)
    }

    override suspend fun deleteAllByUser(userId: Int) = withContext(Dispatchers.IO) {
        db.deleteAllByUser(userId)
    }

    override suspend fun cleanExpired() = withContext(Dispatchers.IO) {
        db.cleanExpired()
    }
}
