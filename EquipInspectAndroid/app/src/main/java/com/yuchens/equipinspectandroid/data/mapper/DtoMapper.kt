package com.yuchens.equipinspectandroid.data.mapper

import com.yuchens.equipinspectandroid.data.local.entity.EquipEntity
import com.yuchens.equipinspectandroid.data.remote.dto.EquipDto


fun EquipEntity.toUploadDto(): EquipDto =
    EquipDto(
        guid = this.guid,
        barcodeId = this.barcodeId,
        barcode = this.barcode,
        replaceMedicineDate = this.replaceMedicineDate,
        categoryName = this.categoryName,
        itemId = this.itemId,
        itemName = this.itemName,
        buildingName = this.buildingName,
        areaName = this.areaName,
        abnormal = this.abnormal,
        taskId = this.taskId,
        enable = this.enable,
        insertUser = this.insertUser,
        insertTime = this.insertTime,
        updateUser = this.updateUser,
        updateTime = this.updateTime
    )