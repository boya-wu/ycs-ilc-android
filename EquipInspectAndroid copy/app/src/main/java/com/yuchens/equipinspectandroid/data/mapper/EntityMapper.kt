package com.yuchens.equipinspectandroid.data.mapper

import com.yuchens.equipinspectandroid.data.local.entity.EquipAbnormalEntity
import com.yuchens.equipinspectandroid.data.local.entity.EquipEntity
import com.yuchens.equipinspectandroid.data.local.entity.UserEntity
import com.yuchens.equipinspectandroid.data.remote.dto.EquipAbnormalDto
import com.yuchens.equipinspectandroid.data.remote.dto.EquipDto
import com.yuchens.equipinspectandroid.data.remote.dto.UserDto

fun UserDto.toEntity() = UserEntity(
    guid = requireNotNull(guid) { "User GUID is null" },
    userNo = userNo,
    userPassword = userPassword,
    intervalLimit = intervalLimit
)

fun EquipDto.toEntity() = EquipEntity(
    guid = requireNotNull(guid) { "Equip GUID is null" },
    barcodeId = barcodeId,
    barcode = barcode,
    replaceMedicineDate = replaceMedicineDate,
    categoryName = categoryName,
    itemId = itemId,
    itemName = itemName,
    buildingName = buildingName,
    areaName = areaName,
    abnormal = abnormal,
    taskId = taskId,
    enable = enable,
    insertUser = insertUser,
    insertTime = insertTime,
    updateUser = updateUser,
    updateTime = updateTime
)

fun EquipAbnormalDto.toEntity() = EquipAbnormalEntity(
    guid = requireNotNull(guid) { "Abnormal GUID is null" },
    abnormalName = abnormalName,
    itemId = itemId
)
