package com.example.ble_radar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [BleDeviceEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class BleDatabase : RoomDatabase() {

  abstract fun bleDeviceDao(): BleDeviceDao

  companion object {
    @Volatile
    private var INSTANCE: BleDatabase? = null

    fun getDatabase(context: Context): BleDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BleDatabase::class.java,
          "ble_radar.db",
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
