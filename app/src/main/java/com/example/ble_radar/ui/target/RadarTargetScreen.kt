package com.example.ble_radar.ui.target

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ble_radar.R
import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.theme.BleRadarTheme
import com.example.ble_radar.theme.RadarOnPrimaryContainer
import com.example.ble_radar.theme.RadarOnSecondary
import com.example.ble_radar.theme.RadarOnSurface
import com.example.ble_radar.theme.RadarOnSurfaceVariant
import com.example.ble_radar.theme.RadarOutline
import com.example.ble_radar.theme.RadarOutlineVariant
import com.example.ble_radar.theme.RadarPrimary
import com.example.ble_radar.theme.RadarPrimaryContainer
import com.example.ble_radar.theme.RadarSecondary
import com.example.ble_radar.theme.RadarSecondaryContainer
import com.example.ble_radar.theme.RadarSurface
import com.example.ble_radar.theme.RadarSurfaceContainerHigh
import com.example.ble_radar.theme.RadarSurfaceContainerHighest
import com.example.ble_radar.theme.RadarSurfaceContainerLow
import com.example.ble_radar.theme.RadarSurfaceContainerLowest
import com.example.ble_radar.theme.RadarSurfaceVariant
import com.example.ble_radar.theme.SignalLost
import com.example.ble_radar.theme.SignalModerate
import com.example.ble_radar.theme.SignalStrong
import com.example.ble_radar.theme.SignalVeryStrong
import com.example.ble_radar.theme.SignalVeryWeak
import com.example.ble_radar.theme.SignalWeak
import com.example.ble_radar.ui.scanner.barColor

@Composable
fun RadarTargetScreen(
  macAddress: String,
  modifier: Modifier = Modifier,
  viewModel: RadarTargetViewModel = hiltViewModel(
    key = macAddress,
    creationCallback = { factory: RadarTargetViewModel.Factory ->
      factory.create(macAddress)
    },
  ),
  onBackClick: () -> Unit = {},
  onNavigateToScanner: () -> Unit = {},
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

  RadarTargetContent(
    uiState = uiState,
    modifier = modifier,
    onBackClick = onBackClick,
    onToggleTracking = {
      if (uiState.isScanning) {
        viewModel.stopTracking()
      } else {
        val hasPermissions = permissions.all {
          ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermissions) {
          viewModel.startTracking()
        } else {
          permissionLauncher.launch(permissions)
        }
      }
    },
    onDismissError = viewModel::clearErrorMessage,
    onNavigateToScanner = onNavigateToScanner,
    onNavigateToHistory = onNavigateToHistory,
  )
}

@Composable
fun RadarTargetContent(
  uiState: RadarTargetUiState,
  modifier: Modifier = Modifier,
  onBackClick: () -> Unit = {},
  onToggleTracking: () -> Unit = {},
  onDismissError: () -> Unit = {},
  onNavigateToScanner: () -> Unit = {},
  onNavigateToHistory: () -> Unit = {},
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = RadarSurface,
    topBar = {
      RadarTargetTopBar(
        isLive = uiState.isScanning,
        onBackClick = onBackClick,
      )
    },
    bottomBar = {
      RadarTargetBottomNavBar(
        onScannerClick = onNavigateToScanner,
        onHistoryClick = onNavigateToHistory,
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
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                  contentDescription = stringResource(R.string.content_desc_dismiss_error),
                  tint = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
        }
      }

      item(key = "target_header") {
        TargetHeaderBanner(
          deviceName = uiState.deviceName,
          macAddress = uiState.targetMacAddress,
          isLive = uiState.isScanning,
          isUnknown = uiState.isUnknown,
        )
      }

      item(key = "telemetry_metrics") {
        PrimaryTelemetryCard(
          rssi = uiState.rssi,
          estimatedDistance = uiState.estimatedDistance,
          signalLevel = uiState.signalLevel,
          isLive = uiState.isScanning,
        )
      }

      item(key = "tracking_controls") {
        TrackingControlsCard(
          isScanning = uiState.isScanning,
          onToggleTracking = onToggleTracking,
        )
      }
    }
  }
}

@Composable
fun RadarTargetTopBar(
  isLive: Boolean,
  onBackClick: () -> Unit,
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
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.size(36.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.action_back),
            tint = RadarPrimary,
            modifier = Modifier.size(22.dp),
          )
        }

        Text(
          text = stringResource(R.string.title_radar_view),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = RadarPrimary,
          letterSpacing = (-0.5).sp,
        )

        if (isLive) {
          LiveBeaconPulseDot()
        }
      }
    }
  }
}

