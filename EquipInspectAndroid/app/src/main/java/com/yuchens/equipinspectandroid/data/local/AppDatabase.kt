package com.yuchens.equipinspectandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yuchens.equipinspectandroid.data.local.dao.EquipAbnormalDao
import com.yuchens.equipinspectandroid.data.local.dao.EquipDao
import com.yuchens.equipinspectandroid.data.local.dao.UserDao
import com.yuchens.equipinspectandroid.data.local.entity.EquipAbnormalEntity
import com.yuchens.equipinspectandroid.data.local.entity.EquipEntity
import com.yuchens.equipinspectandroid.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EquipEntity::class,
        EquipAbnormalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun equipDao(): EquipDao
    abstract fun equipAbnormalDao(): EquipAbnormalDao
}
