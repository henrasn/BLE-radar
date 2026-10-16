package com.example.ble_radar.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BleDeviceDao {

  @Upsert
  suspend fun upsert(device: BleDeviceEntity)

  @Upsert
  suspend fun upsertAll(devices: List<BleDeviceEntity>)

  @Query("SELECT * FROM ble_devices ORDER BY lastSeenTimestamp DESC")
  fun getAllDevices(): Flow<List<BleDeviceEntity>>

  @Query("SELECT * FROM ble_devices WHERE macAddress = :macAddress")
  suspend fun getDeviceByMac(macAddress: String): BleDeviceEntity?

  @Query("SELECT * FROM ble_devices WHERE macAddress = :macAddress")
  fun observeDeviceByMac(macAddress: String): Flow<BleDeviceEntity?>

  @Query("SELECT COUNT(*) FROM ble_devices")
  fun getDeviceCount(): Flow<Int>

  @Query("DELETE FROM ble_devices")
  suspend fun clearAll()
}
