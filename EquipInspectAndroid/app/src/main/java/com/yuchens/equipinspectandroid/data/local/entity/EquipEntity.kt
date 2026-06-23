package com.yuchens.equipinspectandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equip")
data class EquipEntity(
    @PrimaryKey val guid: String,
    val barcodeId: String?,
    val barcode: String?,
    val replaceMedicineDate: String?,
    val categoryName: String?,
    val itemId: String?,
    val itemName: String?,
    val buildingName: String?,
    val areaName: String?,
    val abnormal: String?,
    val taskId: String?,
    val enable: Boolean?,
    val insertUser: String?,
    val insertTime: String?,
    val updateUser: String?,
    val updateTime: String?
)
