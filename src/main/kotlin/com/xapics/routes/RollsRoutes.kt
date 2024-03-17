package com.xapics.routes

import com.xapics.data.*
import com.xapics.routes.authenticate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.kotlin.datetime.dateLiteral
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun Route.rolls(
    picsDao: PicsDao,
) {
    get("v1/rollthumbs") {
        call.respond(
            HttpStatusCode.OK,
            picsDao.getRollThumbs()
        )
    }

    get("v1/rolls") {
        call.respond(
            HttpStatusCode.OK,
            picsDao.getRollsList()
        )
    }

    authenticate {
        post("v1/rolls") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName != "admin") {
                call.respond(HttpStatusCode.Forbidden)
            } else {
                val roll = call.receiveParameters()

                transaction {
                    val filmEntity = FilmEntity.find { Films.filmName eq (roll["film"] ?: "nullFilm") }.first() // TODO firstOrNull ?
                    val rollTitle = roll["title"] ?: "nullRoll" // TODO filter spaces and other special symbols (FOR PATH ONLY)

                    if (roll["isNewRoll"] == "true") {
                        val rollIndex = (Rolls.selectAll().count() + 1).toInt()

                        RollEntity.new {
                            index = rollIndex
                            title = rollTitle
                            film = filmEntity
                            xpro = roll["xpro"] == "true"
                            expired = roll["expired"] == "true"
                        }
                    } else {
                        val rollToEdit = RollEntity.find { Rolls.title eq rollTitle }.first()
                        // TODO how to edit title? get roll id?
                        rollToEdit.film = filmEntity
                        rollToEdit.xpro = roll["xpro"] == "true"
                        rollToEdit.expired = roll["expired"] == "true"
                    }
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}