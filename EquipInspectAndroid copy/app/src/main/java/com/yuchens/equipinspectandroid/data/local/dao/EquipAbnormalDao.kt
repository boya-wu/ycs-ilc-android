package com.yuchens.equipinspectandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuchens.equipinspectandroid.data.local.entity.EquipAbnormalEntity

@Dao
interface EquipAbnormalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<EquipAbnormalEntity>)

    @Query("SELECT * FROM equip_abnormal WHERE itemId = :itemId")
    suspend fun getByItemId(itemId: String): List<EquipAbnormalEntity>

    @Query("DELETE FROM equip_abnormal")
    suspend fun deleteAll(): Int
}
