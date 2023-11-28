package com.xapics

import com.xapics.data.DatabaseFactory
import com.xapics.plugins.*
import io.ktor.server.application.*
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureMonitoring()
    configureKoin()
    configureRouting()
}
