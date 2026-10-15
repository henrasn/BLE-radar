package com.example.ble_radar.ui.target

import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.ui.scanner.ScannerNavTab

data class RadarTargetUiState(
  val targetMacAddress: String,
  val deviceName: String = "Tracking Target",
  val rssi: Int = -95,
  val estimatedDistance: String = "Disconnected",
  val signalLevel: BleSignalLevel = BleSignalLevel.LOST,
  val isLive: Boolean = true,
  val isScanning: Boolean = false,
  val lastSeenTimestamp: Long = System.currentTimeMillis(),
  val selectedTab: ScannerNavTab = ScannerNavTab.SCANNER,
  val errorMessage: String? = null,
  val needsPermission: Boolean = false,
) {
  val isUnknown: Boolean
    get() = deviceName.isBlank() || deviceName.equals("Unknown Device", ignoreCase = true)
}
