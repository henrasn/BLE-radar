package com.example.ble_radar.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ble_radar.theme.BleRadarTheme
import com.example.ble_radar.theme.RadarOnPrimaryContainer
import com.example.ble_radar.theme.RadarOnSecondary
import com.example.ble_radar.theme.RadarOnSurface
import com.example.ble_radar.theme.RadarOnSurfaceVariant
import com.example.ble_radar.theme.RadarOutline
import com.example.ble_radar.theme.RadarOutlineVariant
import com.example.ble_radar.theme.RadarPrimary
import com.example.ble_radar.theme.RadarPrimaryContainer
import com.example.ble_radar.theme.RadarPrimaryFixedDim
import com.example.ble_radar.theme.RadarSecondary
import com.example.ble_radar.theme.RadarSurface
import com.example.ble_radar.theme.RadarSurfaceContainer
import com.example.ble_radar.theme.RadarSurfaceContainerHigh
import com.example.ble_radar.theme.RadarSurfaceContainerLow
import com.example.ble_radar.theme.RadarSurfaceContainerLowest
import com.example.ble_radar.theme.RadarSurfaceVariant

/**
 * Stateful compose screen that manages ViewModel lifecycle, permissions, and state collection.
 */
@Composable
fun ScannerScreen(
  modifier: Modifier = Modifier,
  viewModel: ScannerViewModel = viewModel(
    factory = ScannerViewModel.createFactory(LocalContext.current),
  ),
  onDeviceClick: (BleDeviceItem) -> Unit = {},
  onNavigateToHistory: () -> Unit = {},
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.ACCESS_FINE_LOCATION,
    )
  } else {
    arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
  ) { results ->
    val allGranted = results.values.all { it }
    viewModel.onPermissionsResult(allGranted)
  }

  ScannerContent(
    uiState = uiState,
    modifier = modifier,
    onToggleScan = {
      if (uiState.isScanning) {
        viewModel.stopScan()
      } else {
        val hasPermissions = permissions.all {
          ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermissions) {
          viewModel.startScan()
        } else {
          permissionLauncher.launch(permissions)
        }
      }
    },
    onSearchQueryChange = viewModel::updateSearchQuery,
    onClearSearch = viewModel::clearSearchQuery,
    onDismissError = viewModel::clearErrorMessage,
    onFilterClick = viewModel::openFilterSheet,
    onResetFilter = viewModel::resetFilter,
    onDeviceClick = onDeviceClick,
    onNavTabClick = { tab ->
      viewModel.selectTab(tab)
      if (tab == ScannerNavTab.HISTORY) {
        onNavigateToHistory()
      }
    },
  )

  if (uiState.isFilterSheetOpen) {
    ScannerFilterBottomSheet(
      filterState = uiState.filterState,
      onSelectTarget = viewModel::setFilterTarget,
      onSelectSignalLevel = viewModel::setSignalLevelFilter,
      onResetFilter = viewModel::resetFilter,
      onDismiss = viewModel::closeFilterSheet,
    )
  }
}

/**
 * Stateless compose content displaying the BLE radar scanner UI.
 */
@Composable
fun ScannerContent(
  uiState: ScannerUiState,
  modifier: Modifier = Modifier,
  onToggleScan: () -> Unit = {},
  onSearchQueryChange: (String) -> Unit = {},
  onClearSearch: () -> Unit = {},
  onDismissError: () -> Unit = {},
  onFilterClick: () -> Unit = {},
  onResetFilter: () -> Unit = {},
  onDeviceClick: (BleDeviceItem) -> Unit = {},
  onNavTabClick: (ScannerNavTab) -> Unit = {},
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = RadarSurface,
    topBar = {
      ScannerTopBar()
    },
    bottomBar = {
      ScannerBottomNavBar(
        selectedTab = uiState.selectedTab,
        onTabSelected = onNavTabClick,
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
        top = 12.dp,
        bottom = 24.dp,
      ),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Error Banner
      if (uiState.errorMessage != null) {
        item(key = "error_banner") {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = uiState.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
              )
              IconButton(
                onClick = onDismissError,
                modifier = Modifier.size(24.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss error",
                  tint = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }

      // Diagnostic HUD
      item(key = "diagnostic_hud") {
        DiagnosticHudCard(
          isScanning = uiState.isScanning,
          totalFoundCount = uiState.totalFoundCount,
          onToggleScan = onToggleScan,
        )
      }

      // Search & Tactical Filter Section
      item(key = "search_section") {
        SearchBarSection(
          searchQuery = uiState.searchQuery,
          filterState = uiState.filterState,
          onQueryChange = onSearchQueryChange,
          onClearQuery = onClearSearch,
          onFilterClick = onFilterClick,
          onResetFilter = onResetFilter,
        )
      }

      // Telemetry Header
      item(key = "telemetry_header") {
        TelemetryHeader(
          foundCount = uiState.filteredDevices.size,
        )
      }

      // Real-Time Detected Devices Stack
      if (uiState.filteredDevices.isEmpty()) {
        item(key = "empty_state") {
          EmptyDevicesView(
            isSearching = uiState.searchQuery.isNotEmpty(),
          )
        }
      } else {
        items(
          items = uiState.filteredDevices,
          key = { it.id },
        ) { device ->
          BleDeviceCard(
            device = device,
            onClick = { onDeviceClick(device) },
          )
        }
      }
    }
  }
}

/**
 * Top App Bar with title and pulsing live indicator.
 */
@Composable
fun ScannerTopBar(
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
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "BLE Pulse Radar",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RadarPrimary,
          letterSpacing = (-0.5).sp,
        )

        // Pulsing radar live indicator
        LivePulseIndicator()
      }
    }
  }
}

