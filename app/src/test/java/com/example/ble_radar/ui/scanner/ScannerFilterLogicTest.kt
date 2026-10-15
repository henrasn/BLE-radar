package com.example.ble_radar.ui.scanner

import com.example.ble_radar.data.model.BleSignalLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerFilterLogicTest {

  private val testDevices = listOf(
    BleDeviceItem(
      id = "AA:BB:CC:DD:EE:01",
      name = "Nordic nRF Beacon",
      macAddress = "AA:BB:CC:DD:EE:01",
      rssi = -25, // VERY_STRONG (< 1 meter)
      estimatedDistance = "< 1 meter",
    ),
    BleDeviceItem(
      id = "11:22:33:44:55:66",
      name = "Apple Watch Ultra",
      macAddress = "11:22:33:44:55:66",
      rssi = -42, // STRONG (1 – 3 meters)
      estimatedDistance = "1 – 3 meters",
    ),
    BleDeviceItem(
      id = "AA:11:22:33:44:99",
      name = "ESP32 Sensor Hub",
      macAddress = "AA:11:22:33:44:99",
      rssi = -68, // MODERATE (3 – 10 meters)
      estimatedDistance = "3 – 10 meters",
    ),
    BleDeviceItem(
      id = "0000FE95-0000-1000-8000-00805F9B34FB",
      name = "Unknown Device",
      uuid = "0000FE95-0000-1000-8000-00805F9B34FB",
      rssi = -88, // VERY_WEAK (> 20 meters)
      estimatedDistance = "> 20 meters",
      isUnknown = true,
    ),
  )

  @Test
  fun filter_whenQueryIsBlankAndNoSignalLevelSelected_returnsAllDevices() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "",
      filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
    )

    assertEquals(4, state.filteredDevices.size)
  }

  @Test
  fun filterTargetAll_matchesDeviceNameCaseInsensitive() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "watch",
      filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
    )

    assertEquals(1, state.filteredDevices.size)
    assertEquals("Apple Watch Ultra", state.filteredDevices[0].name)
  }

  @Test
  fun filterTargetAll_matchesMacAddress() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "EE:01",
      filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
    )

    assertEquals(1, state.filteredDevices.size)
    assertEquals("AA:BB:CC:DD:EE:01", state.filteredDevices[0].macAddress)
  }

  @Test
  fun filterTargetAll_matchesUuid() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "FE95",
      filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
    )

    assertEquals(1, state.filteredDevices.size)
    assertEquals("0000FE95-0000-1000-8000-00805F9B34FB", state.filteredDevices[0].uuid)
  }

  @Test
  fun filterTargetDeviceName_matchesOnlyNameAndNotMacOrUuid() {
    // Query "AA" appears in both names and MAC addresses
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "AA",
      filterState = ScannerFilterState(target = SearchFilterTarget.DEVICE_NAME),
    )

    // No device has "AA" in their name ("Nordic...", "Apple...", "ESP32...", "Unknown...")
    assertEquals(0, state.filteredDevices.size)

    val stateMatch = ScannerUiState(
      devices = testDevices,
      searchQuery = "nordic",
      filterState = ScannerFilterState(target = SearchFilterTarget.DEVICE_NAME),
    )
    assertEquals(1, stateMatch.filteredDevices.size)
    assertEquals("Nordic nRF Beacon", stateMatch.filteredDevices[0].name)
  }

  @Test
  fun filterTargetMacAddress_matchesOnlyMacAddress() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "AA:11",
      filterState = ScannerFilterState(target = SearchFilterTarget.MAC_ADDRESS),
    )

    assertEquals(1, state.filteredDevices.size)
    assertEquals("ESP32 Sensor Hub", state.filteredDevices[0].name)

    // Searching device name "ESP32" under MAC filter should yield 0
    val stateMismatch = ScannerUiState(
      devices = testDevices,
      searchQuery = "ESP32",
      filterState = ScannerFilterState(target = SearchFilterTarget.MAC_ADDRESS),
    )
    assertEquals(0, stateMismatch.filteredDevices.size)
  }

  @Test
  fun filterTargetRssi_matchesNumericRssiAndSignalLevelLabel() {
    // 1. Matches numeric RSSI substring
    val stateNumeric = ScannerUiState(
      devices = testDevices,
      searchQuery = "-42",
      filterState = ScannerFilterState(target = SearchFilterTarget.RSSI),
    )
    assertEquals(1, stateNumeric.filteredDevices.size)
    assertEquals(-42, stateNumeric.filteredDevices[0].rssi)

    // 2. Matches signal level label (e.g. "Very Strong")
    val stateLabel = ScannerUiState(
      devices = testDevices,
      searchQuery = "Very Strong",
      filterState = ScannerFilterState(target = SearchFilterTarget.RSSI),
    )
    assertEquals(1, stateLabel.filteredDevices.size)
    assertEquals("Nordic nRF Beacon", stateLabel.filteredDevices[0].name)

    // 3. Matches estimated distance string (e.g. "3 – 10 meters")
    val stateDistance = ScannerUiState(
      devices = testDevices,
      searchQuery = "3 – 10",
      filterState = ScannerFilterState(target = SearchFilterTarget.RSSI),
    )
    assertEquals(1, stateDistance.filteredDevices.size)
    assertEquals("ESP32 Sensor Hub", stateDistance.filteredDevices[0].name)
  }

  @Test
  fun filterTargetRssi_withSelectedSignalLevel_filtersStrictlyByLevel() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "",
      filterState = ScannerFilterState(
        target = SearchFilterTarget.RSSI,
        selectedSignalLevel = BleSignalLevel.VERY_STRONG,
      ),
    )

    assertEquals(1, state.filteredDevices.size)
    assertEquals(BleSignalLevel.VERY_STRONG, state.filteredDevices[0].signalLevel)
  }

  @Test
  fun filterTargetRssi_combinedTextAndSignalLevelFilter() {
    // Both signal level VERY_STRONG and search query matching name or RSSI
    val stateMatch = ScannerUiState(
      devices = testDevices,
      searchQuery = "Nordic",
      filterState = ScannerFilterState(
        target = SearchFilterTarget.RSSI,
        selectedSignalLevel = BleSignalLevel.VERY_STRONG,
      ),
    )
    // -25 dBm matches VERY_STRONG, but target is RSSI so query "Nordic" doesn't match RSSI fields
    assertEquals(0, stateMatch.filteredDevices.size)

    val stateMatchRssi = ScannerUiState(
      devices = testDevices,
      searchQuery = "-25",
      filterState = ScannerFilterState(
        target = SearchFilterTarget.RSSI,
        selectedSignalLevel = BleSignalLevel.VERY_STRONG,
      ),
    )
    assertEquals(1, stateMatchRssi.filteredDevices.size)
    assertEquals("Nordic nRF Beacon", stateMatchRssi.filteredDevices[0].name)
  }

  @Test
  fun filter_whenNoDeviceMatches_returnsEmptyList() {
    val state = ScannerUiState(
      devices = testDevices,
      searchQuery = "NonExistentString12345",
      filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
    )

    assertTrue(state.filteredDevices.isEmpty())
  }
}
