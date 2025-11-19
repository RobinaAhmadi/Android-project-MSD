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
     * TEST FUNCTION - Verify Firebase connection
     */
    suspend fun testFirebaseConnection(): Result<String> {
        return try {
            Log.d("NotificationRepo", "🧪 Testing Firebase connection...")

            val testDoc = notificationsCollection.document("test_doc")
            val testData = mapOf(
                "test" to "Hello Firebase!",
                "timestamp" to Timestamp.now()
            )

            testDoc.set(testData).await()

            Log.d("NotificationRepo", "✅ Test document written successfully!")
            Result.success("Firebase connection OK")
        } catch (e: Exception) {
            Log.e("NotificationRepo", "❌ Test failed: ${e.message}", e)
            Result.failure(e)
        }
    }

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
            Log.d("NotificationRepo", "🚀 STEP 1: Creating invitation from $fromUserId to $toEmail for group $groupId")

            // Get sender info
            Log.d("NotificationRepo", "🚀 STEP 2: Fetching sender info for userId: $fromUserId")
            val sender = usersCollection.document(fromUserId).get().await()
                .toObject(User::class.java)
                ?: throw Exception("Sender not found")
            Log.d("NotificationRepo", "✅ STEP 2 SUCCESS: Sender found: ${sender.name}")

            // Find recipient by email
            Log.d("NotificationRepo", "🚀 STEP 3: Searching for recipient with email: $toEmail")
            val recipientQuery = usersCollection
                .whereEqualTo("email", toEmail.trim())
                .get()
                .await()

            Log.d("NotificationRepo", "Query returned ${recipientQuery.documents.size} documents")

            if (recipientQuery.isEmpty) {
                throw Exception("No user found with email: $toEmail")
            }

            val recipient = recipientQuery.documents.first().toObject(User::class.java)
                ?: throw Exception("Failed to parse recipient data")
            Log.d("NotificationRepo", "✅ STEP 3 SUCCESS: Recipient found: ${recipient.name} (ID: ${recipient.id})")

            // Create invitation
            Log.d("NotificationRepo", "🚀 STEP 4: Creating invitation document reference")
            val invitationRef = notificationsCollection.document()
            Log.d("NotificationRepo", "Document reference created with ID: ${invitationRef.id}")

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

            Log.d("NotificationRepo", "🚀 STEP 5: Writing invitation to Firestore...")
            Log.d("NotificationRepo", "Path: notifications/${invitation.id}")
            Log.d("NotificationRepo", "Data: toUserId=${invitation.toUserId}, status=${invitation.status}")

            invitationRef.set(invitation).await()

            Log.d("NotificationRepo", "✅✅✅ STEP 5 SUCCESS: Invitation written to Firestore!")
            Log.d("NotificationRepo", "✅ Complete path: notifications/${invitation.id}")

            Result.success(invitation)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "❌❌❌ ERROR creating invitation: ${e.message}", e)
            Log.e("NotificationRepo", "❌ Exception type: ${e.javaClass.simpleName}")
            Log.e("NotificationRepo", "❌ Stack trace: ${e.stackTraceToString()}")
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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Error listening to invitations: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("NotificationRepo", "Snapshot is null")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                Log.d("NotificationRepo", "Snapshot received with ${snapshot.documents.size} documents")

                val invitations = snapshot.documents.mapNotNull { doc ->
                    Log.d("NotificationRepo", "Processing document ${doc.id}: ${doc.data}")
                    try {
                        val invitation = doc.toObject(GroupInvitation::class.java)
                        if (invitation != null) {
                            Log.d("NotificationRepo", "Parsed invitation: fromUser=${invitation.fromUserName}, toUser=${invitation.toUserId}, status=${invitation.status}")
                        } else {
                            Log.w("NotificationRepo", "Failed to parse invitation from doc ${doc.id}")
                        }
                        invitation
                    } catch (e: Exception) {
                        Log.e("NotificationRepo", "Error parsing invitation doc ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                Log.d("NotificationRepo", "Received ${invitations.size} invitations for user $userId")
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

