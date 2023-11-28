package com.xapics.plugins

import com.xapics.data.PicsDao
import com.xapics.routes.getFilmsList
import com.xapics.routes.getPicsList
import com.xapics.routes.uploadFile
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!") // TODO
        }
        val picsDao by inject<PicsDao>()
        getPicsList(picsDao)
        getFilmsList(picsDao)
        uploadFile()
//        listOfPics()
//        randomPic()
        staticResources("", "static") // TODO remotePath = "/pics" ?
    }
}
