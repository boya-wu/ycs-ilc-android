package com.yuchens.equipinspectandroid.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadBody(
    @SerialName("liEquipDetail") val liEquipDetail: List<EquipDto>
)
