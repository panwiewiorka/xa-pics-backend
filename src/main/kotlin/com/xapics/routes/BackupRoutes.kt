package com.xapics.routes

import com.xapics.data.models.TheString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.backup() {
    authenticate {
        get("v1/backup") {
            call.respond(
                status = HttpStatusCode.OK,
                message = TheString("backup/latest.zip")
            )
        }
    }
}