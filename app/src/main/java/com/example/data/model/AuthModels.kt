package com.example.data.model

enum class AuthMethod {
    PHONE_OTP,
    EMAIL_OTP,
    GOOGLE
}

data class UserProfile(
    val id: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val authMethod: AuthMethod,
    val vehicleModel: String? = null,
    val vehicleHeight: String? = null,
    val vehicleWeight: String? = null,
    val licensePlate: String? = null,
    val memberSince: String = "October 2026",
    val isVerifiedCamper: Boolean = true
)
