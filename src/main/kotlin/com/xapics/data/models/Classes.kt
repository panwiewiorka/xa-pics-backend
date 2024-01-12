package com.xapics.data.models

import kotlinx.serialization.Serializable

const val BASE_URL = "http://192.168.0.87:8080"
//const val BASE_URL = "http://0.0.0.0:8080"

@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String
)

@Serializable
data class User(
    val username: String,
    val password: String,
    val salt: String,
    val id: Int? = null
)

@Serializable
data class Film(
    val filmName: String = "",
    val iso: Int? = null,
    val type: FilmType = FilmType.NULL,
    val xpro: Boolean = false,
    val expired: Boolean = false,
)

@Serializable
data class Roll(
    val title: String,
    val film: String,
//    val date: Date,
    val nonXa: Boolean = false,
)

@Serializable
data class Pic(
    val id: Int,
    val year: Int,
    val description: String,
    val imageUrl: String,
    val tags: String,
    val film: String,
//    val collections: String? = null,
)

enum class FilmType { SLIDE, NEGATIVE, BW, NULL }