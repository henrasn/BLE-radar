package com.example.ble_radar.ui.scanner

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
class ScannerViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var fakeRepository: FakeBleDeviceRepository
  private lateinit var viewModel: ScannerViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeBleDeviceRepository()
    viewModel = ScannerViewModel(fakeRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialState_isIdleAndReady() = runTest {
    advanceUntilIdle()
    val state = viewModel.uiState.value
    assertFalse(state.isScanning)
    assertEquals(0, state.totalFoundCount)
    assertEquals(ScannerNavTab.SCANNER, state.selectedTab)
    assertFalse(state.isFilterSheetOpen)
  }

  @Test
  fun distanceClassification_matchesUserSpecifiedRanges() {
    // -10 to -30 dBm Very Strong (Very Close) < 1 meter
    assertEquals("< 1 meter", DiscoveredDevice.calculateDistance(-10))
    assertEquals("< 1 meter", DiscoveredDevice.calculateDistance(-25))
    assertEquals("< 1 meter", DiscoveredDevice.calculateDistance(-30))
    assertEquals(BleSignalLevel.VERY_STRONG, BleSignalLevel.fromRssi(-28))

    // -30 to -50 dBm Strong (Close) 1 – 3 meters
    assertEquals("1 – 3 meters", DiscoveredDevice.calculateDistance(-31))
    assertEquals("1 – 3 meters", DiscoveredDevice.calculateDistance(-45))
    assertEquals("1 – 3 meters", DiscoveredDevice.calculateDistance(-50))
    assertEquals(BleSignalLevel.STRONG, BleSignalLevel.fromRssi(-42))

    // -50 to -70 dBm Moderate / Good 3 – 10 meters
    assertEquals("3 – 10 meters", DiscoveredDevice.calculateDistance(-55))
    assertEquals("3 – 10 meters", DiscoveredDevice.calculateDistance(-70))
    assertEquals(BleSignalLevel.MODERATE, BleSignalLevel.fromRssi(-64))

    // -70 to -80 dBm Weak 10 – 20 meters
    assertEquals("10 – 20 meters", DiscoveredDevice.calculateDistance(-71))
    assertEquals("10 – 20 meters", DiscoveredDevice.calculateDistance(-80))
    assertEquals(BleSignalLevel.WEAK, BleSignalLevel.fromRssi(-74))

    // -80 to -90 dBm Very Weak / Intermittent > 20 meters (Out of range threshold)
    assertEquals("> 20 meters", DiscoveredDevice.calculateDistance(-81))
    assertEquals("> 20 meters", DiscoveredDevice.calculateDistance(-90))
    assertEquals(BleSignalLevel.VERY_WEAK, BleSignalLevel.fromRssi(-86))

    // < -90 dBm Signal Lost (Disconnected / Out of range)
    assertEquals("Disconnected", DiscoveredDevice.calculateDistance(-91))
    assertEquals("Disconnected", DiscoveredDevice.calculateDistance(-99))
    assertEquals(BleSignalLevel.LOST, BleSignalLevel.fromRssi(-95))
  }

  @Test
  fun startScan_startsDiscoveryAndSavesToDbWithUniqueMac() = runTest {
    viewModel.startScan()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isScanning)

    // Simulate discovering first device
    val device1 = DiscoveredDevice(
      macAddress = "AA:BB:CC:DD:EE:01",
      name = "Beacon Sensor 1",
      rssi = -25,
      estimatedDistance = "< 1 meter",
    )
    fakeRepository.discoveryChannel.send(device1)
    advanceUntilIdle()

    assertEquals(1, viewModel.uiState.value.devices.size)
    assertEquals("AA:BB:CC:DD:EE:01", viewModel.uiState.value.devices[0].macAddress)
    assertEquals(-25, viewModel.uiState.value.devices[0].rssi)

    // Verify it was saved to DB
    assertEquals(1, fakeRepository.savedDevicesList.size)
    assertEquals("AA:BB:CC:DD:EE:01", fakeRepository.savedDevicesList[0].macAddress)

    // Simulate discovering the SAME MAC address with updated RSSI
    val device1Updated = DiscoveredDevice(
      macAddress = "AA:BB:CC:DD:EE:01",
      name = "Beacon Sensor 1",
      rssi = -40,
      estimatedDistance = "1 – 3 meters",
    )
    fakeRepository.discoveryChannel.send(device1Updated)
    advanceUntilIdle()

    // Must still have only 1 unique device, but updated values
    assertEquals(1, viewModel.uiState.value.devices.size)
    assertEquals(-40, viewModel.uiState.value.devices[0].rssi)
    assertEquals("1 – 3 meters", viewModel.uiState.value.devices[0].estimatedDistance)

    // DB must also have unique entry
    assertEquals(1, fakeRepository.savedDevicesList.size)
    assertEquals(-40, fakeRepository.savedDevicesList[0].rssi)

    // Simulate discovering a second device with a different MAC
    val device2 = DiscoveredDevice(
      macAddress = "11:22:33:44:55:66",
      name = "Smart Tag",
      rssi = -75,
      estimatedDistance = "10 – 20 meters",
    )
    fakeRepository.discoveryChannel.send(device2)
    advanceUntilIdle()

    assertEquals(2, viewModel.uiState.value.devices.size)
    assertEquals(2, fakeRepository.savedDevicesList.size)

    // Stop scan
    viewModel.stopScan()
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isScanning)
  }

  @Test
  fun toggleScanning_startsAndStopsProperly() = runTest {
    assertFalse(viewModel.uiState.value.isScanning)

    viewModel.toggleScanning()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isScanning)

    viewModel.toggleScanning()
    advanceUntilIdle()
    assertFalse(viewModel.uiState.value.isScanning)
  }

  @Test
  fun startScan_whenPermissionsMissing_setsNeedsPermission() = runTest {
    fakeRepository.permissionsGranted = false

    viewModel.startScan()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isScanning)
    assertTrue(viewModel.uiState.value.needsPermission)
    assertNotNull(viewModel.uiState.value.errorMessage)
  }

  @Test
  fun filterBottomSheet_opensAndCloses() = runTest {
    assertFalse(viewModel.uiState.value.isFilterSheetOpen)

    viewModel.openFilterSheet()
    assertTrue(viewModel.uiState.value.isFilterSheetOpen)

    viewModel.closeFilterSheet()
    assertFalse(viewModel.uiState.value.isFilterSheetOpen)
  }

  @Test
  fun searchFilter_filtersDiscoveredDevicesByTargetAndRssi() = runTest {
    viewModel.startScan()
    advanceUntilIdle()

    fakeRepository.discoveryChannel.send(
      DiscoveredDevice(
        macAddress = "AA:BB:CC:DD:EE:01",
        name = "Nordic Beacon",
        rssi = -25, // VERY_STRONG (< 1m)
        estimatedDistance = "< 1 meter",
      ),
    )
    fakeRepository.discoveryChannel.send(
      DiscoveredDevice(
        macAddress = "11:22:33:44:55:66",
        name = "Apple Watch",
        rssi = -65, // MODERATE (3 - 10m)
        estimatedDistance = "3 – 10 meters",
      ),
    )
    fakeRepository.discoveryChannel.send(
      DiscoveredDevice(
        macAddress = "99:88:77:66:55:44",
        name = "AirTag Beacon",
        rssi = -85, // VERY_WEAK (> 20m)
        estimatedDistance = "> 20 meters",
      ),
    )
    advanceUntilIdle()

    assertEquals(3, viewModel.uiState.value.devices.size)

    // 1. Text filter by Name
    viewModel.setFilterTarget(SearchFilterTarget.DEVICE_NAME)
    viewModel.updateSearchQuery("Watch")
    assertEquals(1, viewModel.uiState.value.filteredDevices.size)
    assertEquals("Apple Watch", viewModel.uiState.value.filteredDevices[0].name)

    // 2. Text filter by MAC Address
    viewModel.setFilterTarget(SearchFilterTarget.MAC_ADDRESS)
    viewModel.updateSearchQuery("99:88")
    assertEquals(1, viewModel.uiState.value.filteredDevices.size)
    assertEquals("AirTag Beacon", viewModel.uiState.value.filteredDevices[0].name)

    // 3. Signal Level filter
    viewModel.clearSearchQuery()
    viewModel.setSignalLevelFilter(BleSignalLevel.VERY_STRONG)
    assertEquals(1, viewModel.uiState.value.filteredDevices.size)
    assertEquals("Nordic Beacon", viewModel.uiState.value.filteredDevices[0].name)

    // Reset filter
    viewModel.resetFilter()
    assertEquals(3, viewModel.uiState.value.filteredDevices.size)
  }
}

