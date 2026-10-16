package com.example.ble_radar.data.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.ble_radar.data.model.DiscoveredDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface BleScannerDataSource {
  fun hasRequiredPermissions(): Boolean
  fun isBluetoothEnabled(): Boolean
  fun startScan(): Flow<DiscoveredDevice>
  fun stopScan()
}

class AndroidBleScannerDataSource @Inject constructor(
  @param:ApplicationContext private val context: Context,
) : BleScannerDataSource {

  private val bluetoothManager: BluetoothManager? by lazy {
    context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
  }

  private val bluetoothAdapter: BluetoothAdapter?
    get() = bluetoothManager?.adapter

  private var activeCallback: ScanCallback? = null

  override fun hasRequiredPermissions(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_SCAN,
      ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.BLUETOOTH_CONNECT,
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
      ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }
  }

  override fun isBluetoothEnabled(): Boolean {
    return bluetoothAdapter?.isEnabled == true
  }

  @SuppressLint("MissingPermission")
  override fun startScan(): Flow<DiscoveredDevice> = callbackFlow {
    val adapter = bluetoothAdapter
    if (adapter == null || !adapter.isEnabled) {
      close(IllegalStateException("Bluetooth is not available or disabled"))
      return@callbackFlow
    }

    if (!hasRequiredPermissions()) {
      close(SecurityException("Bluetooth scan permissions are not granted"))
      return@callbackFlow
    }

    val scanner = adapter.bluetoothLeScanner
    if (scanner == null) {
      close(IllegalStateException("BLE Scanner is not available on this device"))
      return@callbackFlow
    }

    val scanCallback = object : ScanCallback() {
      override fun onScanResult(callbackType: Int, result: ScanResult?) {
        result?.let { scanResult ->
          val device = scanResult.device ?: return
          val address = device.address ?: return
          val rawName = scanResult.scanRecord?.deviceName ?: device.name
          val appleData = scanResult.scanRecord?.getManufacturerSpecificData(0x004C)
          val displayName = when {
            !rawName.isNullOrBlank() -> rawName.trim()
            appleData != null -> "Apple Device"
            else -> "Unknown Device"
          }
          val rssi = scanResult.rssi
          val estimatedDistance = DiscoveredDevice.calculateDistance(rssi)

          val discovered = DiscoveredDevice(
            macAddress = address,
            name = displayName,
            rssi = rssi,
            estimatedDistance = estimatedDistance,
            lastSeenTimestamp = System.currentTimeMillis(),
          )
          trySend(discovered)
        }
      }

      override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        results?.forEach { scanResult ->
          val device = scanResult.device ?: return@forEach
          val address = device.address ?: return@forEach
          val rawName = scanResult.scanRecord?.deviceName ?: device.name
          val appleData = scanResult.scanRecord?.getManufacturerSpecificData(0x004C)
          val displayName = when {
            !rawName.isNullOrBlank() -> rawName.trim()
            appleData != null -> "Apple Device"
            else -> "Unknown Device"
          }
          val rssi = scanResult.rssi
          val estimatedDistance = DiscoveredDevice.calculateDistance(rssi)

          val discovered = DiscoveredDevice(
            macAddress = address,
            name = displayName,
            rssi = rssi,
            estimatedDistance = estimatedDistance,
            lastSeenTimestamp = System.currentTimeMillis(),
          )
          trySend(discovered)
        }
      }

      override fun onScanFailed(errorCode: Int) {
        close(RuntimeException("BLE Scan failed with error code: $errorCode"))
      }
    }

    activeCallback = scanCallback

    val settings = ScanSettings.Builder()
      .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
      .setReportDelay(0)
      .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
      .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
      .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
      .build()

    try {
      scanner.startScan(null, settings, scanCallback)
    } catch (e: Exception) {
      close(e)
    }

    awaitClose {
      try {
        scanner.stopScan(scanCallback)
      } catch (_: SecurityException) {
      }
      activeCallback = null
    }
  }

  @SuppressLint("MissingPermission")
  override fun stopScan() {
    activeCallback?.let { callback ->
      try {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(callback)
      } catch (_: Exception) {
      }
      activeCallback = null
    }
  }
}
