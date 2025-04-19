package com.andreromano.devjobboard.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin

fun Application.createHikariDataSource(): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = environment.config.property("postgres.url").getString()
        username = environment.config.property("postgres.user").getString()
        password = environment.config.property("postgres.password").getString()
        maximumPoolSize = environment.config.propertyOrNull("maxPoolSize")?.getString()?.toInt() ?: 10
        isAutoCommit = true
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    return HikariDataSource(hikariConfig)
}

fun HikariDataSource.runMigrations(): HikariDataSource = also {
    val clean = true
    val flyway = Flyway.configure()
        .cleanDisabled(!clean)
        .dataSource(this)
        .load()

    if (clean) flyway.clean()

    flyway.migrate()
}

fun HikariDataSource.createJdbi(): Jdbi = Jdbi.create(this).apply {
    installPlugins()
    installPlugin(KotlinPlugin(enableCoroutineSupport = true))
}