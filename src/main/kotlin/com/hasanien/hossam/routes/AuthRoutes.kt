package com.hasanien.hossam.routes

import com.hasanien.hossam.data.requests.AuthRequest
import com.hasanien.hossam.data.responses.AuthResponse
import com.hasanien.hossam.data.user.User
import com.hasanien.hossam.data.user.UserDataSource
import com.hasanien.hossam.routes.authenticate
import com.hasanien.hossam.security.hashing.HashingService
import com.hasanien.hossam.security.hashing.SlatedHash
import com.hasanien.hossam.security.token.TokenClaim
import com.hasanien.hossam.security.token.TokenConfig
import com.hasanien.hossam.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signup(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenConfig: TokenConfig,
    tokenService: TokenService
){
    post("/signup"){
        val authRequest = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = authRequest.username.isBlank() || authRequest.password.isBlank()
        val isPasswordShort = authRequest.password.length < 8
        if (areFieldsBlank || isPasswordShort){
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val saltedHash = hashingService.generateHashWithSalt(authRequest.password)
        val user = User(
            username = authRequest.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val isUserCreated = userDataSource.insertUser(user)
        if (isUserCreated) {

            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userId",
                    value = user.id.toString()
                ),
            )
            call.respond(HttpStatusCode.OK, AuthResponse(token))
        } else {
            call.respond(HttpStatusCode.Conflict)
        }
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
){
    post("/signin"){
        val authRequest = call.receiveNullable<AuthRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(authRequest.username)
        if (user == null){
            call.respond(HttpStatusCode.Conflict, "User not found")
            return@post
        }

        val isPasswordValid = hashingService.verifyHashWithSalt(
            value= authRequest.password,
            saltedHash = SlatedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isPasswordValid){
            call.respond(HttpStatusCode.Conflict, "Invalid password")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            ),
        )

        call.respond(HttpStatusCode.OK, AuthResponse(token))
    }
}

fun Route.authenticate(){
    authenticate {
        get("/authenticate"){
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId" , String::class)

            call.respond(HttpStatusCode.OK, "Authenticated user $userId")
        }
    }
}