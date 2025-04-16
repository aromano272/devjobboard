package com.andreromano.devjobboard.di

import com.andreromano.devjobboard.database.*
import com.andreromano.devjobboard.service.*
import io.ktor.server.application.*
import org.jdbi.v3.core.Jdbi
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun appModule(application: Application) = module {
    single<Jdbi> {
        application.createHikariDataSource()
            .runMigrations()
            .createJdbi()
    }

    single<JobService> {
        DefaultJobService(
            jobDao = get()
        )
    }

    single<JobDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobDao::class.java)
    }

    single<UserDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(UserDao::class.java)
    }

    single<RefreshTokenDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(RefreshTokenDao::class.java)
    }

    single<JwtService> {
        DefaultJwtService(application.environment.config)
    }

    single<AuthService> {
        DefaultAuthService(
            jwtService = get(),
            userDao = get(),
            refreshTokenDao = get(),
        )
    }
}

fun Application.configureDI() {
    val app = this

    install(Koin) {
        slf4jLogger()
        modules(appModule(app))
    }
}
