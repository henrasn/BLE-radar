package com.example.ble_radar.ui.scanner

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.theme.BleRadarTheme
import com.example.ble_radar.theme.RadarSurfaceContainerLow
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
class ScannerRoborazziTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun scannerContent_scanningState() {
    composeTestRule.setContent {
      BleRadarTheme {
        ScannerContent(
          uiState = ScannerUiState(
            isScanning = true,
            totalFoundCount = 14,
            devices = ScannerViewModel.DEFAULT_DEVICES,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun scannerContent_pausedState() {
    composeTestRule.setContent {
      BleRadarTheme {
        ScannerContent(
          uiState = ScannerUiState(
            isScanning = false,
            totalFoundCount = 14,
            devices = ScannerViewModel.DEFAULT_DEVICES,
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun scannerContent_emptySearchResultState() {
    composeTestRule.setContent {
      BleRadarTheme {
        ScannerContent(
          uiState = ScannerUiState(
            isScanning = true,
            totalFoundCount = 14,
            searchQuery = "UnknownPeripheral",
            devices = emptyList(),
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun scannerContent_activeFilterBadge() {
    composeTestRule.setContent {
      BleRadarTheme {
        ScannerContent(
          uiState = ScannerUiState(
            isScanning = true,
            totalFoundCount = 14,
            devices = ScannerViewModel.DEFAULT_DEVICES,
            filterState = ScannerFilterState(
              target = SearchFilterTarget.DEVICE_NAME,
            ),
          ),
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun scannerFilterContent_defaultBottomSheet() {
    composeTestRule.setContent {
      BleRadarTheme {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(RadarSurfaceContainerLow)
            .padding(top = 16.dp),
        ) {
          ScannerFilterContent(
            filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
            onSelectTarget = {},
            onSelectSignalLevel = {},
            onResetFilter = {},
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }

  @Test
  fun scannerFilterContent_rssiLevelBottomSheet() {
    composeTestRule.setContent {
      BleRadarTheme {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(RadarSurfaceContainerLow)
            .padding(top = 16.dp),
        ) {
          ScannerFilterContent(
            filterState = ScannerFilterState(
              target = SearchFilterTarget.RSSI,
              selectedSignalLevel = BleSignalLevel.VERY_STRONG,
            ),
            onSelectTarget = {},
            onSelectSignalLevel = {},
            onResetFilter = {},
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage()
  }
}
