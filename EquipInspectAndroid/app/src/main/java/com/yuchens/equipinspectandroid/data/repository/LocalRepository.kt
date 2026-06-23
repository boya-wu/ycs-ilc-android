package com.yuchens.equipinspectandroid.data.repository

import androidx.room.withTransaction
import com.yuchens.equipinspectandroid.data.local.AppDatabase
import com.yuchens.equipinspectandroid.data.local.entity.EquipEntity
import com.yuchens.equipinspectandroid.data.local.entity.UserEntity
import com.yuchens.equipinspectandroid.data.mapper.toEntity
import com.yuchens.equipinspectandroid.data.remote.dto.DownloadDto
import com.yuchens.equipinspectandroid.util.AESHelper
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LocalRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val equipDao = db.equipDao()
    private val abnormalDao = db.equipAbnormalDao()

    suspend fun validateUser(userNo: String, plainPassword: String): UserEntity? {
        val encrypted = AESHelper.encrypt(plainPassword)
        return userDao.validateUser(userNo, encrypted)
    }

    suspend fun findAllByKeyPreferGuidFirst(key: String): List<EquipEntity> =
        equipDao.findByGuid(key) + equipDao.findByBarcodeExceptGuid(key)

    suspend fun getEquipAbnormalByItem(itemId: String) = abnormalDao.getByItemId(itemId)

    suspend fun updateEquipAbnormal(
        guid: String,
        abnormal: String?,
        updateUser: String?,
        updateTime: String?
    ): Boolean {
        return equipDao.updateAbnormalInfo(guid, abnormal, updateUser, updateTime) > 0
    }

    suspend fun getEquipUninspected() = equipDao.getUninspected()

    suspend fun getEquipInspected() = equipDao.getInspected()

    suspend fun replaceAllFromDownload(dto: DownloadDto) {
        db.withTransaction {
            // 先全部清掉
            userDao.deleteAllExceptAdmin()
            equipDao.deleteAll()
            abnormalDao.deleteAll()

            // 再插入
            userDao.insertAll(dto.liUserDetail.map { it.toEntity() })
            equipDao.insertAll(dto.liEquipDetail.map { it.toEntity() })
            abnormalDao.insertAll(dto.liEquipAbnormalDetail.map { it.toEntity() })
        }
    }

    suspend fun deleteEquipByGuids(guids: List<String>) = equipDao.deleteByGuids(guids)
}
