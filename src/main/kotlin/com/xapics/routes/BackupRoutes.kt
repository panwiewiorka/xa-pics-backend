package com.xapics.routes

import com.xapics.data.models.TheString
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.backup() {
    authenticate {
        get("v1/backup/get") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName != "admin") {
                call.respond(HttpStatusCode.Forbidden)
            } else {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = TheString("backup/latest.zip")
                )
            }
        }

        post("v1/backup/make") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName != "admin") {
                call.respond(HttpStatusCode.Forbidden)
            } else {
                val result = executeFile("pathToScript.sh")
                call.respondText(result)
            }
        }

        post("v1/backup/restore") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)

            if (userName != "admin") {
                call.respond(HttpStatusCode.Forbidden)
            } else {
                val result = executeFile("pathToScript.sh")
                call.respondText(result)
            }
        }
    }
}


fun executeFile(requestBody: String): String {
    // Use ProcessBuilder to execute the file
    val process = ProcessBuilder(requestBody).start()

    // Read the output from the process
    val output = process.inputStream.bufferedReader().readText()

    // Wait for the process to finish and get the exit code
    val exitCode = process.waitFor()

    return "Execution finished with exit code $exitCode\nOutput:\n$output"
}