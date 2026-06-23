package com.yuchens.equipinspectandroid.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserDto(
    @SerialName("GUID") val guid: String?,
    @SerialName("UserNo") val userNo: String?,
    @SerialName("UserPassword") val userPassword: String?,
    @SerialName("IntervalLimit") val intervalLimit: Boolean?
)