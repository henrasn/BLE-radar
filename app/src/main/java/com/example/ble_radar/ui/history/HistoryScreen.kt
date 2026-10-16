package com.example.ble_radar.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ble_radar.R
import com.example.ble_radar.theme.BleRadarTheme
import com.example.ble_radar.theme.RadarOnSurfaceVariant
import com.example.ble_radar.theme.RadarOutline
import com.example.ble_radar.theme.RadarPrimary
import com.example.ble_radar.theme.RadarSecondary
import com.example.ble_radar.theme.RadarSurface
import com.example.ble_radar.theme.RadarSurfaceContainerLowest
import com.example.ble_radar.ui.components.BleDeviceCard
import com.example.ble_radar.ui.components.BleRadarBottomNavBar
import com.example.ble_radar.ui.scanner.BleDeviceItem
import com.example.ble_radar.ui.scanner.ScannerNavTab

@Composable
fun HistoryScreen(
  modifier: Modifier = Modifier,
  viewModel: HistoryViewModel = hiltViewModel(),
  onNavigateToScanner: () -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  DisposableEffect(Unit) {
    viewModel.stopActiveDiscovery()
    onDispose { }
  }

  HistoryContent(
    uiState = uiState,
    modifier = modifier,
    onNavigateToScanner = onNavigateToScanner,
  )
}

@Composable
fun HistoryContent(
  uiState: HistoryUiState,
  modifier: Modifier = Modifier,
  onNavigateToScanner: () -> Unit = {},
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = RadarSurface,
    topBar = {
      HistoryTopBar()
    },
    bottomBar = {
      BleRadarBottomNavBar(
        selectedTab = ScannerNavTab.HISTORY,
        onTabSelected = { tab ->
          if (tab == ScannerNavTab.SCANNER) {
            onNavigateToScanner()
          }
        },
      )
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp,
        bottom = 24.dp,
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (uiState.devices.isEmpty()) {
        item(key = "empty_history") {
          EmptyHistoryView()
        }
      } else {
        items(
          items = uiState.devices,
          key = { it.id },
        ) { device ->
          BleDeviceCard(
            device = device,
            showLastSaved = true,
          )
        }
      }
    }
  }
}

@Composable
fun HistoryTopBar(
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = RadarSurfaceContainerLowest.copy(alpha = 0.90f),
    shadowElevation = 4.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .height(56.dp)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = stringResource(R.string.title_device_history),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = RadarPrimary,
        letterSpacing = (-0.5).sp,
      )
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(RadarSecondary, CircleShape),
      )
    }
  }
}

@Composable
fun EmptyHistoryView(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 48.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(
        imageVector = Icons.Default.History,
        contentDescription = null,
        tint = RadarOutline,
        modifier = Modifier.size(48.dp),
      )
      Text(
        text = stringResource(R.string.empty_no_saved_devices),
        style = MaterialTheme.typography.bodyMedium,
        color = RadarOnSurfaceVariant,
      )
    }
  }
}

@Preview(name = "History Screen - Populated", showBackground = true)
@Composable
fun HistoryContentPopulatedPreview() {
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
            lastSeenTimestamp = System.currentTimeMillis(),
          ),
          BleDeviceItem(
            id = "7A:38:DE:51:B0:19",
            name = "Apple AirTag (Living Room)",
            macAddress = "7A:38:DE:51:B0:19",
            rssi = -42,
            estimatedDistance = "1 – 3 meters",
            lastSeenTimestamp = System.currentTimeMillis() - 60000L,
          ),
          BleDeviceItem(
            id = "24:6F:28:AE:55:82",
            name = "ESP32_Telemetry_Beacon",
            macAddress = "24:6F:28:AE:55:82",
            rssi = -64,
            estimatedDistance = "3 – 10 meters",
            lastSeenTimestamp = System.currentTimeMillis() - 3600000L,
          ),
          BleDeviceItem(
            id = "F2:A0:C3:7E:11:80",
            name = "Smart Band 8 Active",
            macAddress = "F2:A0:C3:7E:11:80",
            rssi = -74,
            estimatedDistance = "10 – 20 meters",
            lastSeenTimestamp = System.currentTimeMillis() - 86400000L,
          ),
          BleDeviceItem(
            id = "0000FE95-0000-1000-8000-00805F9B34FB",
            name = "Unknown Device",
            uuid = "0000FE95-0000-1000-8000-00805F9B34FB",
            rssi = -86,
            estimatedDistance = "> 20 meters",
            isUnknown = true,
            lastSeenTimestamp = System.currentTimeMillis() - 86400000L * 2,
          ),
        ),
      ),
    )
  }
}

@Preview(name = "History Screen - Empty", showBackground = true)
@Composable
fun HistoryContentEmptyPreview() {
  BleRadarTheme {
    HistoryContent(
      uiState = HistoryUiState(devices = emptyList()),
    )
  }
}
