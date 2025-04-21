package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.RefreshTokenDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.Tokens
import com.andreromano.devjobboard.models.UnauthorizedException
import java.time.Instant
import java.util.*

interface AuthService {
    suspend fun registerAndLogin(
        username: String,
        email: String,
        password: String,
        isAdmin: Boolean
    ): Tokens
    suspend fun login(username: String, password: String): Tokens
    suspend fun refreshToken(refreshToken: String): Tokens
    suspend fun logout(username: String, refreshToken: String)
}

class DefaultAuthService(
    private val jwtService: JwtService,
    private val userDao: UserDao,
    private val refreshTokenDao: RefreshTokenDao,
    private val passwordService: PasswordService,
) : AuthService {

    override suspend fun registerAndLogin(
        username: String,
        email: String,
        password: String,
        isAdmin: Boolean
    ): Tokens {
        if (userDao.findByUsername(username) != null) throw ConflictException("username already exists")

        val hashedPassword = passwordService.hash(password)
        userDao.insert(username, email, isAdmin, hashedPassword)

        return login(username, password)
    }

    override suspend fun login(username: String, password: String): Tokens {
        val user = userDao.findByUsername(username) ?: throw NotFoundException("couldn't find user")
        val storedPassHash = user.passwordHash
        val isValid = passwordService.verify(password, storedPassHash)

        return if (isValid) {
            val access = jwtService.create(user.id, username, user.isAdmin)
            val refresh = UUID.randomUUID().toString()

            val expiry = getNewRefreshTokenExpiry()
            refreshTokenDao.insert(user.id, refresh, expiry)

            Tokens(access, refresh)
        } else {
            throw UnauthorizedException("wrong password")
        }
    }

    override suspend fun refreshToken(refreshToken: String): Tokens {
        val stored = refreshTokenDao.findByToken(refreshToken)
            ?: throw UnauthorizedException("couldn't find refresh token")
        val user = userDao.findById(stored.userId) ?: throw NotFoundException("couldn't find user")

        if (stored.expiresAt.isBefore(Instant.now())) throw UnauthorizedException("refresh token has expired")

        refreshTokenDao.delete(refreshToken)

        val newRefreshToken = UUID.randomUUID().toString()
        val expiry = getNewRefreshTokenExpiry()
        refreshTokenDao.insert(user.id, newRefreshToken, expiry)

        val newAccessToken = jwtService.create(user.id, user.username, user.isAdmin)

        return Tokens(newAccessToken, newRefreshToken)
    }

    override suspend fun logout(username: String, refreshToken: String) {
        val stored =
            refreshTokenDao.findByToken(refreshToken) ?: throw UnauthorizedException("couldn't find refresh token")

        val user = userDao.findById(stored.userId) ?: throw NotFoundException("couldn't find user")
        if (user.username != username) throw UnauthorizedException("username doesn't match refreshToken's username")

        refreshTokenDao.delete(refreshToken)
    }

    private fun getNewRefreshTokenExpiry() = Instant.ofEpochMilli(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
}