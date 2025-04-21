package com.andreromano.devjobboard.di

import at.favre.lib.crypto.bcrypt.BCrypt
import com.andreromano.devjobboard.database.DefaultJobApplicationDao
import com.andreromano.devjobboard.database.DefaultJobDao
import com.andreromano.devjobboard.database.DefaultJobFavoriteDao
import com.andreromano.devjobboard.database.DefaultRefreshTokenDao
import com.andreromano.devjobboard.database.DefaultUserDao
import com.andreromano.devjobboard.database.JobApplicationDao
import com.andreromano.devjobboard.database.JobApplicationDb
import com.andreromano.devjobboard.database.JobDao
import com.andreromano.devjobboard.database.JobDb
import com.andreromano.devjobboard.database.JobFavoriteDao
import com.andreromano.devjobboard.database.JobFavoriteDb
import com.andreromano.devjobboard.database.RefreshTokenDao
import com.andreromano.devjobboard.database.RefreshTokenDb
import com.andreromano.devjobboard.database.UserDao
import com.andreromano.devjobboard.database.UserDb
import com.andreromano.devjobboard.database.createHikariDataSource
import com.andreromano.devjobboard.database.createJdbi
import com.andreromano.devjobboard.database.runMigrations
import com.andreromano.devjobboard.service.AuthService
import com.andreromano.devjobboard.service.DefaultAuthService
import com.andreromano.devjobboard.service.DefaultJobApplicationService
import com.andreromano.devjobboard.service.DefaultJobService
import com.andreromano.devjobboard.service.DefaultJwtService
import com.andreromano.devjobboard.service.DefaultPasswordService
import com.andreromano.devjobboard.service.EmailService
import com.andreromano.devjobboard.service.EmailTemplateRenderer
import com.andreromano.devjobboard.service.JobApplicationService
import com.andreromano.devjobboard.service.JobService
import com.andreromano.devjobboard.service.JwtService
import com.andreromano.devjobboard.service.MailgunEmailService
import com.andreromano.devjobboard.service.MustacheEmailTemplateRenderer
import com.andreromano.devjobboard.service.PasswordService
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheFactory
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

    single<MustacheFactory> {
        DefaultMustacheFactory("templates")
    }

    single<BCrypt.Hasher> {
        BCrypt.withDefaults()
    }

    single<BCrypt.Verifyer> {
        BCrypt.verifyer()
    }

}

fun databaseModule(application: Application) = module {
    single<Jdbi> {
        application.createHikariDataSource()
            .runMigrations()
            .createJdbi()
    }

    single<JobDb> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobDb::class.java)
    }

    single<JobDao> {
        DefaultJobDao(
            db = get()
        )
    }

    single<JobApplicationDb> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobApplicationDb::class.java)
    }

    single<JobApplicationDao> {
        DefaultJobApplicationDao(
            db = get()
        )
    }

    single<JobFavoriteDb> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(JobFavoriteDb::class.java)
    }

    single<JobFavoriteDao> {
        DefaultJobFavoriteDao(
            db = get()
        )
    }

    single<UserDb> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(UserDb::class.java)
    }

    single<UserDao> {
        DefaultUserDao(
            db = get()
        )
    }

    single<RefreshTokenDb> {
        val jdbi: Jdbi = get()
        jdbi.onDemand(RefreshTokenDb::class.java)
    }

    single<RefreshTokenDao> {
        DefaultRefreshTokenDao(
            db = get()
        )
    }
}

fun serviceModule(application: Application) = module {
    single<JwtService> {
        DefaultJwtService(
            config = application.environment.config,
            developmentMode = application.developmentMode,
        )
    }

    single<PasswordService> {
        DefaultPasswordService()
    }

    single<AuthService> {
        DefaultAuthService(
            jwtService = get(),
            userDao = get(),
            refreshTokenDao = get(),
            passwordService = get(),
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
            logger = application.environment.log,
        )
    }

    single<EmailService> {
        MailgunEmailService(
            dotenv = get(),
            httpClient = get(),
            emailTemplateRenderer = get(),
            logger = application.environment.log,
        )
    }

    single<EmailTemplateRenderer> {
        MustacheEmailTemplateRenderer(
            mustacheFactory = get()
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
