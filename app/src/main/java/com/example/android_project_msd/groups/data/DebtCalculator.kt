package com.example.android_project_msd.groups.data

import kotlin.math.abs
import kotlin.math.min

/**
 * Represents a debt between two people
 */
data class Debt(
    val fromPerson: String,  // Person who owes money
    val toPerson: String,    // Person who is owed money
    val amount: Double       // Amount owed
)

/**
 * Represents a settlement suggestion
 */
data class Settlement(
    val fromPerson: String,
    val toPerson: String,
    val amount: Double
)

/**
 * DebtCalculator handles all debt-related calculations for a group.
 * It calculates balances, determines who owes whom, and suggests optimal settlements.
 */
object DebtCalculator {

    /**
     * Calculate individual balances from a list of expenses.
     * Positive balance = person is owed money
     * Negative balance = person owes money
     */
    fun calculateBalances(
        expenses: List<Expense>,
        members: List<String>
    ): Map<String, Double> {
        val balances = members.associateWith { 0.0 }.toMutableMap()

        expenses.forEach { expense ->
            // Determine who should split the expense
            val participants = if (expense.splitAmong.isEmpty()) {
                members
            } else {
                expense.splitAmong
            }

            if (participants.isEmpty()) return@forEach

            // Calculate share per person
            val sharePerPerson = expense.amount / participants.size

            // Person who paid gets credited the full amount
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount

            // Each participant gets debited their share
            participants.forEach { participant ->
                balances[participant] = (balances[participant] ?: 0.0) - sharePerPerson
            }
        }

        // Round to 2 decimal places
        return balances.mapValues { (_, balance) ->
            (balance * 100).toLong() / 100.0
        }
    }

    /**
     * Calculate optimal settlement plan to minimize number of transactions.
     * This uses a greedy algorithm to match the largest creditor with the largest debtor.
     */
    fun calculateSettlements(balances: Map<String, Double>): List<Settlement> {
        val settlements = mutableListOf<Settlement>()

        // Separate creditors (positive balance) and debtors (negative balance)
        val creditors = balances.filter { it.value > 0.01 }
            .toMutableMap()
        val debtors = balances.filter { it.value < -0.01 }
            .mapValues { abs(it.value) }
            .toMutableMap()

        // Match largest creditor with largest debtor repeatedly
        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            // Find person owed the most
            val maxCreditor = creditors.maxByOrNull { it.value }!!
            // Find person who owes the most
            val maxDebtor = debtors.maxByOrNull { it.value }!!

            // Calculate settlement amount (minimum of what's owed and what's due)
            val settlementAmount = min(maxCreditor.value, maxDebtor.value)

            settlements.add(
                Settlement(
                    fromPerson = maxDebtor.key,
                    toPerson = maxCreditor.key,
                    amount = (settlementAmount * 100).toLong() / 100.0
                )
            )

            // Update balances
            creditors[maxCreditor.key] = maxCreditor.value - settlementAmount
            debtors[maxDebtor.key] = maxDebtor.value - settlementAmount

            // Remove if balance is settled (within 0.01 threshold)
            if (creditors[maxCreditor.key]!! < 0.01) {
                creditors.remove(maxCreditor.key)
            }
            if (debtors[maxDebtor.key]!! < 0.01) {
                debtors.remove(maxDebtor.key)
            }
        }

        return settlements
    }

    /**
     * Calculate individual debts (who owes whom specifically).
     * This is useful for showing detailed debt relationships.
     */
    fun calculateDetailedDebts(balances: Map<String, Double>): List<Debt> {
        val settlements = calculateSettlements(balances)
        return settlements.map { settlement ->
            Debt(
                fromPerson = settlement.fromPerson,
                toPerson = settlement.toPerson,
                amount = settlement.amount
            )
        }
    }

    /**
     * Process a payment between two people and return updated balances.
     * This is used when someone settles their debt.
     */
    fun processPayment(
        currentBalances: Map<String, Double>,
        fromPerson: String,
        toPerson: String,
        amount: Double
    ): Map<String, Double> {
        val newBalances = currentBalances.toMutableMap()

        // Person making payment increases their balance (reduces their debt)
        newBalances[fromPerson] = (newBalances[fromPerson] ?: 0.0) + amount

        // Person receiving payment decreases their balance (reduces what they're owed)
        newBalances[toPerson] = (newBalances[toPerson] ?: 0.0) - amount

        // Round to 2 decimal places
        return newBalances.mapValues { (_, balance) ->
            (balance * 100).toLong() / 100.0
        }
    }

    /**
     * Get a summary of how much a specific person owes or is owed in total.
     */
    fun getPersonTotalBalance(
        balances: Map<String, Double>,
        personName: String
    ): Double {
        return balances[personName] ?: 0.0
    }

    /**
     * Check if all debts in the group are settled.
     */
    fun areAllDebtsSettled(balances: Map<String, Double>): Boolean {
        return balances.values.all { abs(it) < 0.01 }
    }
}

/**
 * Data class for Expense (moving from GroupDetailViewModel to shared location)
 */
data class Expense(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val splitAmong: List<String> = emptyList(),
    val date: String = "",
    val createdAt: Long = 0L
)
