package com.example.ble_radar.data.local

import com.example.ble_radar.data.model.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface BleLocalDataSource {
  fun getAllDevices(): Flow<List<DiscoveredDevice>>
  fun getDeviceCount(): Flow<Int>
  suspend fun saveDevice(device: DiscoveredDevice)
  suspend fun saveDevices(devices: List<DiscoveredDevice>)
  suspend fun getDevice(macAddress: String): DiscoveredDevice?
  fun observeDevice(macAddress: String): Flow<DiscoveredDevice?>
  suspend fun clearAll()
}

class BleLocalDataSourceImpl(
  private val dao: BleDeviceDao,
) : BleLocalDataSource {

  override fun getAllDevices(): Flow<List<DiscoveredDevice>> {
    return dao.getAllDevices().map { list ->
      list.map { it.toDiscoveredDevice() }
    }
  }

  override fun getDeviceCount(): Flow<Int> = dao.getDeviceCount()

  override suspend fun saveDevice(device: DiscoveredDevice) {
    dao.upsert(device.toEntity())
  }

  override suspend fun saveDevices(devices: List<DiscoveredDevice>) {
    dao.upsertAll(devices.map { it.toEntity() })
  }

  override suspend fun getDevice(macAddress: String): DiscoveredDevice? {
    return dao.getDeviceByMac(macAddress)?.toDiscoveredDevice()
  }

  override fun observeDevice(macAddress: String): Flow<DiscoveredDevice?> {
    return dao.observeDeviceByMac(macAddress).map { it?.toDiscoveredDevice() }
  }

  override suspend fun clearAll() {
    dao.clearAll()
  }
}

fun BleDeviceEntity.toDiscoveredDevice(): DiscoveredDevice = DiscoveredDevice(
  macAddress = macAddress,
  name = name,
  rssi = rssi,
  estimatedDistance = estimatedDistance,
  lastSeenTimestamp = lastSeenTimestamp,
)

fun DiscoveredDevice.toEntity(): BleDeviceEntity = BleDeviceEntity(
  macAddress = macAddress,
  name = name,
  rssi = rssi,
  estimatedDistance = estimatedDistance,
  lastSeenTimestamp = lastSeenTimestamp,
)
