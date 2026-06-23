package com.yuchens.equipinspectandroid.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class EquipDto(
    @SerialName("GUID") val guid: String?,
    @SerialName("BarcodeId") val barcodeId: String?,
    @SerialName("Barcode") val barcode: String?,
    @SerialName("ReplaceMedicineDate") val replaceMedicineDate: String?,
    @SerialName("CategoryName") val categoryName: String?,
    @SerialName("ItemId") val itemId: String?,
    @SerialName("ItemName") val itemName: String?,
    @SerialName("BuildingName") val buildingName: String?,
    @SerialName("AreaName") val areaName: String?,
    @SerialName("Abnormal") val abnormal: String?,
    @SerialName("TaskId") val taskId: String?,
    @SerialName("Enable") val enable: Boolean?,
    @SerialName("InsertUser") val insertUser: String?,
    @SerialName("InsertTime") val insertTime: String?,
    @SerialName("UpdateUser") val updateUser: String?,
    @SerialName("UpdateTime") val updateTime: String?
)