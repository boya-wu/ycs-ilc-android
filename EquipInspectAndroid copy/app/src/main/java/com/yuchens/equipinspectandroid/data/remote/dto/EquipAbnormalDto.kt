package com.yuchens.equipinspectandroid.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EquipAbnormalDto(
    @SerialName("GUID") val guid: String?,
    @SerialName("AbnormalName") val abnormalName: String?,
    @SerialName("ItemId") val itemId: String?
)