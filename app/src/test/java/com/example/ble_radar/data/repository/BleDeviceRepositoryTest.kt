package com.example.ble_radar.data.repository

import com.example.ble_radar.data.local.BleLocalDataSource
import com.example.ble_radar.data.model.DiscoveredDevice
import com.example.ble_radar.data.scanner.BleScannerDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BleDeviceRepositoryTest {

  private lateinit var fakeScannerDataSource: FakeBleScannerDataSource
  private lateinit var fakeLocalDataSource: FakeBleLocalDataSource
  private lateinit var repository: BleDeviceRepositoryImpl

  @Before
  fun setUp() {
    fakeScannerDataSource = FakeBleScannerDataSource()
    fakeLocalDataSource = FakeBleLocalDataSource()
    repository = BleDeviceRepositoryImpl(fakeScannerDataSource, fakeLocalDataSource)
  }

  @Test
  fun hasPermissions_delegatesToScannerDataSource() {
    fakeScannerDataSource.hasPermissions = true
    assertTrue(repository.hasPermissions())

    fakeScannerDataSource.hasPermissions = false
    assertFalse(repository.hasPermissions())
  }

  @Test
  fun isBluetoothEnabled_delegatesToScannerDataSource() {
    fakeScannerDataSource.bluetoothEnabled = true
    assertTrue(repository.isBluetoothEnabled())

    fakeScannerDataSource.bluetoothEnabled = false
    assertFalse(repository.isBluetoothEnabled())
  }

  @Test
  fun startDiscovery_savesDiscoveredDevicesToLocalDataSourceAndEmits() = runTest {
    val emittedDevices = mutableListOf<DiscoveredDevice>()
    val job = launch {
      repository.startDiscovery().collect { device ->
        emittedDevices.add(device)
      }
    }

    advanceUntilIdle()
    assertTrue(repository.isScanning.first())

    val device1 = DiscoveredDevice(
      macAddress = "11:22:33:44:55:66",
      name = "AirTag",
      rssi = -35,
      estimatedDistance = "1 – 3 meters",
    )
    fakeScannerDataSource.emit(device1)
    advanceUntilIdle()

    assertEquals(1, emittedDevices.size)
    assertEquals("11:22:33:44:55:66", emittedDevices[0].macAddress)
    assertEquals(1, fakeLocalDataSource.savedList.size)
    assertEquals("11:22:33:44:55:66", fakeLocalDataSource.savedList[0].macAddress)

    job.cancel()
    advanceUntilIdle()
    assertFalse(repository.isScanning.first())
  }

  @Test
  fun stopDiscovery_stopsScannerDataSource() {
    repository.stopDiscovery()
    assertTrue(fakeScannerDataSource.stopScanCalled)
  }

  @Test
  fun savedDevicesFlows_delegateToLocalDataSource() = runTest {
    val device = DiscoveredDevice(
      macAddress = "AA:BB:CC:DD:EE:FF",
      name = "ESP32",
      rssi = -60,
      estimatedDistance = "3 – 10 meters",
    )
    repository.saveDevice(device)
    advanceUntilIdle()

    val saved = repository.getSavedDevices().first()
    assertEquals(1, saved.size)
    assertEquals("ESP32", saved[0].name)

    val count = repository.getSavedDeviceCount().first()
    assertEquals(1, count)

    repository.clearSavedDevices()
    advanceUntilIdle()

    val cleared = repository.getSavedDevices().first()
    assertEquals(0, cleared.size)
  }
}

private class FakeBleScannerDataSource : BleScannerDataSource {
  var hasPermissions = true
  var bluetoothEnabled = true
  var stopScanCalled = false
  private val channel = Channel<DiscoveredDevice>(Channel.UNLIMITED)

  override fun hasRequiredPermissions(): Boolean = hasPermissions

  override fun isBluetoothEnabled(): Boolean = bluetoothEnabled

  override fun startScan(): Flow<DiscoveredDevice> = flow {
    for (item in channel) {
      emit(item)
    }
  }

  suspend fun emit(device: DiscoveredDevice) {
    channel.send(device)
  }

  override fun stopScan() {
    stopScanCalled = true
  }
}

private class FakeBleLocalDataSource : BleLocalDataSource {
  val savedList = mutableListOf<DiscoveredDevice>()
  private val flow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())

  override fun getAllDevices(): Flow<List<DiscoveredDevice>> = flow

  override fun getDeviceCount(): Flow<Int> = flow.map { it.size }

  override suspend fun saveDevice(device: DiscoveredDevice) {
    val idx = savedList.indexOfFirst { it.macAddress == device.macAddress }
    if (idx >= 0) {
      savedList[idx] = device
    } else {
      savedList.add(device)
    }
    flow.value = savedList.toList()
  }

  override suspend fun saveDevices(devices: List<DiscoveredDevice>) {
    devices.forEach { saveDevice(it) }
  }

  override suspend fun getDevice(macAddress: String): DiscoveredDevice? {
    return savedList.find { it.macAddress == macAddress }
  }

  override suspend fun clearAll() {
    savedList.clear()
    flow.value = emptyList()
  }
}
