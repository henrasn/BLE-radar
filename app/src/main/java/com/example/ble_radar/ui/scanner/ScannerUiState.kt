package com.example.ble_radar.ui.scanner

import androidx.compose.ui.graphics.Color
import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.data.model.DiscoveredDevice
import com.example.ble_radar.theme.RadarError
import com.example.ble_radar.theme.RadarPrimaryFixedDim
import com.example.ble_radar.theme.RadarSecondary
import com.example.ble_radar.theme.RadarTertiaryFixedDim

data class BleDeviceItem(
  val id: String,
  val name: String,
  val macAddress: String? = null,
  val uuid: String? = null,
  val rssi: Int,
  val estimatedDistance: String,
  val isUnknown: Boolean = false,
  val lastSeenTimestamp: Long = System.currentTimeMillis(),
) {
  val signalLevel: BleSignalLevel
    get() = BleSignalLevel.fromRssi(rssi)

  val rssiColor: Color
    get() =
      when (signalLevel) {
        BleSignalLevel.VERY_STRONG -> RadarSecondary
        BleSignalLevel.STRONG -> RadarPrimaryFixedDim
        BleSignalLevel.MODERATE -> RadarSecondary
        BleSignalLevel.WEAK -> RadarTertiaryFixedDim
        BleSignalLevel.VERY_WEAK -> RadarError
        BleSignalLevel.LOST -> RadarError
      }
}

val BleSignalLevel.barColor: Color
  get() =
    when (this) {
      BleSignalLevel.VERY_STRONG -> com.example.ble_radar.theme.SignalVeryStrong
      BleSignalLevel.STRONG -> com.example.ble_radar.theme.SignalStrong
      BleSignalLevel.MODERATE -> com.example.ble_radar.theme.SignalModerate
      BleSignalLevel.WEAK -> com.example.ble_radar.theme.SignalWeak
      BleSignalLevel.VERY_WEAK -> com.example.ble_radar.theme.SignalVeryWeak
      BleSignalLevel.LOST -> com.example.ble_radar.theme.SignalLost
    }


fun DiscoveredDevice.toBleDeviceItem(): BleDeviceItem = BleDeviceItem(
  id = macAddress,
  name = name,
  macAddress = macAddress,
  rssi = rssi,
  estimatedDistance = estimatedDistance,
  isUnknown = isUnknown,
  lastSeenTimestamp = lastSeenTimestamp,
)

enum class ScannerNavTab {
  SCANNER,
  HISTORY,
}

enum class SearchFilterTarget(val displayName: String) {
  ALL("All"),
  DEVICE_NAME("Device Name"),
  MAC_ADDRESS("MAC Address"),
  RSSI("Signal Strength (RSSI)"),
}

data class ScannerFilterState(
  val target: SearchFilterTarget = SearchFilterTarget.ALL,
  val selectedSignalLevel: BleSignalLevel? = null,
)

data class ScannerUiState(
  val isScanning: Boolean = false,
  val totalFoundCount: Int = 0,
  val searchQuery: String = "",
  val devices: List<BleDeviceItem> = emptyList(),
  val selectedTab: ScannerNavTab = ScannerNavTab.SCANNER,
  val errorMessage: String? = null,
  val needsPermission: Boolean = false,
  val isFilterSheetOpen: Boolean = false,
  val filterState: ScannerFilterState = ScannerFilterState(),
) {
  val filteredDevices: List<BleDeviceItem>
    get() {
      return devices.filter { device ->
        // 1. Text filter based on target
        val textMatches = if (searchQuery.isBlank()) {
          true
        } else {
          val query = searchQuery.trim().lowercase()
          when (filterState.target) {
            SearchFilterTarget.ALL -> {
              device.name.lowercase().contains(query) ||
                device.macAddress?.lowercase()?.contains(query) == true ||
                device.uuid?.lowercase()?.contains(query) == true
            }
            SearchFilterTarget.DEVICE_NAME -> {
              device.name.lowercase().contains(query)
            }
            SearchFilterTarget.MAC_ADDRESS -> {
              device.macAddress?.lowercase()?.contains(query) == true
            }
            SearchFilterTarget.RSSI -> {
              device.rssi.toString().contains(query) ||
                device.signalLevel.label.lowercase().contains(query) ||
                device.estimatedDistance.lowercase().contains(query)
            }
          }
        }

        // 2. Specific RSSI / distance signal level filter if selected
        val signalMatches = if (filterState.target == SearchFilterTarget.RSSI && filterState.selectedSignalLevel != null) {
          device.signalLevel == filterState.selectedSignalLevel
        } else {
          true
        }

        textMatches && signalMatches
      }
    }
}
