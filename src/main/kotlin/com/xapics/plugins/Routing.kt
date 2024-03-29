package com.xapics.plugins

import com.xapics.data.PicsDao
import com.xapics.data.UserDataSource
import com.xapics.data.security.hashing.HashingService
import com.xapics.data.security.token.TokenConfig
import com.xapics.data.security.token.TokenService
import com.xapics.module
import com.xapics.routes.*
import com.xapics.routes.uploadFile
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.io.File


fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
) {
    routing {
        get("/") {
//            val log = LoggerFactory.getLogger(this.javaClass)
//            log.debug("")

            call.respondText("XA pics") // TODO
        }

        signUp(hashingService, userDataSource)
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        authenticate()
        getUserInfo()

        val picsDao by inject<PicsDao>()

        search(picsDao)
        films(picsDao)
        rolls(picsDao)
        pic(picsDao)
        collections(picsDao)
        randomPic(picsDao)
        tags(picsDao)
        uploadFile()
        backup()
        staticResources("v1/files/images/pics", "static/pics")
        staticFiles("v1/files", File("volume-v1/files"))
    }
}
