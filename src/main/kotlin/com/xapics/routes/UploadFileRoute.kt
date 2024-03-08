package com.xapics.routes

import com.xapics.data.PicEntity
import com.xapics.data.RollEntity
import com.xapics.data.Rolls
import com.xapics.data.models.BASE_URL
import com.xapics.routes.authenticate
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File

fun PartData.FileItem.save(path: String, fileName: String): String {
    val fileBytes = streamProvider().readBytes()
    val folder = File(path)
    folder.mkdirs()
    File("$path$fileName").writeBytes(fileBytes)
    return fileName
}

fun Route.uploadFile() {
    val log = LoggerFactory.getLogger(this.javaClass)

    authenticate {
        post("v1/file") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName == "admin") {
                val multipart = call.receiveMultipart()
                val path = "build/resources/main/static/images/"
                var rollTitle = ""
                var theDescription = ""
                var theYear = ""
                var theHashtags = ""
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            when (part.name) {
                                "roll" -> rollTitle = part.value
                                "description" -> theDescription = part.value
                                "year" -> theYear = part.value
                                "hashtags" -> theHashtags = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            if(part.name == "image") {
                                transaction {
                                    val theRoll = RollEntity.find { Rolls.title eq rollTitle }.firstOrNull()
                                    theRoll?.let {
                                        val filePath = String.format("%02d-${theRoll.film.filmName}_$rollTitle/", theRoll.index)
                                        val fileName = String.format("%02d.jpg", theRoll.frames.count() + 1)
                                        part.save(path + filePath, fileName)
                                        PicEntity.new {
                                            year = theYear.toInt()
                                            description = theDescription
                                            imageUrl = "/images/$filePath$fileName"
                                            hashtags = theHashtags
                                            roll = theRoll
                                        }
                                    }
                                }
                            }
                        }
                        else -> Unit
                    }
                }
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}