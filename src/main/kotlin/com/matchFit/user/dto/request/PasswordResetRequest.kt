package com.matchFit.user.dto.request


data class PasswordResetRequest(
    var email: String? = null,
    var token: String? = null,
    var newPassword: String? = null
)
