package com.yuchens.equipinspectandroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val guid: String,
    val userNo: String?,
    val userPassword: String?,
    val intervalLimit: Boolean?,
)
