package com.example.models

import java.io.Serializable

data class Account (
    var id: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var displayName: String = ""
): Serializable