@Composable
fun LiveBeaconPulseDot(
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

@Composable
fun TargetHeaderBanner(
  deviceName: String,
  macAddress: String,
  isLive: Boolean,
  isUnknown: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = RadarSurfaceContainerHigh,
    border = BorderStroke(1.dp, RadarOutlineVariant.copy(alpha = 0.35f)),
    shadowElevation = 2.dp,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = if (isLive) RadarSecondaryContainer.copy(alpha = 0.20f) else RadarSurfaceContainerHighest,
        modifier = Modifier.align(Alignment.TopEnd),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .background(
                color = if (isLive) RadarSecondary else RadarOutline,
                shape = CircleShape,
              ),
          )
          Text(
            text = if (isLive) stringResource(R.string.badge_live) else stringResource(R.string.badge_paused),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isLive) RadarSecondary else RadarOutline,
            letterSpacing = 0.8.sp,
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = RadarPrimaryContainer)
            .clip(RoundedCornerShape(16.dp))
            .background(RadarSurfaceContainerHighest),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Sensors,
            contentDescription = null,
            tint = RadarPrimaryContainer,
            modifier = Modifier.size(32.dp),
          )
        }

        Text(
          text = deviceName,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = if (isUnknown) RadarOutline else RadarPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Surface(
          shape = CircleShape,
          color = RadarSurfaceContainerLowest.copy(alpha = 0.80f),
          border = BorderStroke(1.dp, RadarOutlineVariant.copy(alpha = 0.35f)),
        ) {
          Text(
            text = stringResource(R.string.target_mac_format, macAddress),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = RadarOnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
          )
        }
      }
    }
  }
}

@Composable
fun PrimaryTelemetryCard(
  rssi: Int,
  estimatedDistance: String,
  signalLevel: BleSignalLevel,
  isLive: Boolean,
  modifier: Modifier = Modifier,
) {
  val levelColor by animateColorAsState(
    targetValue = signalLevel.barColor,
    animationSpec = tween(400),
    label = "levelColorAnim",
  )

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = RadarSurfaceContainerHigh,
    border = BorderStroke(1.dp, RadarOutlineVariant.copy(alpha = 0.35f)),
    shadowElevation = 2.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.SignalCellularAlt,
              contentDescription = null,
              tint = levelColor,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = stringResource(R.string.header_raw_rssi),
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = RadarOnSurfaceVariant,
              letterSpacing = 0.8.sp,
            )
          }

          Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "$rssi",
              style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = levelColor,
            )
            Text(
              text = stringResource(R.string.label_dbm_unit),
              style = MaterialTheme.typography.titleMedium,
              fontFamily = FontFamily.Monospace,
              color = RadarOnSurface,
              modifier = Modifier.padding(bottom = 2.dp),
            )
          }
        }

        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Straighten,
              contentDescription = null,
              tint = RadarPrimaryContainer,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = stringResource(R.string.header_est_distance),
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = RadarOnSurfaceVariant,
              letterSpacing = 0.8.sp,
            )
          }

          Text(
            text = estimatedDistance,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = RadarPrimary,
          )
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(RadarOutlineVariant.copy(alpha = 0.35f)),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.WifiTethering,
            contentDescription = null,
            tint = RadarPrimaryContainer,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = stringResource(R.string.header_signal_category),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = RadarOnSurfaceVariant,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (isLive && signalLevel != BleSignalLevel.LOST) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .background(levelColor, CircleShape),
            )
          }
          Text(
            text = signalLevel.label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = levelColor,
          )
        }
      }

      SignalStrengthBar(signalLevel = signalLevel)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.legend_far),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          fontFamily = FontFamily.Monospace,
          color = SignalVeryWeak,
        )
        Text(
          text = stringResource(R.string.legend_mid),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          fontFamily = FontFamily.Monospace,
          color = SignalModerate,
        )
        Text(
          text = stringResource(R.string.legend_close),
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          fontFamily = FontFamily.Monospace,
          color = SignalVeryStrong,
        )
      }
    }
  }
}

