package com.xapics.data.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)
