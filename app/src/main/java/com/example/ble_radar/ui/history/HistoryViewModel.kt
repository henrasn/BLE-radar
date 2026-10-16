package com.example.ble_radar.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_radar.data.repository.BleDeviceRepository
import com.example.ble_radar.ui.scanner.toBleDeviceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
  private val repository: BleDeviceRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
  val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

  init {
    stopActiveDiscovery()
    viewModelScope.launch {
      repository.getSavedDevices().collect { savedList ->
        _uiState.update { current ->
          current.copy(
            devices = savedList.map { it.toBleDeviceItem() },
            isLoading = false,
          )
        }
      }
    }
  }

  fun stopActiveDiscovery() {
    repository.stopDiscovery()
  }
}