/**
 * Pulsing live beacon dot indicator.
 */
@Composable
fun LivePulseIndicator(
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
  val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 2.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Restart,
    ),
    label = "pulse_scale",
  )
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.75f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Restart,
    ),
    label = "pulse_alpha",
  )

  Box(
    modifier = modifier.size(10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .scale(scale)
        .alpha(alpha)
        .background(RadarSecondary, CircleShape),
    )
    Box(
      modifier = Modifier
        .size(8.dp)
        .background(RadarSecondary, CircleShape),
    )
  }
}

/**
 * Diagnostic status HUD card with scan toggle button labeled START / STOP.
 */
@Composable
fun DiagnosticHudCard(
  isScanning: Boolean,
  totalFoundCount: Int,
  onToggleScan: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = RadarSurfaceContainerLow,
    shadowElevation = 2.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = if (isScanning) "Scanner Active" else "Scanner Standby",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = RadarOnSurface,
          )

          Box(
            modifier = Modifier
              .size(6.dp)
              .background(
                color = if (isScanning) RadarSecondary else RadarOutline,
                shape = CircleShape,
              ),
          )
        }

        val statusText = if (isScanning) {
          buildAnnotatedString {
            withStyle(SpanStyle(color = RadarOnSurfaceVariant)) {
              append("SCANNING: ")
            }
            withStyle(SpanStyle(color = RadarPrimary, fontWeight = FontWeight.Bold)) {
              append("$totalFoundCount DEVICES")
            }
            withStyle(SpanStyle(color = RadarOnSurfaceVariant)) {
              append(" FOUND")
            }
          }
        } else {
          buildAnnotatedString {
            withStyle(SpanStyle(color = RadarOnSurfaceVariant)) {
              append("STATUS: ")
            }
            withStyle(SpanStyle(color = RadarOutline, fontWeight = FontWeight.Bold)) {
              if (totalFoundCount > 0) {
                append("PAUSED ($totalFoundCount SAVED)")
              } else {
                append("READY (PRESS START)")
              }
            }
          }
        }

        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 0.5.sp,
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Button(
        onClick = onToggleScan,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isScanning) RadarPrimaryContainer else RadarSecondary,
          contentColor = if (isScanning) RadarOnPrimaryContainer else RadarOnSecondary,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp),
      ) {
        Icon(
          imageVector = if (isScanning) Icons.Default.StopCircle else Icons.Default.PlayArrow,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isScanning) "STOP" else "START",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
        )
      }
    }
  }
}

/**
 * Search input field and filter button.
 */
@Composable
fun SearchBarSection(
  searchQuery: String,
  filterState: ScannerFilterState = ScannerFilterState(),
  onQueryChange: (String) -> Unit,
  onClearQuery: () -> Unit,
  onFilterClick: () -> Unit,
  onResetFilter: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val isFilterActive = filterState.target != SearchFilterTarget.ALL || filterState.selectedSignalLevel != null
  val placeholder = when (filterState.target) {
    SearchFilterTarget.ALL -> "Search Name, MAC, or UUID..."
    SearchFilterTarget.DEVICE_NAME -> "Search Device Name..."
    SearchFilterTarget.MAC_ADDRESS -> "Search MAC Address..."
    SearchFilterTarget.RSSI -> "Search Signal Strength (RSSI)..."
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Search Box
      Surface(
        modifier = Modifier
          .weight(1f)
          .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = RadarSurfaceContainer,
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = RadarOutline,
            modifier = Modifier.size(20.dp),
          )

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (searchQuery.isEmpty()) {
              Text(
                text = placeholder,
                style = MaterialTheme.typography.labelMedium,
                color = RadarOutline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            BasicTextField(
              value = searchQuery,
              onValueChange = onQueryChange,
              textStyle = MaterialTheme.typography.labelMedium.copy(
                color = RadarOnSurface,
              ),
              cursorBrush = SolidColor(RadarPrimaryContainer),
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
          }

          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = onClearQuery,
              modifier = Modifier.size(28.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear search",
                tint = RadarOnSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }
      }

      // Filter Button
      Surface(
        modifier = Modifier
          .size(44.dp)
          .border(
            BorderStroke(
              width = 1.dp,
              color = if (isFilterActive) RadarPrimaryContainer else RadarOutlineVariant.copy(alpha = 0.35f),
            ),
            RoundedCornerShape(12.dp),
          )
          .clip(RoundedCornerShape(12.dp))
          .clickable(onClick = onFilterClick),
        color = RadarSurfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Search filter",
            tint = if (isFilterActive) RadarPrimaryContainer else RadarPrimaryFixedDim,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }

    // Active Filter Chip Indicator
    if (isFilterActive) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 2.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = RadarSurfaceContainerHigh,
          border = BorderStroke(1.dp, RadarPrimaryContainer.copy(alpha = 0.45f)),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            val filterDescription = buildString {
              append("Filter: ${filterState.target.displayName}")
              filterState.selectedSignalLevel?.let {
                append(" • ${it.distanceRange}")
              }
            }
            Text(
              text = filterDescription,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = RadarPrimary,
            )
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Reset filter",
              tint = RadarPrimaryContainer,
              modifier = Modifier
                .size(14.dp)
                .clickable(onClick = onResetFilter),
            )
          }
        }
      }
    }
  }
}

