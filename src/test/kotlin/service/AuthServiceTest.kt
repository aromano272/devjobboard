package com.andreromano.devjobboard.service

import com.andreromano.devjobboard.database.RefreshTokenDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.database.models.RefreshTokenEntity
import com.andreromano.devjobboard.database.models.UserEntity
import com.andreromano.devjobboard.models.ConflictException
import com.andreromano.devjobboard.models.NotFoundException
import com.andreromano.devjobboard.models.UnauthorizedException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AuthServiceTest {
    private val jwtService: JwtService = mockk()
    private val userDao: UserDao = mockk()
    private val refreshTokenDao: RefreshTokenDao = mockk()
    private val passwordService: PasswordService = mockk()
    private val authService = DefaultAuthService(
        jwtService = jwtService,
        userDao = userDao,
        refreshTokenDao = refreshTokenDao,
        passwordService = passwordService
    )

    @Test
    fun `registerAndLogin should throw ConflictException when username exists`() = runTest {
        val username = "testuser"
        val email = "test@example.com"
        val password = "password"
        val isAdmin = false

        coEvery { userDao.findByUsername(username) } returns TEST_USER_ENTITY

        assertFailsWith<ConflictException> {
            authService.registerAndLogin(username, email, password, isAdmin)
        }
    }

    @Test
    fun `registerAndLogin should create user and return tokens`() = runTest {
        val username = "testuser"
        val email = "test@example.com"
        val password = "password"
        val isAdmin = false
        val userId = 1
        val accessToken = "test_access_token"
        val hashedPassword = "\$2a\$12\$1234567890123456789012"

        coEvery { userDao.findByUsername(username) }.returnsMany(null, TEST_USER_ENTITY)
        every { passwordService.hash(password) } returns hashedPassword
        coEvery { userDao.insert(username, email, isAdmin, hashedPassword) } returns userId
        coEvery { jwtService.create(userId, username, isAdmin) } returns accessToken
        coEvery { refreshTokenDao.insert(userId, any(), any()) } returns Unit
        every { passwordService.verify(password, hashedPassword) } returns true

        val tokens = authService.registerAndLogin(username, email, password, isAdmin)

        assertNotNull(tokens)
        assertEquals(accessToken, tokens.access)
        assertNotNull(tokens.refresh)
    }

    @Test
    fun `login should throw NotFoundException when user not found`() = runTest {
        val username = "testuser"
        val password = "password"

        coEvery { userDao.findByUsername(username) } returns null

        assertFailsWith<NotFoundException> {
            authService.login(username, password)
        }
    }

    @Test
    fun `login should throw UnauthorizedException when password is wrong`() = runTest {
        val username = "testuser"
        val password = "wrong_password"
        val user = TEST_USER_ENTITY

        coEvery { userDao.findByUsername(username) } returns user
        every { passwordService.verify(password, user.passwordHash) } returns false

        assertFailsWith<UnauthorizedException> {
            authService.login(username, password)
        }
    }

    @Test
    fun `login should return tokens when credentials are correct`() = runTest {
        val username = "testuser"
        val password = "password"
        val user = TEST_USER_ENTITY
        val accessToken = "test_access_token"

        coEvery { userDao.findByUsername(username) } returns user
        every { passwordService.verify(password, user.passwordHash) } returns true
        coEvery { jwtService.create(user.id, username, user.isAdmin) } returns accessToken
        coEvery { refreshTokenDao.insert(user.id, any(), any()) } returns Unit

        val tokens = authService.login(username, password)

        assertNotNull(tokens)
        assertEquals(accessToken, tokens.access)
        assertNotNull(tokens.refresh)
    }

    @Test
    fun `refreshToken should throw UnauthorizedException when token not found`() = runTest {
        val refreshToken = "invalid_token"

        coEvery { refreshTokenDao.findByToken(refreshToken) } returns null

        assertFailsWith<UnauthorizedException> {
            authService.refreshToken(refreshToken)
        }
    }

    @Test
    fun `refreshToken should throw UnauthorizedException when token is expired`() = runTest {
        val refreshToken = "expired_token"
        val expiredToken = RefreshTokenEntity(
            id = 1,
            userId = 1,
            token = refreshToken,
            expiresAt = Instant.now().minusSeconds(3600),
            createdAt = Instant.now().minusSeconds(3600),
        )

        coEvery { refreshTokenDao.findByToken(refreshToken) } returns expiredToken
        coEvery { userDao.findById(expiredToken.userId) } returns TEST_USER_ENTITY

        assertFailsWith<UnauthorizedException> {
            authService.refreshToken(refreshToken)
        }
    }

    @Test
    fun `refreshToken should return new tokens when token is valid`() = runTest {
        val refreshToken = "valid_token"
        val user = TEST_USER_ENTITY
        val validToken = RefreshTokenEntity(
            id = user.id,
            userId = user.id,
            token = refreshToken,
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now().plusSeconds(3600),
        )
        val newAccessToken = "new_access_token"

        coEvery { refreshTokenDao.findByToken(refreshToken) } returns validToken
        coEvery { userDao.findById(user.id) } returns user
        coEvery { refreshTokenDao.delete(refreshToken) } returns Unit
        coEvery { refreshTokenDao.insert(user.id, any(), any()) } returns Unit
        coEvery { jwtService.create(user.id, user.username, user.isAdmin) } returns newAccessToken

        val tokens = authService.refreshToken(refreshToken)

        assertNotNull(tokens)
        assertEquals(newAccessToken, tokens.access)
        assertNotNull(tokens.refresh)
    }

    @Test
    fun `logout should throw UnauthorizedException when token not found`() = runTest {
        val username = "testuser"
        val refreshToken = "invalid_token"

        coEvery { refreshTokenDao.findByToken(refreshToken) } returns null

        assertFailsWith<UnauthorizedException> {
            authService.logout(username, refreshToken)
        }
    }

    @Test
    fun `logout should throw UnauthorizedException when username doesn't match`() = runTest {
        val username = "wrong_user"
        val refreshToken = "valid_token"
        val tokenEntity = RefreshTokenEntity(
            id = 1,
            userId = 1,
            token = refreshToken,
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now().plusSeconds(3600),
        )
        val user = TEST_USER_ENTITY.copy(username = "correct_user")

        coEvery { refreshTokenDao.findByToken(refreshToken) } returns tokenEntity
        coEvery { userDao.findById(tokenEntity.userId) } returns user

        assertFailsWith<UnauthorizedException> {
            authService.logout(username, refreshToken)
        }
    }

    @Test
    fun `logout should delete token when successful`() = runTest {
        val username = "testuser"
        val refreshToken = "valid_token"
        val tokenEntity = RefreshTokenEntity(
            id = 1,
            userId = 1,
            token = refreshToken,
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now().plusSeconds(3600),
        )
        val user = TEST_USER_ENTITY.copy(username = username)

        coEvery { refreshTokenDao.findByToken(refreshToken) } returns tokenEntity
        coEvery { userDao.findById(tokenEntity.userId) } returns user
        coEvery { refreshTokenDao.delete(refreshToken) } returns Unit

        authService.logout(username, refreshToken)

        coVerify { refreshTokenDao.delete(refreshToken) }
    }

    companion object {
        private val TEST_USER_ENTITY = UserEntity(
            id = 1,
            username = "testuser",
            email = "test@example.com",
            passwordHash = "\$2a\$12\$1234567890123456789012",
            isAdmin = false
        )
    }
} 