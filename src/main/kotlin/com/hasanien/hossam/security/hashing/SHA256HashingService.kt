package com.hasanien.hossam.security.hashing

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom

class SHA256HashingService: HashingService {
    override fun generateHashWithSalt(value: String, saltLength: Int): SlatedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltAsHex = Hex.encodeHexString(salt)
        val hash = DigestUtils.sha256Hex("$saltAsHex$value")
        return SlatedHash(hash= hash, salt = saltAsHex)
    }

    override fun verifyHashWithSalt(value: String, saltedHash: SlatedHash): Boolean {
        return DigestUtils.sha256Hex("${saltedHash.salt}$value") == saltedHash.hash
    }
}