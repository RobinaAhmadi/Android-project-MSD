package com.example.server.db

import com.example.server.db.tables.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.UUID

data class UserRecord(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val displayName: String?
)

class UserDao {
    suspend fun createUser(email: String, passwordHash: String, displayName: String?): UserRecord? =
        DatabaseFactory.dbQuery {
            val existing = Users
                .selectAll()
                .andWhere { Users.email eq email }
                .singleOrNull()
            if (existing != null) return@dbQuery null

            val id = Users.newId()
            Users.insert {
                it[Users.id] = id
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.displayName] = displayName
                it[Users.createdAt] = Instant.now().toEpochMilli()
            }
            UserRecord(id, email, passwordHash, displayName)
        }

    suspend fun findByEmail(email: String): UserRecord? =
        DatabaseFactory.dbQuery {
            Users
                .selectAll()
                .andWhere { Users.email eq email }
                .singleOrNull()
                ?.toUser()
        }

    private fun ResultRow.toUser() = UserRecord(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        displayName = this[Users.displayName]
    )
}
