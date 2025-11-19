package com.example.android_project_msd.data

data class User(
    val id: String = "", //string in firebase
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val cardHolderName: String = "",
    val cardNumber: String = "",
    val expiryDate: String = "",
    val cvv: String = ""
)
