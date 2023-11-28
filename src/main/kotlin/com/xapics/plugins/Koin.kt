package com.xapics.plugins

import com.xapics.data.PicsDao
import com.xapics.data.PicsDaoImpl
import io.ktor.server.application.*
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(picModule)
    }
}

val picModule = module {
    single<PicsDao> { PicsDaoImpl() }
}