package com.example.ble_radar.ui.target

import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.data.model.DiscoveredDevice
import com.example.ble_radar.data.repository.BleDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RadarTargetViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeTargetRepository
  private lateinit var viewModel: RadarTargetViewModel
  private val targetMac = "E4:5F:01:9B:4A:2C"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeTargetRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun init_loadsCachedDataFromDbAndStartsContinuousTracking() = runTest {
    fakeRepository.saveDevice(
      DiscoveredDevice(
        macAddress = targetMac,
        name = "Nordic Dongle",
        rssi = -55,
        estimatedDistance = "3 – 10 meters",
      ),
    )

    viewModel = RadarTargetViewModel(targetMac, fakeRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertEquals(targetMac, state.targetMacAddress)
    assertEquals("Nordic Dongle", state.deviceName)
    assertEquals(-55, state.rssi)
    assertEquals("3 – 10 meters", state.estimatedDistance)
    assertEquals(BleSignalLevel.MODERATE, state.signalLevel)
    assertTrue(state.isScanning)
  }

  @Test
  fun continuousTracking_updatesTelemetryWhenTargetPacketArrives() = runTest {
    viewModel = RadarTargetViewModel(targetMac, fakeRepository)
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isScanning)

    val closePacket = DiscoveredDevice(
      macAddress = targetMac,
      name = "Nordic Dongle",
      rssi = -25,
      estimatedDistance = "< 1 meter",
    )
    fakeRepository.targetChannel.send(closePacket)
    advanceUntilIdle()

    val closeState = viewModel.uiState.value
    assertEquals(-25, closeState.rssi)
    assertEquals("< 1 meter", closeState.estimatedDistance)
    assertEquals(BleSignalLevel.VERY_STRONG, closeState.signalLevel)

    val farPacket = DiscoveredDevice(
      macAddress = targetMac,
      name = "Nordic Dongle",
      rssi = -75,
      estimatedDistance = "10 – 20 meters",
    )
    fakeRepository.targetChannel.send(farPacket)
    advanceUntilIdle()

    val farState = viewModel.uiState.value
    assertEquals(-75, farState.rssi)
    assertEquals("10 – 20 meters", farState.estimatedDistance)
    assertEquals(BleSignalLevel.WEAK, farState.signalLevel)

    val lostPacket = DiscoveredDevice(
      macAddress = targetMac,
      name = "Nordic Dongle",
      rssi = -95,
      estimatedDistance = "Disconnected",
    )
    fakeRepository.targetChannel.send(lostPacket)
    advanceUntilIdle()

    val lostState = viewModel.uiState.value
    assertEquals(-95, lostState.rssi)
    assertEquals("Disconnected", lostState.estimatedDistance)
    assertEquals(BleSignalLevel.LOST, lostState.signalLevel)
  }

  @Test
  fun toggleTracking_pausesAndResumesContinuousDiscovery() = runTest {
    viewModel = RadarTargetViewModel(targetMac, fakeRepository)
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isScanning)

    viewModel.toggleTracking()
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isScanning)

    viewModel.toggleTracking()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isScanning)
  }

  @Test
  fun tracking_whenPermissionsMissing_setsNeedsPermission() = runTest {
    fakeRepository.permissionsGranted = false
    viewModel = RadarTargetViewModel(targetMac, fakeRepository)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isScanning)
    assertTrue(viewModel.uiState.value.needsPermission)
    assertNotNull(viewModel.uiState.value.errorMessage)
  }

  @Test
  fun tracking_whenBluetoothDisabled_setsErrorMessage() = runTest {
    fakeRepository.bluetoothEnabled = false
    viewModel = RadarTargetViewModel(targetMac, fakeRepository)
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isScanning)
    assertNotNull(viewModel.uiState.value.errorMessage)
  }
}

private class FakeTargetRepository : BleDeviceRepository {
  var permissionsGranted = true
  var bluetoothEnabled = true
  val savedList = mutableListOf<DiscoveredDevice>()
  private val _savedFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
  private val _isScanning = MutableStateFlow(false)
  val targetChannel = Channel<DiscoveredDevice>(Channel.UNLIMITED)

  override val isScanning: Flow<Boolean> = _isScanning

  override fun hasPermissions(): Boolean = permissionsGranted

  override fun isBluetoothEnabled(): Boolean = bluetoothEnabled

  override fun startDiscovery(): Flow<DiscoveredDevice> = flow {
    _isScanning.value = true
    try {
      for (device in targetChannel) {
        emit(device)
      }
    } finally {
      _isScanning.value = false
    }
  }

  override fun startTargetDiscovery(macAddress: String): Flow<DiscoveredDevice> = flow {
    _isScanning.value = true
    try {
      for (device in targetChannel) {
        if (device.macAddress.equals(macAddress, ignoreCase = true)) {
          emit(device)
        }
      }
    } finally {
      _isScanning.value = false
    }
  }

  override fun stopDiscovery() {
    _isScanning.value = false
  }

  override fun getSavedDevices(): Flow<List<DiscoveredDevice>> = _savedFlow

  override fun getSavedDeviceCount(): Flow<Int> = _savedFlow.map { it.size }

  override fun observeDevice(macAddress: String): Flow<DiscoveredDevice?> {
    return _savedFlow.map { list -> list.find { it.macAddress.equals(macAddress, ignoreCase = true) } }
  }

  override suspend fun saveDevice(device: DiscoveredDevice) {
    val idx = savedList.indexOfFirst { it.macAddress == device.macAddress }
    if (idx >= 0) {
      savedList[idx] = device
    } else {
      savedList.add(device)
    }
    _savedFlow.value = savedList.toList()
  }

  override suspend fun clearSavedDevices() {
    savedList.clear()
    _savedFlow.value = emptyList()
  }
}
