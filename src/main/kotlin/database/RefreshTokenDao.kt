package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.RefreshTokenEntity
import org.jdbi.v3.sqlobject.statement.*
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.kotlin.RegisterKotlinMapper
import java.time.Instant

@RegisterKotlinMapper(RefreshTokenEntity::class)
interface RefreshTokenDao {

    @SqlUpdate("""
        INSERT INTO refresh_tokens (user_id, token, expires_at)
        VALUES (:userId, :token, :expiresAt)
    """)
    fun insert(
        @Bind("userId") userId: Int,
        @Bind("token") token: String,
        @Bind("expiresAt") expiresAt: Instant,
    )

    @SqlQuery("SELECT * FROM refresh_tokens WHERE token = :token")
    fun findByToken(@Bind("token") token: String): RefreshTokenEntity?

    @SqlUpdate("DELETE FROM refresh_tokens WHERE token = :token")
    fun delete(@Bind("token") token: String)

    @SqlUpdate("DELETE FROM refresh_tokens WHERE user_id = :userId")
    fun deleteAllByUser(@Bind("userId") userId: Int)

    @SqlUpdate("DELETE FROM refresh_tokens WHERE expires_at < now()")
    fun cleanExpired()
}
