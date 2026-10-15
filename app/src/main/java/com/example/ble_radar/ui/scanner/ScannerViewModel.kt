package com.example.ble_radar.ui.scanner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ble_radar.data.local.BleDatabase
import com.example.ble_radar.data.local.BleLocalDataSourceImpl
import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.data.repository.BleDeviceRepository
import com.example.ble_radar.data.repository.BleDeviceRepositoryImpl
import com.example.ble_radar.data.scanner.AndroidBleScannerDataSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScannerViewModel(
  private val repository: BleDeviceRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(
    ScannerUiState(
      isScanning = false,
      totalFoundCount = 0,
      devices = emptyList(),
      selectedTab = ScannerNavTab.SCANNER,
    ),
  )
  val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

  private var scanJob: Job? = null

  init {
    // Collect existing devices persisted in local Room DB on start
    viewModelScope.launch {
      repository.getSavedDevices().collect { savedList ->
        _uiState.update { current ->
          // If we are not currently active in a live scan or devices list is empty, initialize from DB
          if (current.devices.isEmpty() && savedList.isNotEmpty()) {
            current.copy(
              devices = savedList.map { it.toBleDeviceItem() },
              totalFoundCount = savedList.size,
            )
          } else {
            current
          }
        }
      }
    }
  }

  fun toggleScanning() {
    if (_uiState.value.isScanning) {
      stopScan()
    } else {
      startScan()
    }
  }

  fun startScan() {
    if (_uiState.value.isScanning) return

    if (!repository.hasPermissions()) {
      _uiState.update {
        it.copy(
          needsPermission = true,
          errorMessage = "Bluetooth and Location permissions are required to scan for devices.",
        )
      }
      return
    }

    if (!repository.isBluetoothEnabled()) {
      _uiState.update {
        it.copy(
          errorMessage = "Bluetooth is disabled. Please turn on Bluetooth on your device.",
        )
      }
      return
    }

    _uiState.update { it.copy(isScanning = true, errorMessage = null, needsPermission = false) }

    scanJob?.cancel()
    scanJob = viewModelScope.launch {
      repository.startDiscovery()
        .catch { error ->
          _uiState.update {
            it.copy(
              isScanning = false,
              errorMessage = error.localizedMessage ?: "An error occurred during BLE scanning",
            )
          }
        }
        .collect { discovered ->
          _uiState.update { current ->
            val updatedList = current.devices.toMutableList()
            val existingIndex = updatedList.indexOfFirst { it.macAddress == discovered.macAddress }
            val newItem = discovered.toBleDeviceItem()

            if (existingIndex >= 0) {
              // Update existing unique device with latest RSSI and distance
              updatedList[existingIndex] = newItem
            } else {
              // Add newly discovered device at top
              updatedList.add(0, newItem)
            }

            current.copy(
              devices = updatedList,
              totalFoundCount = updatedList.size,
            )
          }
        }
    }
  }

  fun stopScan() {
    scanJob?.cancel()
    scanJob = null
    repository.stopDiscovery()
    _uiState.update { it.copy(isScanning = false) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { current ->
      current.copy(searchQuery = query)
    }
  }

  fun clearSearchQuery() {
    _uiState.update { current ->
      current.copy(searchQuery = "")
    }
  }

  fun selectTab(tab: ScannerNavTab) {
    _uiState.update { current ->
      current.copy(selectedTab = tab)
    }
  }

  fun clearErrorMessage() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  fun onPermissionsResult(granted: Boolean) {
    _uiState.update { it.copy(needsPermission = false) }
    if (granted) {
      startScan()
    } else {
      _uiState.update {
        it.copy(errorMessage = "Bluetooth permission was denied. Cannot start scanning.")
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    stopScan()
  }

  fun openFilterSheet() {
    _uiState.update { it.copy(isFilterSheetOpen = true) }
  }

  fun closeFilterSheet() {
    _uiState.update { it.copy(isFilterSheetOpen = false) }
  }

  fun setFilterTarget(target: SearchFilterTarget) {
    _uiState.update { current ->
      current.copy(
        filterState = current.filterState.copy(target = target),
      )
    }
  }

  fun setSignalLevelFilter(level: BleSignalLevel?) {
    _uiState.update { current ->
      current.copy(
        filterState = current.filterState.copy(
          target = SearchFilterTarget.RSSI,
          selectedSignalLevel = level,
        ),
      )
    }
  }

  fun resetFilter() {
    _uiState.update { current ->
      current.copy(
        filterState = ScannerFilterState(),
      )
    }
  }

  companion object {
    val DEFAULT_DEVICES = listOf(
      BleDeviceItem(
        id = "E4:5F:01:9B:4A:2C",
        name = "Nordic nRF52840 Dongle",
        macAddress = "E4:5F:01:9B:4A:2C",
        rssi = -28,
        estimatedDistance = "< 1 meter",
      ),
      BleDeviceItem(
        id = "7A:38:DE:51:B0:19",
        name = "Apple AirTag (Living Room)",
        macAddress = "7A:38:DE:51:B0:19",
        rssi = -42,
        estimatedDistance = "1 – 3 meters",
      ),
      BleDeviceItem(
        id = "24:6F:28:AE:55:82",
        name = "ESP32_Telemetry_Beacon",
        macAddress = "24:6F:28:AE:55:82",
        rssi = -64,
        estimatedDistance = "3 – 10 meters",
      ),
      BleDeviceItem(
        id = "F2:A0:C3:7E:11:80",
        name = "Smart Band 8 Active",
        macAddress = "F2:A0:C3:7E:11:80",
        rssi = -74,
        estimatedDistance = "10 – 20 meters",
      ),
      BleDeviceItem(
        id = "0000FE95-0000-1000-8000-00805F9B34FB",
        name = "Unknown Device",
        uuid = "0000FE95-0000-1000-8000-00805F9B34FB",
        rssi = -86,
        estimatedDistance = "> 20 meters",
        isUnknown = true,
      ),
    )

    fun createFactory(context: Context): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          val db = BleDatabase.getDatabase(context)
          val localDataSource = BleLocalDataSourceImpl(db.bleDeviceDao())
          val scannerDataSource = AndroidBleScannerDataSource(context)
          val repository = BleDeviceRepositoryImpl(scannerDataSource, localDataSource)
          return ScannerViewModel(repository) as T
        }
      }
  }
}
