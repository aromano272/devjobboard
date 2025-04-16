package com.andreromano.devjobboard.service

import at.favre.lib.crypto.bcrypt.BCrypt
import com.andreromano.devjobboard.database.RefreshTokenDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.Tokens
import com.andreromano.devjobboard.models.UnauthorizedException
import java.time.Instant
import java.util.*

interface AuthService {
    fun registerAndLogin(username: String, password: String, isAdmin: Boolean): Tokens
    fun login(username: String, password: String): Tokens
    fun refreshToken(refreshToken: String): Tokens
    fun logout(username: String, refreshToken: String)
}

class DefaultAuthService(
    private val jwtService: JwtService,
    private val userDao: UserDao,
    private val refreshTokenDao: RefreshTokenDao,
) : AuthService {

    override fun registerAndLogin(username: String, password: String, isAdmin: Boolean): Tokens {
        if (userDao.findByUsername(username) != null) throw ConflictException("username already exists")

        val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
        userDao.insert(username, isAdmin, hashedPassword)

        return login(username, password)
    }

    override fun login(username: String, password: String): Tokens {
        val user = userDao.findByUsername(username) ?: throw NotFoundException("couldn't find user")
        val storedPassHash = user.password_hash
        val result = BCrypt.verifyer().verify(password.toCharArray(), storedPassHash)

        return if (result.verified) {
            val access = jwtService.create(username, user.is_admin)
            val refresh = UUID.randomUUID().toString()

            val expiry = Instant.ofEpochMilli(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
            refreshTokenDao.insert(user.id, refresh, expiry)

            Tokens(access, refresh)
        } else {
            throw UnauthorizedException("wrong password")
        }
    }

    override fun refreshToken(refreshToken: String): Tokens {
        val stored = refreshTokenDao.findByToken(refreshToken)
            ?: throw UnauthorizedException("couldn't find refresh token")
        val user = userDao.findById(stored.user_id) ?: throw NotFoundException("couldn't find user")

        if (stored.expires_at.isBefore(Instant.now())) throw UnauthorizedException("refresh token has expired")

        refreshTokenDao.delete(refreshToken)

        val newRefreshToken = UUID.randomUUID().toString()
        val expiry = Instant.ofEpochMilli(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        refreshTokenDao.insert(user.id, newRefreshToken, expiry)

        val newAccessToken = jwtService.create(user.username, user.is_admin)

        return Tokens(newAccessToken, newRefreshToken)
    }

    override fun logout(username: String, refreshToken: String) {
        val stored =
            refreshTokenDao.findByToken(refreshToken) ?: throw UnauthorizedException("couldn't find refresh token")

        val user = userDao.findById(stored.user_id) ?: throw NotFoundException("couldn't find user")
        if (user.username != username) throw UnauthorizedException("username doesn't match refreshToken's username")

        refreshTokenDao.delete(refreshToken)
    }
}