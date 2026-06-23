package com.yuchens.equipinspectandroid.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiEnvelope<T>(
    @SerialName("Data") val data: T? = null,
    @SerialName("Success") val success: Boolean = false,
    @SerialName("Message") val message: String? = null
)
