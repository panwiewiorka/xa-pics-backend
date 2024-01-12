package com.xapics.data

import com.xapics.data.DatabaseFactory.dbQuery
import com.xapics.data.models.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserDataSourceImpl: UserDataSource {

    override suspend fun insertUser(user: User): Boolean {
        var isSuccess = false
        transaction {
            if (UserEntity.find { Users.username eq user.username }.firstOrNull() == null) {
                UserEntity.new {
                    username = user.username
                    password = user.password
                    salt = user.salt
                }
                isSuccess = true
            }
        }
        return isSuccess
    }

    override suspend fun getUserByUsername(username: String): User? {
        val query = Users.select { Users.username eq username }
        return dbQuery {
            query.map {
                User(
                    it[Users.username],
                    it[Users.password],
                    it[Users.salt],
                    it[Users.id].value
                )
            }.firstOrNull()
        }
    }

}