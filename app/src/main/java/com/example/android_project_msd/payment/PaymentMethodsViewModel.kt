package com.example.android_project_msd.payment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PaymentType {
    CARD,
    PAYPAL
}

data class PaymentMethod(
    val type: PaymentType,
    val brand: String,
    val primaryText: String,
    val secondaryText: String
)

data class PaymentMethodsUiState(
    val methods: List<PaymentMethod> = emptyList(),
    val showAddCardDialog: Boolean = false,
    val showAddPayPalDialog: Boolean = false,
    val selectedForOptions: PaymentMethod? = null
)

class PaymentMethodsViewModel : ViewModel() {

    private val _ui = MutableStateFlow(PaymentMethodsUiState())
    val ui = _ui.asStateFlow()

    fun showCardDialog(show: Boolean) {
        _ui.value = _ui.value.copy(showAddCardDialog = show)
    }

    fun showPayPalDialog(show: Boolean) {
        _ui.value = _ui.value.copy(showAddPayPalDialog = show)
    }

    fun selectForOptions(method: PaymentMethod?) {
        _ui.value = _ui.value.copy(selectedForOptions = method)
    }

    fun addCard(cardNumber: String, holderName: String) {
        val last4 = cardNumber.filter { it.isDigit() }.takeLast(4)
        if (last4.isEmpty()) return

        val newMethod = PaymentMethod(
            type = PaymentType.CARD,
            brand = "Card",
            primaryText = "Card •••• $last4",
            secondaryText = holderName.ifBlank { "Added card" }
        )

        _ui.value = _ui.value.copy(
            methods = _ui.value.methods + newMethod
        )
    }

    fun addPayPal(email: String) {
        if (email.isBlank()) return

        val newMethod = PaymentMethod(
            type = PaymentType.PAYPAL,
            brand = "PayPal",
            primaryText = "PayPal — $email",
            secondaryText = "Linked to your account"
        )

        _ui.value = _ui.value.copy(
            methods = _ui.value.methods + newMethod
        )
    }

    fun removeMethod(method: PaymentMethod) {
        _ui.value = _ui.value.copy(
            methods = _ui.value.methods.filterNot { it == method },
            selectedForOptions = null
        )
    }
}