private class FakeBleDeviceRepository : BleDeviceRepository {
  var permissionsGranted = true
  var bluetoothEnabled = true
  val savedDevicesList = mutableListOf<DiscoveredDevice>()
  private val _savedDevicesFlow = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
  private val _isScanning = MutableStateFlow(false)
  val discoveryChannel = Channel<DiscoveredDevice>(Channel.UNLIMITED)

  override val isScanning: Flow<Boolean> = _isScanning

  override fun hasPermissions(): Boolean = permissionsGranted

  override fun isBluetoothEnabled(): Boolean = bluetoothEnabled

  override fun startDiscovery(): Flow<DiscoveredDevice> = flow {
    _isScanning.value = true
    try {
      for (device in discoveryChannel) {
        saveDevice(device)
        emit(device)
      }
    } finally {
      _isScanning.value = false
    }
  }

  override fun stopDiscovery() {
    _isScanning.value = false
  }

  override fun getSavedDevices(): Flow<List<DiscoveredDevice>> = _savedDevicesFlow

  override fun getSavedDeviceCount(): Flow<Int> = _savedDevicesFlow.map { it.size }

  override fun observeDevice(macAddress: String): Flow<DiscoveredDevice?> {
    return _savedDevicesFlow.map { list -> list.find { it.macAddress.equals(macAddress, ignoreCase = true) } }
  }

  override fun startTargetDiscovery(macAddress: String): Flow<DiscoveredDevice> = flow {
    _isScanning.value = true
    try {
      for (device in discoveryChannel) {
        if (device.macAddress.equals(macAddress, ignoreCase = true)) {
          emit(device)
        }
      }
    } finally {
      _isScanning.value = false
    }
  }

  override suspend fun saveDevice(device: DiscoveredDevice) {
    val idx = savedDevicesList.indexOfFirst { it.macAddress == device.macAddress }
    if (idx >= 0) {
      savedDevicesList[idx] = device
    } else {
      savedDevicesList.add(device)
    }
    _savedDevicesFlow.value = savedDevicesList.toList()
  }

  override suspend fun clearSavedDevices() {
    savedDevicesList.clear()
    _savedDevicesFlow.value = emptyList()
  }
}
