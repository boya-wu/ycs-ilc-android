package com.yuchens.equipinspectandroid.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class DownloadDto(
    @SerialName("liUserDetail") val liUserDetail: List<UserDto>,
    @SerialName("liEquipDetail") val liEquipDetail: List<EquipDto>,
    @SerialName("liEquipAbnormalDetail") val liEquipAbnormalDetail: List<EquipAbnormalDto>
)