package com.xapics.routes

import com.xapics.data.PicsDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.collections(picsDao: PicsDao, baseUrl: String) {
    authenticate {

        val log = LoggerFactory.getLogger(this.javaClass)

        get("pic-collections") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt()
            val picId = call.request.queryParameters["picid"]?.toInt()
            if (picId != null) {
                call.respond(
                    HttpStatusCode.OK,
                    picsDao.getPicCollections(userId!!, picId)
                )
            }
        }

        get("all-collections") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt()
            log.debug("userIDff = $userId")
            call.respond(
                HttpStatusCode.OK,
                picsDao.getAllCollections(userId!!, baseUrl)
            )
        }

        get("collection") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt()
            val collection = call.request.queryParameters["collection"]
            call.respond(
                HttpStatusCode.OK,
                picsDao.getCollection(userId!!, collection!!, baseUrl)
            )
        }

        post("collection") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt()
            val parameters = call.receiveParameters()
            picsDao.editCollection(userId!!, parameters["collection"], parameters["picId"]?.toInt())
            call.respond(HttpStatusCode.Accepted)
        }

        post("rename-delete-collection") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)?.toInt()
            val parameters = call.receiveParameters()
            picsDao.renameOrDeleteCollection(userId!!, parameters["collectionTitle"] ?: "", parameters["renamedTitle"])
            call.respond(HttpStatusCode.Accepted)
        }
    }
}