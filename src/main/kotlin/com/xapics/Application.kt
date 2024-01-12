package com.xapics

import com.xapics.data.DatabaseFactory
import com.xapics.data.UserDataSource
import com.xapics.data.security.hashing.SHA256HashingService
import com.xapics.data.security.token.JwtTokenService
import com.xapics.data.security.token.TokenConfig
import com.xapics.plugins.*
import io.ktor.server.application.*
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.days

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()

    val userDataSource by inject<UserDataSource>()
    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365.days.inWholeMilliseconds,
//        secret = System.getenv("JWT_SECRET")
        secret = environment.config.property("jwt.secret").getString(),
    )
    val hashingService = SHA256HashingService()
//    val BASE_URL = environment.config.property("jwt.domain").getString()

    configureSerialization()
    configureMonitoring()
    configureKoin()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig)
}
