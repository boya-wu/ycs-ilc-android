package com.yuchens.equipinspectandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equip_abnormal")
data class EquipAbnormalEntity(
    @PrimaryKey val guid: String,
    val abnormalName: String?,
    val itemId: String?
)
