package com.yuchens.equipinspectandroid.di

import android.content.Context
import com.yuchens.equipinspectandroid.data.local.AppDatabase
import com.yuchens.equipinspectandroid.data.local.DatabaseProvider
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        DatabaseProvider.get(ctx)

    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideEquipDao(db: AppDatabase) = db.equipDao()

    @Provides
    fun provideAbnormalDao(db: AppDatabase) = db.equipAbnormalDao()

    @Provides
    @Singleton
    fun provideLocalRepository(db: AppDatabase): LocalRepository =
        LocalRepository(db)
}
