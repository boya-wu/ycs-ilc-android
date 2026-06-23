package com.yuchens.equipinspectandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuchens.equipinspectandroid.data.local.entity.EquipEntity

@Dao
interface EquipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<EquipEntity>)

    @Query("""
        SELECT * FROM equip 
        WHERE barcodeId = :barcode COLLATE NOCASE
    """)
    suspend fun findByGuid(barcode: String): List<EquipEntity>

    @Query("""
        SELECT * FROM equip 
        WHERE barcode = :barcode COLLATE NOCASE
          AND barcodeId <> :barcode COLLATE NOCASE
        ORDER BY buildingName, areaName, categoryName, itemName
    """)
    suspend fun findByBarcodeExceptGuid(barcode: String): List<EquipEntity>

    @Query("""
        SELECT * FROM equip
        WHERE updateUser IS NULL
        ORDER BY buildingName, areaName, categoryName, itemName, barcode
    """)
    suspend fun getUninspected(): List<EquipEntity>

    @Query("""
        SELECT * FROM equip
        WHERE updateUser IS NOT NULL
        ORDER BY buildingName, areaName, categoryName, itemName, barcode
    """)
    suspend fun getInspected(): List<EquipEntity>

    @Query("""
        UPDATE equip
        SET abnormal = :abnormal,
            updateUser = :updateUser,
            updateTime = :updateTime
        WHERE guid = :guid
    """)
    suspend fun updateAbnormalInfo(
        guid: String,
        abnormal: String?,
        updateUser: String?,
        updateTime: String?
    ): Int

    @Query("DELETE FROM equip WHERE guid IN (:guids)")
    suspend fun deleteByGuids(guids: List<String>): Int

    @Query("DELETE FROM equip")
    suspend fun deleteAll(): Int
}
