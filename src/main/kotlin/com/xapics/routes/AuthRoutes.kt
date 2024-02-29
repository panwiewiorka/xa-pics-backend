package com.xapics.routes

import com.xapics.data.UserDataSource
import com.xapics.data.models.AuthRequest
import com.xapics.data.models.AuthResponse
import com.xapics.data.models.TheString
import com.xapics.data.models.User
import com.xapics.data.security.hashing.HashingService
import com.xapics.data.security.hashing.SaltedHash
import com.xapics.data.security.token.TokenClaim
import com.xapics.data.security.token.TokenConfig
import com.xapics.data.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        if(areFieldsBlank) {
            call.respond(HttpStatusCode.Conflict, "Please fill in the fields")
            return@post
        }
        val isPwTooShort = request.password.length < 6
        if(isPwTooShort) {
            call.respond(HttpStatusCode.Conflict, "Password is too short (< 6 symbols)")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )

        val wasAcknowledged = userDataSource.insertUser(user)

        if(!wasAcknowledged)  {
            call.respond(HttpStatusCode.Conflict, "This username is already taken")
            return@post
        }

        call.respond(HttpStatusCode.OK)

    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveOrNull<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if(user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            ),
            TokenClaim(
                name = "userName",
                value = user.username
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token,
                userId = user.username
            )
        )
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getUserInfo() {
    val log = LoggerFactory.getLogger(this.javaClass)
    authenticate {
        get("profile") {
            val principal = call.principal<JWTPrincipal>()
            val userName = principal?.getClaim("userName", String::class)
            log.debug("Route.getUserInfo(): userName = $userName")
            call.respond(
                status = HttpStatusCode.OK,
                message = TheString(userName.toString())
            )
            // Introduced TheString due to text userNames in regular String crashing frontend (but Strings like "111" weren't)
        }
    }
}