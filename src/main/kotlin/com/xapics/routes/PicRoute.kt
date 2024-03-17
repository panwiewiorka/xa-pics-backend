package com.xapics.routes

import com.xapics.data.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun Route.pic(
    picsDao: PicsDao,
) {
    authenticate {
        post("v1/pic") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName != "admin") {
                call.respond(HttpStatusCode.Forbidden)
            } else {
                val picParams = call.receiveParameters()
                val picId = picParams["id"]?.toInt()

                if (picId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    val success = picsDao.editPic(picParams, picId)
                    if (success) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}