package com.example.server.db.tables

import org.jetbrains.exposed.sql.Table
import java.util.UUID

object Users : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 255).nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)

    fun newId(): UUID = UUID.randomUUID()
}
