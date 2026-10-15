package com.example.ble_radar.ui.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_radar.data.repository.BleDeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = RadarTargetViewModel.Factory::class)
class RadarTargetViewModel @AssistedInject constructor(
  @Assisted private val targetMacAddress: String,
  private val repository: BleDeviceRepository,
) : ViewModel() {

  @AssistedFactory
  interface Factory {
    fun create(targetMacAddress: String): RadarTargetViewModel
  }

  private val _uiState = MutableStateFlow(
    RadarTargetUiState(
      targetMacAddress = targetMacAddress,
    ),
  )
  val uiState: StateFlow<RadarTargetUiState> = _uiState.asStateFlow()

  private var trackingJob: Job? = null

  init {
    viewModelScope.launch {
      repository.observeDevice(targetMacAddress).collect { cached ->
        if (cached != null) {
          _uiState.update { current ->
            current.copy(
              deviceName = if (cached.name.isNotBlank()) cached.name else current.deviceName,
              rssi = cached.rssi,
              estimatedDistance = cached.estimatedDistance,
              signalLevel = cached.signalLevel,
              lastSeenTimestamp = cached.lastSeenTimestamp,
            )
          }
        }
      }
    }

    startTracking()
  }

  fun startTracking() {
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

    trackingJob?.cancel()
    trackingJob = viewModelScope.launch {
      repository.startTargetDiscovery(targetMacAddress)
        .catch { error ->
          _uiState.update {
            it.copy(
              isScanning = false,
              errorMessage = error.localizedMessage ?: "An error occurred during target BLE scanning",
            )
          }
        }
        .collect { targetDevice ->
          _uiState.update { current ->
            current.copy(
              deviceName = if (targetDevice.name.isNotBlank()) targetDevice.name else current.deviceName,
              rssi = targetDevice.rssi,
              estimatedDistance = targetDevice.estimatedDistance,
              signalLevel = targetDevice.signalLevel,
              lastSeenTimestamp = targetDevice.lastSeenTimestamp,
            )
          }
        }
    }
  }

  fun stopTracking() {
    trackingJob?.cancel()
    trackingJob = null
    repository.stopDiscovery()
    _uiState.update { it.copy(isScanning = false) }
  }

  fun toggleTracking() {
    if (_uiState.value.isScanning) {
      stopTracking()
    } else {
      startTracking()
    }
  }

  fun clearErrorMessage() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  fun onPermissionsResult(granted: Boolean) {
    _uiState.update { it.copy(needsPermission = false) }
    if (granted) {
      startTracking()
    } else {
      _uiState.update {
        it.copy(errorMessage = "Bluetooth permission was denied. Cannot track target.")
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    stopTracking()
  }
}
