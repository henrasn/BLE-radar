package com.example.ble_radar.data.repository

import com.example.ble_radar.data.local.BleLocalDataSource
import com.example.ble_radar.data.model.DiscoveredDevice
import com.example.ble_radar.data.scanner.BleScannerDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow

interface BleDeviceRepository {
  val isScanning: Flow<Boolean>
  fun hasPermissions(): Boolean
  fun isBluetoothEnabled(): Boolean
  fun startDiscovery(): Flow<DiscoveredDevice>
  fun stopDiscovery()
  fun getSavedDevices(): Flow<List<DiscoveredDevice>>
  fun getSavedDeviceCount(): Flow<Int>
  suspend fun saveDevice(device: DiscoveredDevice)
  suspend fun clearSavedDevices()
  fun observeDevice(macAddress: String): Flow<DiscoveredDevice?>
  fun startTargetDiscovery(macAddress: String): Flow<DiscoveredDevice>
}

class BleDeviceRepositoryImpl(
  private val scannerDataSource: BleScannerDataSource,
  private val localDataSource: BleLocalDataSource,
) : BleDeviceRepository {

  private val _isScanning = MutableStateFlow(false)
  override val isScanning: Flow<Boolean> = _isScanning.asStateFlow()

  override fun hasPermissions(): Boolean = scannerDataSource.hasRequiredPermissions()

  override fun isBluetoothEnabled(): Boolean = scannerDataSource.isBluetoothEnabled()

  override fun startDiscovery(): Flow<DiscoveredDevice> = channelFlow {
    _isScanning.value = true
    try {
      scannerDataSource.startScan().collect { device ->
        // Persist to Room local database with unique MAC address
        localDataSource.saveDevice(device)
        // Emit into discovery stream for real-time UI consumption
        send(device)
      }
    } finally {
      _isScanning.value = false
    }
  }

  override fun startTargetDiscovery(macAddress: String): Flow<DiscoveredDevice> = channelFlow {
    _isScanning.value = true
    try {
      scannerDataSource.startScan().collect { device ->
        // Persist all incoming packets to Room local DB
        localDataSource.saveDevice(device)
        // If packet matches the target MAC address, emit to subscriber
        if (device.macAddress.equals(macAddress, ignoreCase = true)) {
          send(device)
        }
      }
    } finally {
      _isScanning.value = false
    }
  }

  override fun stopDiscovery() {
    scannerDataSource.stopScan()
    _isScanning.value = false
  }

  override fun getSavedDevices(): Flow<List<DiscoveredDevice>> {
    return localDataSource.getAllDevices()
  }

  override fun getSavedDeviceCount(): Flow<Int> {
    return localDataSource.getDeviceCount()
  }

  override fun observeDevice(macAddress: String): Flow<DiscoveredDevice?> {
    return localDataSource.observeDevice(macAddress)
  }

  override suspend fun saveDevice(device: DiscoveredDevice) {
    localDataSource.saveDevice(device)
  }

  override suspend fun clearSavedDevices() {
    localDataSource.clearAll()
  }
}
