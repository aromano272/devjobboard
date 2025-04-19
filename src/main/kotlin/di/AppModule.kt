package com.andreromano.devjobboard.di

import com.andreromano.devjobboard.database.JobApplicationDao
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.JobFavoriteDao
import com.andreromano.devjobboard.database.RefreshTokenDao
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.database.createHikariDataSource
import com.andreromano.devjobboard.database.createJdbi
import com.andreromano.devjobboard.database.runMigrations
import com.andreromano.devjobboard.service.AuthService
import com.andreromano.devjobboard.service.DefaultAuthService
import com.andreromano.devjobboard.service.DefaultJobApplicationService
import com.andreromano.devjobboard.service.DefaultJobService
import com.andreromano.devjobboard.service.DefaultJwtService
import com.andreromano.devjobboard.service.EmailService
import com.andreromano.devjobboard.service.JobApplicationService
import com.andreromano.devjobboard.service.JobService
import com.andreromano.devjobboard.service.JwtService
import com.andreromano.devjobboard.service.MailgunEmailService
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.jdbi.v3.core.Jdbi
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val appModule = module {
    single<Dotenv> {
        dotenv {
            ignoreIfMissing = true
        }
    }

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(DefaultRequest) {
                contentType(ContentType.Application.Json)
            }
        }
    }
}

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
            emailService = get(),
        )
    }

    single<EmailService> {
        MailgunEmailService(
            dotenv = get(),
            httpClient = get(),
            logger = application.environment.log,
        )
    }

}

fun Application.configureDI() {
    val app = this

    install(Koin) {
        slf4jLogger()
        modules(appModule)
        modules(databaseModule(app))
        modules(serviceModule(app))
    }
}
