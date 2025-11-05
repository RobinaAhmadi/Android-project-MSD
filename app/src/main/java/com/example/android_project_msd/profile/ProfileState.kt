package com.example.android_project_msd.profile

// En data class til at holde den samlede tilstand for vores skærm.
// Dette er "Model" i MVVM og vores "Single Source of Truth".
data class ProfileState(
    // Data fra din lo-fi
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    // Vi gemmer ikke det rigtige kodeord, kun det brugeren evt. indtaster
    val newPassword: String = "",

    // Notifikationsindstillinger
    val alertOnNewPayment: Boolean = false,
    val alertOnMissingPayment: Boolean = false,

    // UI-tilstand: Er vi i "Edit" mode?
    val isEditing: Boolean = false,

    // Viser en "Loading" spinner, mens vi gemmer
    val isLoading: Boolean = false
)