package com.example.ble_radar.di

import android.content.Context
import com.example.ble_radar.data.local.BleDatabase
import com.example.ble_radar.data.local.BleDeviceDao
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
  fun provideBleDatabase(
    @ApplicationContext context: Context,
  ): BleDatabase {
    return BleDatabase.getDatabase(context)
  }

  @Provides
  fun provideBleDeviceDao(database: BleDatabase): BleDeviceDao {
    return database.bleDeviceDao()
  }
}
