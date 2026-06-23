package com.yuchens.equipinspectandroid.ui.model

data class EquipUi(
    val guid: String?,
    val barcode: String?,
    val buildingName: String?,
    val areaName: String?,
    val categoryName: String?,
    val itemName: String?,

    // 給 InspectFragment 用
    val itemId: String?,
    val abnormal: String?,
    val replaceMedicineDate: String?,

    // 通用旗標
    val inspected: Boolean
)
