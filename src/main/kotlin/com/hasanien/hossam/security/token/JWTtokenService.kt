package com.hasanien.hossam.security.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JWTtokenService: TokenService {
    override fun generate(config: TokenConfig, vararg claims: TokenClaim): String {
        val token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expireIn))

        claims.forEach {
            token.withClaim(it.name, it.value)
        }

        return token.sign(Algorithm.HMAC256(config.secret))
    }
}