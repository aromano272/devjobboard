package com.andreromano.devjobboard.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.andreromano.devjobboard.database.RefreshTokenDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.models.Tokens
import java.time.Instant
import java.util.UUID

interface AuthRepository {
    fun registerAndLogin(username: String, password: String, isAdmin: Boolean): Tokens
    fun login(username: String, password: String): Tokens
    fun refreshToken(refreshToken: String): Tokens
    fun logout(username: String, refreshToken: String)
}

class DefaultAuthRepository(
    private val jwtService: JwtService,
    private val userDao: UserDao,
    private val refreshTokenDao: RefreshTokenDao,
) : AuthRepository {

    override fun registerAndLogin(username: String, password: String, isAdmin: Boolean): Tokens {
        if (userDao.findByUsername(username) != null) throw IllegalArgumentException("username already exists")

        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        userDao.insert(username, isAdmin, hashedPassword)

        return login(username, password)
    }

    override fun login(username: String, password: String): Tokens {
        val error = IllegalArgumentException("invalid username or password")

        val user = userDao.findByUsername(username) ?: throw error
        val storedPassHash = user.passwordHash
        val result = BCrypt.verifyer().verify(password.toCharArray(), storedPassHash)

        return if (result.verified) {
            val access = jwtService.create(username, user.isAdmin)
            val refresh = UUID.randomUUID().toString()

            val expiry = Instant.ofEpochMilli(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
            refreshTokenDao.insert(user.id, refresh, Instant.now(), expiry)

            Tokens(access, refresh)
        } else {
            throw error
        }
    }

    override fun refreshToken(refreshToken: String): Tokens {
        val error = IllegalArgumentException("couldn't refresh token")
        val stored = refreshTokenDao.findByToken(refreshToken) ?: throw error
        if (stored.expiresAt.isBefore(Instant.now())) throw error

        val user = userDao.findById(stored.userId) ?: throw error

        refreshTokenDao.delete(refreshToken)

        val newRefreshToken = UUID.randomUUID().toString()
        val expiry = Instant.ofEpochMilli(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        refreshTokenDao.insert(user.id, newRefreshToken, Instant.now(), expiry)

        val newAccessToken = jwtService.create(user.username, user.isAdmin)

        return Tokens(newAccessToken, newRefreshToken)
    }

    override fun logout(username: String, refreshToken: String) {
        val error = IllegalArgumentException("couldn't logout")
        val stored = refreshTokenDao.findByToken(refreshToken) ?: throw error

        val user = userDao.findById(stored.userId) ?: throw error
        if (user.username != username) throw error

        refreshTokenDao.delete(refreshToken)
    }
}