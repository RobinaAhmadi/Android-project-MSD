package com.example.android_project_msd.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationsCollection = firestore.collection("notifications")
    private val usersCollection = firestore.collection("users")
    private val groupsCollection = firestore.collection("groups")

    /**
     * Create a group invitation notification
     */
    suspend fun createGroupInvitation(
        fromUserId: String,
        toEmail: String,
        groupId: String,
        groupName: String,
        groupDescription: String
    ): Result<GroupInvitation> {
        return try {
            Log.d("NotificationRepo", "Creating invitation from $fromUserId to $toEmail for group $groupId")

            // Get sender info
            val sender = usersCollection.document(fromUserId).get().await()
                .toObject(User::class.java)
                ?: throw Exception("Sender not found")

            // Find recipient by email
            val recipientQuery = usersCollection
                .whereEqualTo("email", toEmail.trim())
                .get()
                .await()

            if (recipientQuery.isEmpty) {
                throw Exception("No user found with email: $toEmail")
            }

            val recipient = recipientQuery.documents.first().toObject(User::class.java)
                ?: throw Exception("Failed to parse recipient data")

            // Create invitation
            val invitationRef = notificationsCollection.document()
            val invitation = GroupInvitation(
                id = invitationRef.id,
                type = "GROUP_INVITATION",
                fromUserId = fromUserId,
                fromUserName = sender.name,
                fromUserEmail = sender.email,
                toUserId = recipient.id,
                toUserEmail = recipient.email,
                groupId = groupId,
                groupName = groupName,
                groupDescription = groupDescription,
                status = InvitationStatus.PENDING.name,
                message = "${sender.name} invited you to join $groupName",
                createdAt = Timestamp.now()
            )

            invitationRef.set(invitation).await()
            Log.d("NotificationRepo", "Invitation created successfully: ${invitation.id}")

            Result.success(invitation)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error creating invitation: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get all pending invitations for a user (real-time)
     */
    fun getMyInvitationsFlow(userId: String): Flow<List<GroupInvitation>> = callbackFlow {
        Log.d("NotificationRepo", "Setting up invitations listener for user: $userId")

        val listener = notificationsCollection
            .whereEqualTo("toUserId", userId)
            .whereEqualTo("status", InvitationStatus.PENDING.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Error listening to invitations: ${error.message}", error)
                    return@addSnapshotListener
                }

                val invitations = snapshot?.documents?.mapNotNull {
                    it.toObject(GroupInvitation::class.java)
                } ?: emptyList()

                Log.d("NotificationRepo", "Received ${invitations.size} invitations")
                trySend(invitations)
            }

        awaitClose {
            Log.d("NotificationRepo", "Removing invitations listener")
            listener.remove()
        }
    }

    /**
     * Get pending invitations (one-time fetch)
     */
    suspend fun getMyInvitations(userId: String): List<GroupInvitation> {
        return try {
            val querySnapshot = notificationsCollection
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", InvitationStatus.PENDING.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull {
                it.toObject(GroupInvitation::class.java)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error fetching invitations: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Respond to a group invitation (Accept or Decline)
     */
    suspend fun respondToInvitation(
        invitationId: String,
        response: InvitationStatus,
        userId: String
    ): Result<Unit> {
        return try {
            Log.d("NotificationRepo", "Responding to invitation $invitationId with $response")

            // Get invitation
            val invitationDoc = notificationsCollection.document(invitationId).get().await()
            val invitation = invitationDoc.toObject(GroupInvitation::class.java)
                ?: throw Exception("Invitation not found")

            // Verify user is the recipient
            if (invitation.toUserId != userId) {
                throw Exception("You are not authorized to respond to this invitation")
            }

            // Update invitation status
            notificationsCollection.document(invitationId).update(
                mapOf(
                    "status" to response.name,
                    "respondedAt" to Timestamp.now()
                )
            ).await()

            // If accepted, add user to group
            if (response == InvitationStatus.ACCEPTED) {
                val groupDoc = groupsCollection.document(invitation.groupId).get().await()
                val group = groupDoc.toObject(Group::class.java)
                    ?: throw Exception("Group not found")

                // Add user to group members
                val updatedMembers = group.members + userId
                val updatedEmails = group.memberEmails + invitation.toUserEmail
                val updatedNames = group.memberNames + invitation.fromUserName // Will be corrected

                // Get actual user name
                val user = usersCollection.document(userId).get().await()
                    .toObject(User::class.java)

                val actualUpdatedNames = if (user != null) {
                    group.memberNames + user.name
                } else {
                    updatedNames
                }

                groupsCollection.document(invitation.groupId).update(
                    mapOf(
                        "members" to updatedMembers,
                        "memberEmails" to updatedEmails,
                        "memberNames" to actualUpdatedNames,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()

                Log.d("NotificationRepo", "User added to group successfully")
            }

            Log.d("NotificationRepo", "Invitation response recorded: $response")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error responding to invitation: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get count of pending invitations
     */
    suspend fun getPendingInvitationsCount(userId: String): Int {
        return try {
            val querySnapshot = notificationsCollection
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", InvitationStatus.PENDING.name)
                .get()
                .await()

            querySnapshot.size()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error getting count: ${e.message}", e)
            0
        }
    }
}

