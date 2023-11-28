package com.xapics.routes

import com.xapics.data.PicEntity
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun PartData.FileItem.save(path: String, fileName: String): String {
    val fileBytes = streamProvider().readBytes()
    val folder = File(path)
    folder.mkdirs()
    File("$path$fileName").writeBytes(fileBytes)
    return fileName
}

fun Route.uploadFile() {
    post("file") {
        val multipart = call.receiveMultipart()
        var path = "build/resources/main/static/images/"
        var fileName = ""
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
//                    PicEntity.new {
//                        year = 2023
//                        description = "House gate"
//                        imageUrl = "$BASE_URL/pics/09ektachrome/01.jpg"
//                        rollTitle = ekta
//                    }
                    if(part.name == "metadataPath") path = "$path${part.value}" else fileName = part.value
                }
                is PartData.FileItem -> {
                    if(part.name == "image") {
                        part.save(path, "${fileName}.jpg")
                    }
                }
                else -> Unit
            }
        }
        call.respond(HttpStatusCode.OK)
    }
}