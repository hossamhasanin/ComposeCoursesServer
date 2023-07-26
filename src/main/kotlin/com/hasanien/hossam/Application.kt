package com.hasanien.hossam

import com.hasanien.hossam.data.user.UserDataSourceImpl
import io.ktor.server.application.*
import com.hasanien.hossam.plugins.*
import com.hasanien.hossam.security.hashing.SHA256HashingService
import com.hasanien.hossam.security.token.JWTtokenService
import com.hasanien.hossam.security.token.TokenConfig
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val dbPassword = System.getenv("MONGO_PW")
    val dbName = "coursesApp"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://hossamhasanin7:$dbPassword@cluster0.ycgxoyt.mongodb.net/$dbName?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)
    val userDataSource = UserDataSourceImpl(db)
    val tokenService = JWTtokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expireIn = 365L * 24L * 60L * 60L * 1000L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSecurity(tokenConfig)
    configureSerialization()
    configureRouting(userDataSource, tokenService, hashingService, tokenConfig)
}
