package com.example.android_project_msd.data

import android.util.Log
import com.example.android_project_msd.groups.data.Expense
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    suspend fun addExpense(groupId: String, expense: Expense): Result<Unit> {
        return try {
            val expenseRef = groupsCollection
                .document(groupId)
                .collection("expenses")
                .document(expense.id)

            expenseRef.set(expense.toFirestoreMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Failed to add expense: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun observeExpenses(groupId: String): Flow<List<Expense>> = callbackFlow {
        val registration = groupsCollection
            .document(groupId)
            .collection("expenses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ExpenseRepository", "Expense listener error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val expenses = snapshot.documents.mapNotNull { it.toExpense() }

                trySend(expenses)
            }

        awaitClose { registration.remove() }
    }

    suspend fun getExpensesOnce(groupId: String): Result<List<Expense>> {
        return try {
            val snapshot = groupsCollection
                .document(groupId)
                .collection("expenses")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            Result.success(snapshot.documents.mapNotNull { it.toExpense() })
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Failed to fetch expenses: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toExpense(): Expense? {
        val data = data ?: return null
        val description = data["description"] as? String ?: return null
        val amount = (data["amount"] as? Number)?.toDouble() ?: return null
        val paidBy = data["paidBy"] as? String ?: return null
        val splitAmong = (data["splitAmong"] as? List<*>)?.filterIsInstance<String>()
            ?: emptyList()
        val date = data["date"] as? String ?: ""
        val createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L

        return Expense(
            id = data["id"] as? String ?: id,
            description = description,
            amount = amount,
            paidBy = paidBy,
            splitAmong = splitAmong,
            date = date,
            createdAt = createdAt
        )
    }

    private fun Expense.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "description" to description,
            "amount" to amount,
            "paidBy" to paidBy,
            "splitAmong" to splitAmong,
            "date" to date,
            "createdAt" to createdAt
        )
    }
}
