package com.xapics.routes

import com.xapics.data.FilmEntity
import com.xapics.data.Films
import com.xapics.data.PicsDao
import com.xapics.data.models.FilmType
import com.xapics.data.models.FilmType.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun Route.films(
    picsDaoImpl: PicsDao
) {
    get("v1/films") {
        call.respond(
            HttpStatusCode.OK,
            picsDaoImpl.getFilmsList()
        )
    }

    val log = LoggerFactory.getLogger(this.javaClass)

    authenticate {
        post("v1/films") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName == "admin") {
                val film = call.receiveParameters()
//        log.debug(film["filmName"])
                transaction {
                    if (film["isNewFilm"] == "true") {
                        FilmEntity.new {
                            filmName = film["filmName"] ?: "nullFilm"
                            iso = film["iso"]?.toInt() ?: 0
                            type = when(film["type"]) {
                                "SLIDE" -> SLIDE
                                "NEGATIVE" -> NEGATIVE
                                "BW" -> BW
                                else -> NULL
                            }
                        }
                    } else {
                        val filmToEdit = FilmEntity.find { Films.filmName eq (film["filmName"] ?: "nullFilm") }.first()
                        filmToEdit.iso = film["iso"]?.toInt() ?: 0
                        filmToEdit.type = when(film["type"]) {
                            "SLIDE" -> SLIDE
                            "NEGATIVE" -> NEGATIVE
                            "BW" -> BW
                            else -> NULL
                        }
                    }
                }
                call.respond(HttpStatusCode.Accepted)
            } else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}