/**
 * Header row above the device list.
 */
@Composable
fun TelemetryHeader(
  foundCount: Int,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
        contentDescription = null,
        tint = RadarPrimaryContainer,
        modifier = Modifier.size(16.dp),
      )
      Text(
        text = "DETECTED DEVICES LIST",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = RadarOnSurfaceVariant,
        letterSpacing = 0.8.sp,
      )
    }

    Text(
      text = "$foundCount Found",
      style = MaterialTheme.typography.labelSmall,
      fontFamily = FontFamily.Monospace,
      color = RadarOutline,
    )
  }
}

/**
 * Individual device telemetry card.
 */
@Composable
fun BleDeviceCard(
  device: BleDeviceItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    color = RadarSurfaceContainer,
    shadowElevation = 1.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
      ) {
        Text(
          text = device.name,
          style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
          fontWeight = FontWeight.Bold,
          color = if (device.isUnknown) RadarOutline else RadarPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = if (device.macAddress != null) "MAC:" else "UUID:",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            color = RadarOnSurfaceVariant,
          )
          Text(
            text = device.macAddress ?: device.uuid.orEmpty(),
            style = MaterialTheme.typography.labelMedium.copy(
              fontSize = if (device.uuid != null) 11.sp else 12.sp,
            ),
            fontFamily = FontFamily.Monospace,
            color = RadarOnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "${device.rssi} dBm",
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = device.rssiColor,
          )

          Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = RadarOutlineVariant,
          )

          Text(
            text = "Est. Distance: ${device.estimatedDistance}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = RadarOnSurfaceVariant,
          )
        }
      }
    }
  }
}

/**
 * View displayed when no devices match the search or scanner buffer.
 */
@Composable
fun EmptyDevicesView(
  isSearching: Boolean,
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
        imageVector = Icons.Default.Radar,
        contentDescription = null,
        tint = RadarOutline,
        modifier = Modifier.size(48.dp),
      )
      Text(
        text = if (isSearching) "No matching devices found" else "No devices detected yet",
        style = MaterialTheme.typography.bodyMedium,
        color = RadarOnSurfaceVariant,
      )
    }
  }
}

/**
 * Bottom navigation bar with Scanner and History tabs.
 */
@Composable
fun ScannerBottomNavBar(
  selectedTab: ScannerNavTab,
  onTabSelected: (ScannerNavTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = RadarSurfaceContainerLowest.copy(alpha = 0.88f),
    shadowElevation = 8.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .height(64.dp)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Scanner Tab
      val isScannerActive = selectedTab == ScannerNavTab.SCANNER
      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isScannerActive) RadarSurfaceVariant.copy(alpha = 0.45f) else RadarSurfaceContainerLowest,
          )
          .clickable { onTabSelected(ScannerNavTab.SCANNER) },
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Radar,
            contentDescription = "Scanner",
            tint = if (isScannerActive) RadarPrimaryContainer else RadarOnSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "SCANNER",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = if (isScannerActive) RadarPrimaryContainer else RadarOnSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // History Tab
      val isHistoryActive = selectedTab == ScannerNavTab.HISTORY
      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isHistoryActive) RadarSurfaceVariant.copy(alpha = 0.45f) else RadarSurfaceContainerLowest,
          )
          .clickable { onTabSelected(ScannerNavTab.HISTORY) },
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "History",
            tint = if (isHistoryActive) RadarPrimaryContainer else RadarOnSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "HISTORY",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = if (isHistoryActive) RadarPrimaryContainer else RadarOnSurfaceVariant,
          )
        }
      }
    }
  }
}

// ==========================================
// Previews
// ==========================================

@Preview(name = "Scanner Content - Default Scanning State", showBackground = true)
@Composable
fun ScannerContentDefaultPreview() {
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

@Preview(name = "Scanner Content - Paused State", showBackground = true)
@Composable
fun ScannerContentPausedPreview() {
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

@Preview(name = "Scanner Content - Empty Search State", showBackground = true)
@Composable
fun ScannerContentEmptyPreview() {
  BleRadarTheme {
    ScannerContent(
      uiState = ScannerUiState(
        isScanning = true,
        totalFoundCount = 14,
        searchQuery = "Unknownxyz",
        devices = emptyList(),
      ),
    )
  }
}
