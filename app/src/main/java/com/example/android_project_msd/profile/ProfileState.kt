package com.example.android_project_msd.profile

// En data class til at holde den samlede tilstand for vores skærm.
// Dette er "Model" i MVVM og vores "Single Source of Truth".
data class ProfileState(

    val name: String = "",
    val phone: String = "",
    val email: String = "",

    val newPassword: String = "",


    val alertOnNewPayment: Boolean = false,
    val alertOnMissingPayment: Boolean = false,

    val isEditing: Boolean = false,

    val isLoading: Boolean = false
)