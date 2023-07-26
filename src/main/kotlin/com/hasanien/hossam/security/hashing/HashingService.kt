package com.hasanien.hossam.security.hashing

interface HashingService {
    fun generateHashWithSalt(value: String, saltLength: Int = 32): SlatedHash
    fun verifyHashWithSalt(value: String, saltedHash: SlatedHash): Boolean
}