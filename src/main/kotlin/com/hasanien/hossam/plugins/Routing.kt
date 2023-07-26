package com.hasanien.hossam.plugins

import com.hasanien.hossam.data.user.UserDataSource
import com.hasanien.hossam.routes.authenticate
import com.hasanien.hossam.routes.signIn
import com.hasanien.hossam.routes.signup
import com.hasanien.hossam.security.hashing.HashingService
import com.hasanien.hossam.security.token.TokenConfig
import com.hasanien.hossam.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    tokenService: TokenService,
    hashingService: HashingService,
    tokenConfig: TokenConfig
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        signup(hashingService, userDataSource, tokenConfig, tokenService)
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        authenticate()
    }
}
