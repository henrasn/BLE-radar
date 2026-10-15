package com.example.ble_radar.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ble_devices")
data class BleDeviceEntity(
  @PrimaryKey
  val macAddress: String,
  val name: String,
  val rssi: Int,
  val estimatedDistance: String,
  val lastSeenTimestamp: Long,
)
