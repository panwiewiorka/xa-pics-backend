package com.xapics.routes

import com.xapics.data.PicsDao
import com.xapics.data.models.FilmPic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getFilmsList(
    picsDaoImpl: PicsDao
) {
    get("/filmslist") {
        call.respond(
            HttpStatusCode.OK,
            picsDaoImpl.getFilmsList()
        )
    }
}