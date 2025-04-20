package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface UserDao {

    suspend fun insert(
        username: String,
        email: String,
        isAdmin: Boolean,
        passwordHash: String,
    ): Int

    suspend fun getAllByIds(ids: List<Int>): List<UserEntity>

    suspend fun findByUsername(username: String): UserEntity?

    suspend fun findById(id: Int): UserEntity?

}

class DefaultUserDao(
    private val db: UserDb,
) : UserDao {
    override suspend fun insert(
        username: String,
        email: String,
        isAdmin: Boolean,
        passwordHash: String
    ): Int = withContext(Dispatchers.IO) {
        db.insert(username, email, isAdmin, passwordHash)
    }

    override suspend fun getAllByIds(ids: List<Int>): List<UserEntity> = withContext(Dispatchers.IO) {
        db.getAllByIds(ids)
    }

    override suspend fun findByUsername(username: String): UserEntity? = withContext(Dispatchers.IO) {
        db.findByUsername(username)
    }

    override suspend fun findById(id: Int): UserEntity? = withContext(Dispatchers.IO) {
        db.findById(id)
    }
}