package com.xapics.data

import com.xapics.data.models.User

interface UserDataSource {
    suspend fun insertUser(user: User): Boolean
    suspend fun getUserByUsername(username: String): User?
}