@Composable
fun SignalStrengthBar(
  signalLevel: BleSignalLevel,
  modifier: Modifier = Modifier,
) {
  val segmentConfigs = listOf(
    Pair(SignalVeryWeak, BleSignalLevel.VERY_WEAK),
    Pair(SignalWeak, BleSignalLevel.WEAK),
    Pair(SignalModerate, BleSignalLevel.MODERATE),
    Pair(SignalStrong, BleSignalLevel.STRONG),
    Pair(SignalVeryStrong, BleSignalLevel.VERY_STRONG),
  )

  val currentRank = when (signalLevel) {
    BleSignalLevel.LOST -> 0
    BleSignalLevel.VERY_WEAK -> 1
    BleSignalLevel.WEAK -> 2
    BleSignalLevel.MODERATE -> 3
    BleSignalLevel.STRONG -> 4
    BleSignalLevel.VERY_STRONG -> 5
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .height(14.dp),
    shape = RoundedCornerShape(4.dp),
    color = RadarSurfaceContainerLowest.copy(alpha = 0.85f),
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(2.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      segmentConfigs.forEachIndexed { index, pair ->
        val segmentColor = pair.first
        val isSegmentActive = (index + 1) <= currentRank

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(2.dp))
            .background(
              if (isSegmentActive) {
                segmentColor
              } else {
                RadarSurfaceVariant.copy(alpha = 0.30f)
              },
            )
            .border(
              width = if (isSegmentActive) 0.dp else 0.5.dp,
              color = if (isSegmentActive) Color.Transparent else RadarOutlineVariant.copy(alpha = 0.35f),
              shape = RoundedCornerShape(2.dp),
            ),
        )
      }

      val isLost = signalLevel == BleSignalLevel.LOST
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxSize()
          .clip(RoundedCornerShape(2.dp))
          .background(
            if (isLost) {
              SignalLost
            } else {
              RadarSurfaceVariant.copy(alpha = 0.20f)
            },
          )
          .border(
            width = 0.5.dp,
            color = if (isLost) RadarOutline else RadarOutlineVariant.copy(alpha = 0.30f),
            shape = RoundedCornerShape(2.dp),
          ),
      )
    }
  }
}

@Composable
fun TrackingControlsCard(
  isScanning: Boolean,
  onToggleTracking: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
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
        Text(
          text = if (isScanning) stringResource(R.string.target_tracking_active) else stringResource(R.string.target_tracking_paused),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = RadarOnSurface,
        )
        Text(
          text = if (isScanning) stringResource(R.string.target_tracking_active_desc) else stringResource(R.string.target_tracking_paused_desc),
          style = MaterialTheme.typography.labelSmall,
          color = RadarOnSurfaceVariant,
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Button(
        onClick = onToggleTracking,
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
          text = if (isScanning) stringResource(R.string.btn_stop) else stringResource(R.string.btn_start),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
        )
      }
    }
  }
}

@Composable
fun RadarTargetBottomNavBar(
  onScannerClick: () -> Unit,
  onHistoryClick: () -> Unit,
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
      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(RadarSurfaceVariant.copy(alpha = 0.45f))
          .clickable(onClick = onScannerClick),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Radar,
            contentDescription = stringResource(R.string.nav_scanner),
            tint = RadarPrimaryContainer,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.nav_scanner).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = RadarPrimaryContainer,
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(RadarSurfaceContainerLowest)
          .clickable(onClick = onHistoryClick),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(R.string.nav_history),
            tint = RadarOnSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.nav_history).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = RadarOnSurfaceVariant,
          )
        }
      }
    }
  }
}

@Preview(name = "Radar Target - Very Strong (< 1m)", showBackground = true)
@Composable
fun RadarTargetVeryStrongPreview() {
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

@Preview(name = "Radar Target - Strong (1-3m)", showBackground = true)
@Composable
fun RadarTargetStrongPreview() {
  BleRadarTheme {
    RadarTargetContent(
      uiState = RadarTargetUiState(
        targetMacAddress = "7A:38:DE:51:B0:19",
        deviceName = "Apple AirTag (Living Room)",
        rssi = -42,
        estimatedDistance = "1 – 3 meters",
        signalLevel = BleSignalLevel.STRONG,
        isScanning = true,
      ),
    )
  }
}

@Preview(name = "Radar Target - Moderate (3-10m)", showBackground = true)
@Composable
fun RadarTargetModeratePreview() {
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

@Preview(name = "Radar Target - Weak (10-20m)", showBackground = true)
@Composable
fun RadarTargetWeakPreview() {
  BleRadarTheme {
    RadarTargetContent(
      uiState = RadarTargetUiState(
        targetMacAddress = "F2:A0:C3:7E:11:80",
        deviceName = "Smart Band 8 Active",
        rssi = -74,
        estimatedDistance = "10 – 20 meters",
        signalLevel = BleSignalLevel.WEAK,
        isScanning = false,
      ),
    )
  }
}

@Preview(name = "Radar Target - Signal Lost", showBackground = true)
@Composable
fun RadarTargetLostPreview() {
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
