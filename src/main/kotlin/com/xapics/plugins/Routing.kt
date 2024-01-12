package com.xapics.plugins

import com.xapics.data.PicsDao
import com.xapics.data.UserDataSource
import com.xapics.data.security.hashing.HashingService
import com.xapics.data.security.token.TokenConfig
import com.xapics.data.security.token.TokenService
import com.xapics.routes.*
import com.xapics.routes.uploadFile
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
) {
    routing {
        get("/") {
            call.respondText("Hello World!") // TODO
        }

        signUp(hashingService, userDataSource)
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        authenticate()
        getUserInfo()

        val picsDao by inject<PicsDao>()

        picsList(picsDao)
        films(picsDao)
        rolls(picsDao)
        rollThumbs(picsDao)
        collections(picsDao)
        uploadFile()
//        listOfPics()
//        randomPic()
        staticResources("", "static") // TODO remotePath = "/pics" ?
    }
}
