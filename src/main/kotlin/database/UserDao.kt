package com.andreromano.devjobboard.database

import com.andreromano.devjobboard.database.models.UserEntity
import org.jdbi.v3.sqlobject.customizer.Bind
import org.jdbi.v3.sqlobject.kotlin.RegisterKotlinMapper
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

@RegisterKotlinMapper(UserEntity::class)
interface UserDao {

    @SqlUpdate("INSERT INTO users (username, is_admin, password_hash) VALUES (:username, :isAdmin, :passwordHash)")
    @GetGeneratedKeys
    fun insert(
        @Bind("username") username: String,
        @Bind("isAdmin") isAdmin: Boolean,
        @Bind("passwordHash") passwordHash: String,
    ): Int

    @SqlQuery("SELECT * FROM users WHERE username = :username")
    fun findByUsername(@Bind("username") username: String): UserEntity?

    @SqlQuery("SELECT * FROM users WHERE id = :id")
    fun findById(@Bind("id") id: Int): UserEntity?

}