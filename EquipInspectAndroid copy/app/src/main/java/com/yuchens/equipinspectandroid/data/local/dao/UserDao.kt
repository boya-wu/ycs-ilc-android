package com.yuchens.equipinspectandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yuchens.equipinspectandroid.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    // 刪除所有非 admin 使用者（等同你原本 deleteAll() 的條件）
    @Query("DELETE FROM user")
    suspend fun deleteAllExceptAdmin(): Int

    // 用 AES 加密後比對；這裡先比對明文，實務上你會先把密碼加密再傳進來
    @Query("""
        SELECT * FROM user
        WHERE userNo = :userNo AND userPassword = :encryptedPassword
        LIMIT 1
    """)
    suspend fun validateUser(userNo: String, encryptedPassword: String): UserEntity?
}
