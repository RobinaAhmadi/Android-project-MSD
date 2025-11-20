package com.example.android_project_msd.profile

// Combined UI state for the profile screen.
data class ProfileState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",

    val newPassword: String = "",

    val alertOnNewPayment: Boolean = false,
    val alertOnMissingPayment: Boolean = false,

    // URL to the profile image stored in Firebase Storage (can be null/blank)
    val profileImageUrl: String? = null,

    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isUploadingImage: Boolean = false
)
