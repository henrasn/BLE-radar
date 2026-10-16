package com.example.ble_radar.ui.history

import com.example.ble_radar.data.model.DiscoveredDevice
import com.example.ble_radar.data.repository.BleDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeHistoryRepository
  private lateinit var viewModel: HistoryViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeHistoryRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialState_loadsSavedDevicesFromRepository() = runTest {
    val initialDevices = listOf(
      DiscoveredDevice(
        macAddress = "E4:5F:01:9B:4A:2C",
        name = "Nordic Dongle",
        rssi = -28,
        estimatedDistance = "< 1 meter",
        lastSeenTimestamp = 1000L,
      ),
      DiscoveredDevice(
        macAddress = "7A:38:DE:51:B0:19",
        name = "AirTag",
        rssi = -42,
        estimatedDistance = "1 – 3 meters",
        lastSeenTimestamp = 2000L,
      ),
    )
    fakeRepository.emitSavedDevices(initialDevices)

    viewModel = HistoryViewModel(fakeRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(2, state.devices.size)
    assertEquals("Nordic Dongle", state.devices[0].name)
    assertEquals("E4:5F:01:9B:4A:2C", state.devices[0].macAddress)
    assertEquals(-28, state.devices[0].rssi)
    assertEquals("AirTag", state.devices[1].name)
  }

  @Test
  fun emptyRepository_emitsEmptyDevicesList() = runTest {
    fakeRepository.emitSavedDevices(emptyList())

    viewModel = HistoryViewModel(fakeRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertTrue(state.devices.isEmpty())
  }

  @Test
  fun repositoryUpdates_emitsNewSavedDevicesList() = runTest {
    viewModel = HistoryViewModel(fakeRepository)
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.devices.isEmpty())

    fakeRepository.emitSavedDevices(
      listOf(
        DiscoveredDevice(
          macAddress = "24:6F:28:AE:55:82",
          name = "ESP32_Beacon",
          rssi = -64,
          estimatedDistance = "3 – 10 meters",
          lastSeenTimestamp = 3000L,
        ),
      ),
    )
    advanceUntilIdle()

    val updatedState = viewModel.uiState.value
    assertEquals(1, updatedState.devices.size)
    assertEquals("ESP32_Beacon", updatedState.devices[0].name)
  }

  @Test
  fun init_stopsActiveDiscovery() = runTest {
    viewModel = HistoryViewModel(fakeRepository)
    advanceUntilIdle()

    assertTrue(fakeRepository.stopDiscoveryCalled)
  }

  @Test
  fun stopActiveDiscovery_invokesRepositoryStopDiscovery() = runTest {
    viewModel = HistoryViewModel(fakeRepository)
    fakeRepository.stopDiscoveryCalled = false

    viewModel.stopActiveDiscovery()
    assertTrue(fakeRepository.stopDiscoveryCalled)
  }
}

private class FakeHistoryRepository : BleDeviceRepository {
  private val _savedDevicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
  var stopDiscoveryCalled = false

  fun emitSavedDevices(devices: List<DiscoveredDevice>) {
    _savedDevicesFlow.value = devices
  }

  override val isScanning: Flow<Boolean> = emptyFlow()

  override fun hasPermissions(): Boolean = true

  override fun isBluetoothEnabled(): Boolean = true

  override fun startDiscovery(): Flow<DiscoveredDevice> = emptyFlow()

  override fun stopDiscovery() {
    stopDiscoveryCalled = true
  }

  override fun getSavedDevices(): Flow<List<DiscoveredDevice>> = _savedDevicesFlow

  override fun getSavedDeviceCount(): Flow<Int> = _savedDevicesFlow.map { it.size }

  override suspend fun saveDevice(device: DiscoveredDevice) {
    _savedDevicesFlow.value = _savedDevicesFlow.value + device
  }

  override suspend fun clearSavedDevices() {
    _savedDevicesFlow.value = emptyList()
  }

  override fun observeDevice(macAddress: String): Flow<DiscoveredDevice?> = emptyFlow()

  override fun startTargetDiscovery(macAddress: String): Flow<DiscoveredDevice> = emptyFlow()
}
