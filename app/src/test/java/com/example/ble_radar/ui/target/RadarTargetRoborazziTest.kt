package com.example.ble_radar.ui.target

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.theme.BleRadarTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7, sdk = [34])
class RadarTargetRoborazziTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun radarTargetContent_veryStrongState() {
    composeTestRule.setContent {
      BleRadarTheme {
        RadarTargetContent(
          uiState = RadarTargetUiState(
            targetMacAddress = "E4:5F:01:9B:4A:2C",
            deviceName = "Nordic nRF52840 Dongle",
            rssi = -28,
            estimatedDistance = "< 1 meter",
            signalLevel = BleSignalLevel.VERY_STRONG,
            isScanning = true,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun radarTargetContent_moderateState() {
    composeTestRule.setContent {
      BleRadarTheme {
        RadarTargetContent(
          uiState = RadarTargetUiState(
            targetMacAddress = "24:6F:28:AE:55:82",
            deviceName = "ESP32_Telemetry_Beacon",
            rssi = -64,
            estimatedDistance = "3 – 10 meters",
            signalLevel = BleSignalLevel.MODERATE,
            isScanning = true,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun radarTargetContent_weakState() {
    composeTestRule.setContent {
      BleRadarTheme {
        RadarTargetContent(
          uiState = RadarTargetUiState(
            targetMacAddress = "F2:A0:C3:7E:11:80",
            deviceName = "Smart Band 8 Active",
            rssi = -74,
            estimatedDistance = "10 – 20 meters",
            signalLevel = BleSignalLevel.WEAK,
            isScanning = true,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun radarTargetContent_lostState() {
    composeTestRule.setContent {
      BleRadarTheme {
        RadarTargetContent(
          uiState = RadarTargetUiState(
            targetMacAddress = "0000FE95-0000-1000-8000-00805F9B34FB",
            deviceName = "Unknown Device",
            rssi = -95,
            estimatedDistance = "Disconnected",
            signalLevel = BleSignalLevel.LOST,
            isScanning = true,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun radarTargetContent_pausedTracking() {
    composeTestRule.setContent {
      BleRadarTheme {
        RadarTargetContent(
          uiState = RadarTargetUiState(
            targetMacAddress = "E4:5F:01:9B:4A:2C",
            deviceName = "Nordic nRF52840 Dongle",
            rssi = -28,
            estimatedDistance = "< 1 meter",
            signalLevel = BleSignalLevel.VERY_STRONG,
            isScanning = false,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }
}
