package com.example.ble_radar.ui.history

import com.example.ble_radar.ui.scanner.BleDeviceItem

data class HistoryUiState(
  val devices: List<BleDeviceItem> = emptyList(),
  val isLoading: Boolean = false,
)
