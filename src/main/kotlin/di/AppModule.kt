package com.andreromano.devjobboard.di

import com.andreromano.devjobboard.database.*
import com.andreromano.devjobboard.service.*
import io.ktor.server.application.*
import org.jdbi.v3.core.Jdbi
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun databaseModule(application: Application) = module {
    single<Jdbi> {
        application.createHikariDataSource()
            .runMigrations()
            .createJdbi()
    }

    single<JobDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobDao::class.java)
    }

    single<JobApplicationDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobApplicationDao::class.java)
    }

    single<JobFavoriteDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobFavoriteDao::class.java)
    }

    single<UserDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(UserDao::class.java)
    }

    single<RefreshTokenDao> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(RefreshTokenDao::class.java)
    }
}

fun serviceModule(application: Application) = module {
    single<JwtService> {
        DefaultJwtService(
            config = application.environment.config,
            developmentMode = application.developmentMode,
        )
    }

    single<AuthService> {
        DefaultAuthService(
            jwtService = get(),
            userDao = get(),
            refreshTokenDao = get(),
        )
    }

    single<JobService> {
        DefaultJobService(
            jobDao = get(),
            jobFavoriteDao = get(),
            jobApplicationDao = get(),
        )
    }

    single<JobApplicationService> {
        DefaultJobApplicationService(
            jobDao = get(),
            jobApplicationDao = get(),
            jobFavoriteDao = get(),
            userDao = get(),
        )
    }

}

fun Application.configureDI() {
    val app = this

    install(Koin) {
        slf4jLogger()
        modules(databaseModule(app))
        modules(serviceModule(app))
    }
}
