package com.example.android_project_msd.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GroupRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")
    private val usersCollection = firestore.collection("users")

    /**
     * Create a new group
     */
    suspend fun createGroup(
        name: String,
        description: String,
        ownerId: String
    ): Result<Group> {
        return try {
            Log.d("GroupRepository", "Creating group: $name for owner: $ownerId")

            // Get owner info
            val owner = usersCollection.document(ownerId).get().await()
                .toObject(User::class.java)
                ?: throw Exception("Owner not found")

            // Create new group document with auto-generated ID
            val groupRef = groupsCollection.document()

            val group = Group(
                id = groupRef.id,
                name = name,
                description = description,
                ownerId = ownerId,
                ownerEmail = owner.email,
                ownerName = owner.name,
                members = listOf(ownerId), // Owner is automatically a member
                memberEmails = listOf(owner.email),
                memberNames = listOf(owner.name),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            groupRef.set(group).await()
            Log.d("GroupRepository", "Group created successfully with ID: ${group.id}")

            Result.success(group)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error creating group: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all groups where user is a member
     */
    suspend fun getGroupsForUser(userId: String): List<Group> {
        return try {
            Log.d("GroupRepository", "Fetching groups for user: $userId")

            val querySnapshot = groupsCollection
                .whereArrayContains("members", userId)
                .get()
                .await()

            val groups = querySnapshot.documents.mapNotNull {
                it.toObject(Group::class.java)
            }

            Log.d("GroupRepository", "Found ${groups.size} groups for user")
            groups
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error fetching groups: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get a specific group by ID
     */
    suspend fun getGroupById(groupId: String): Group? {
        return try {
            val document = groupsCollection.document(groupId).get().await()
            document.toObject(Group::class.java)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error fetching group: ${e.message}", e)
            null
        }
    }

    /**
     * Add a member to a group (by email)
     */
    suspend fun addMemberToGroup(groupId: String, memberEmail: String): Result<Unit> {
        return try {
            Log.d("GroupRepository", "Adding member $memberEmail to group $groupId")

            // Find user by email
            val userQuery = usersCollection
                .whereEqualTo("email", memberEmail)
                .get()
                .await()

            if (userQuery.isEmpty) {
                throw Exception("No user found with email: $memberEmail")
            }

            val user = userQuery.documents.first().toObject(User::class.java)
                ?: throw Exception("Failed to parse user data")

            // Get current group
            val group = getGroupById(groupId) ?: throw Exception("Group not found")

            // Check if user is already a member
            if (group.members.contains(user.id)) {
                throw Exception("User is already a member of this group")
            }

            // Update group with new member
            val updatedMembers = group.members + user.id
            val updatedEmails = group.memberEmails + user.email
            val updatedNames = group.memberNames + user.name

            groupsCollection.document(groupId).update(
                mapOf(
                    "members" to updatedMembers,
                    "memberEmails" to updatedEmails,
                    "memberNames" to updatedNames,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Log.d("GroupRepository", "Member added successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error adding member: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove a member from a group
     */
    suspend fun removeMemberFromGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val group = getGroupById(groupId) ?: throw Exception("Group not found")

            // Can't remove owner
            if (group.ownerId == userId) {
                throw Exception("Cannot remove the group owner")
            }

            val memberIndex = group.members.indexOf(userId)
            if (memberIndex == -1) {
                throw Exception("User is not a member of this group")
            }

            val updatedMembers = group.members.toMutableList().apply { removeAt(memberIndex) }
            val updatedEmails = group.memberEmails.toMutableList().apply { removeAt(memberIndex) }
            val updatedNames = group.memberNames.toMutableList().apply { removeAt(memberIndex) }

            groupsCollection.document(groupId).update(
                mapOf(
                    "members" to updatedMembers,
                    "memberEmails" to updatedEmails,
                    "memberNames" to updatedNames,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error removing member: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update group details (only owner can do this)
     */
    suspend fun updateGroup(groupId: String, name: String, description: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId).update(
                mapOf(
                    "name" to name,
                    "description" to description,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error updating group: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a group (only owner can do this)
     */
    suspend fun deleteGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val group = getGroupById(groupId) ?: throw Exception("Group not found")

            if (group.ownerId != userId) {
                throw Exception("Only the group owner can delete the group")
            }

            groupsCollection.document(groupId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GroupRepository", "Error deleting group: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if user is the owner of a group
     */
    suspend fun isGroupOwner(groupId: String, userId: String): Boolean {
        return try {
            val group = getGroupById(groupId)
            group?.ownerId == userId
        } catch (e: Exception) {
            false
        }
    }
}

