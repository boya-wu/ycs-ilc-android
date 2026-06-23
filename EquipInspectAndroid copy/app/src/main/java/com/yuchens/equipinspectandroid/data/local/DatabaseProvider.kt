package com.yuchens.equipinspectandroid.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yuchens.equipinspectandroid.util.AESHelper

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
        }
    }

    private fun build(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "EquipInspect_Android.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val guid = java.util.UUID.randomUUID().toString()
                    val enc = AESHelper.encrypt("0000")

                    db.compileStatement(
                        "INSERT INTO user (guid, userNo, userPassword, intervalLimit) VALUES (?, ?, ?, ?)"
                    ).apply {
                        bindString(1, guid)
                        bindString(2, "admin")
                        bindString(3, enc)
                        bindLong(4, 0L)
                        executeInsert()
                        close()
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }
}

