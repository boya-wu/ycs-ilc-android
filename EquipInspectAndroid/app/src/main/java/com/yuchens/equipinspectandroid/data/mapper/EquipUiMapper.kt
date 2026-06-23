// data/mapper/EquipMappers.kt
package com.yuchens.equipinspectandroid.data.mapper

import com.yuchens.equipinspectandroid.data.local.entity.EquipEntity
import com.yuchens.equipinspectandroid.ui.model.EquipUi

fun EquipEntity.toUi() = EquipUi(
    guid = guid,
    barcode = barcode,
    buildingName = buildingName,
    areaName = areaName,
    categoryName = categoryName,
    itemName = itemName,

    itemId = itemId,
    abnormal = abnormal,
    replaceMedicineDate = replaceMedicineDate,

    inspected = !updateUser.isNullOrBlank()
)

fun List<EquipEntity>.toUiList(): List<EquipUi> = map { it.toUi() }
