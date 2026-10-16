package com.example.ble_radar.ui.history

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ble_radar.theme.BleRadarTheme
import com.example.ble_radar.ui.scanner.BleDeviceItem
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
class HistoryRoborazziTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun historyContent_populatedState() {
    composeTestRule.setContent {
      BleRadarTheme {
        HistoryContent(
          uiState = HistoryUiState(
            devices = listOf(
              BleDeviceItem(
                id = "E4:5F:01:9B:4A:2C",
                name = "Nordic nRF52840 Dongle",
                macAddress = "E4:5F:01:9B:4A:2C",
                rssi = -28,
                estimatedDistance = "< 1 meter",
                lastSeenTimestamp = 1729000000000L,
              ),
              BleDeviceItem(
                id = "7A:38:DE:51:B0:19",
                name = "Apple AirTag (Living Room)",
                macAddress = "7A:38:DE:51:B0:19",
                rssi = -42,
                estimatedDistance = "1 – 3 meters",
                lastSeenTimestamp = 1728990000000L,
              ),
              BleDeviceItem(
                id = "24:6F:28:AE:55:82",
                name = "ESP32_Telemetry_Beacon",
                macAddress = "24:6F:28:AE:55:82",
                rssi = -64,
                estimatedDistance = "3 – 10 meters",
                lastSeenTimestamp = 1728950000000L,
              ),
              BleDeviceItem(
                id = "F2:A0:C3:7E:11:80",
                name = "Smart Band 8 Active",
                macAddress = "F2:A0:C3:7E:11:80",
                rssi = -74,
                estimatedDistance = "10 – 20 meters",
                lastSeenTimestamp = 1728900000000L,
              ),
              BleDeviceItem(
                id = "0000FE95-0000-1000-8000-00805F9B34FB",
                name = "Unknown Device",
                uuid = "0000FE95-0000-1000-8000-00805F9B34FB",
                rssi = -86,
                estimatedDistance = "> 20 meters",
                isUnknown = true,
                lastSeenTimestamp = 1728800000000L,
              ),
            ),
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun historyContent_emptyState() {
    composeTestRule.setContent {
      BleRadarTheme {
        HistoryContent(
          uiState = HistoryUiState(
            devices = emptyList(),
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }
}
