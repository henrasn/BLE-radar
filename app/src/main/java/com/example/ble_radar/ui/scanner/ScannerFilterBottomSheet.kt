package com.example.ble_radar.ui.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble_radar.data.model.BleSignalLevel
import com.example.ble_radar.theme.BleRadarTheme
import com.example.ble_radar.theme.RadarOnSurface
import com.example.ble_radar.theme.RadarOnSurfaceVariant
import com.example.ble_radar.theme.RadarOutline
import com.example.ble_radar.theme.RadarOutlineVariant
import com.example.ble_radar.theme.RadarPrimary
import com.example.ble_radar.theme.RadarPrimaryContainer
import com.example.ble_radar.theme.RadarSurfaceContainer
import com.example.ble_radar.theme.RadarSurfaceContainerLow

/**
 * Stateful compose modal bottom sheet for search criteria and RSSI filtering.
 * Represents Stitch screen 179dd18e9c4843099423deaf6c0067d3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerFilterBottomSheet(
  filterState: ScannerFilterState,
  onSelectTarget: (SearchFilterTarget) -> Unit,
  onSelectSignalLevel: (BleSignalLevel?) -> Unit,
  onResetFilter: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = RadarSurfaceContainerLow,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = {
      // Drag Handle Pill
      Box(
        modifier = Modifier
          .padding(top = 12.dp, bottom = 8.dp)
          .size(width = 40.dp, height = 6.dp)
          .background(RadarOutlineVariant.copy(alpha = 0.5f), CircleShape),
      )
    },
    modifier = modifier,
  ) {
    ScannerFilterContent(
      filterState = filterState,
      onSelectTarget = { target ->
        onSelectTarget(target)
        if (target != SearchFilterTarget.RSSI) {
          onDismiss()
        }
      },
      onSelectSignalLevel = { level ->
        onSelectSignalLevel(level)
        onDismiss()
      },
      onResetFilter = onResetFilter,
    )
  }
}

/**
 * Stateless compose content displaying filter buttons and RSSI distance levels.
 */
@Composable
fun ScannerFilterContent(
  filterState: ScannerFilterState,
  onSelectTarget: (SearchFilterTarget) -> Unit,
  onSelectSignalLevel: (BleSignalLevel?) -> Unit,
  onResetFilter: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp)
      .padding(bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Header Row with Title and Reset Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Search Filter",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = RadarPrimary,
      )

      IconButton(
        onClick = onResetFilter,
        modifier = Modifier.size(28.dp),
      ) {
        Icon(
          imageVector = Icons.Default.RestartAlt,
          contentDescription = "Reset filter",
          tint = RadarOutline,
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Filter Criteria Buttons Stack (from Stitch screen 179dd18e9c4843099423deaf6c0067d3)
    SearchFilterTarget.entries.forEach { target ->
      val isSelected = filterState.target == target
      FilterOptionButton(
        title = target.displayName,
        isSelected = isSelected,
        onClick = { onSelectTarget(target) },
      )
    }

    // Sub-levels when RSSI is chosen
    if (filterState.target == SearchFilterTarget.RSSI) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Select Distance / Signal Strength:",
        style = MaterialTheme.typography.labelSmall,
        color = RadarOnSurfaceVariant,
        fontFamily = FontFamily.Monospace,
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        BleSignalLevel.entries.forEach { level ->
          val isLevelSelected = filterState.selectedSignalLevel == level
          SignalLevelOptionCard(
            level = level,
            isSelected = isLevelSelected,
            onClick = {
              if (isLevelSelected) {
                onSelectSignalLevel(null)
              } else {
                onSelectSignalLevel(level)
              }
            },
          )
        }
      }
    }
  }
}

/**
 * Filter option item button with active/inactive border and styling.
 */
@Composable
fun FilterOptionButton(
  title: String,
  isSelected: Boolean,
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
    border = BorderStroke(
      width = 1.dp,
      color = if (isSelected) {
        RadarPrimaryContainer.copy(alpha = 0.5f)
      } else {
        RadarOutlineVariant.copy(alpha = 0.3f)
      },
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Monospace,
        color = if (isSelected) RadarPrimary else RadarOnSurface,
      )

      if (isSelected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = RadarPrimaryContainer,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

/**
 * Detail card for specific RSSI distance level.
 */
@Composable
fun SignalLevelOptionCard(
  level: BleSignalLevel,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) RadarSurfaceContainerLow else RadarSurfaceContainer,
    border = BorderStroke(
      width = 1.dp,
      color = if (isSelected) {
        RadarPrimaryContainer.copy(alpha = 0.6f)
      } else {
        RadarOutlineVariant.copy(alpha = 0.2f)
      },
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = level.label,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = if (isSelected) RadarPrimary else RadarOnSurface,
        )
        Text(
          text = level.distanceRange,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = RadarOnSurfaceVariant,
        )
      }

      if (isSelected) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = RadarPrimaryContainer,
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

// ==========================================
// Previews
// ==========================================

@Preview(name = "Scanner Filter - Default All", showBackground = true)
@Composable
fun ScannerFilterDefaultPreview() {
  BleRadarTheme {
    ScannerFilterContent(
      filterState = ScannerFilterState(target = SearchFilterTarget.ALL),
      onSelectTarget = {},
      onSelectSignalLevel = {},
      onResetFilter = {},
    )
  }
}

@Preview(name = "Scanner Filter - RSSI Active", showBackground = true)
@Composable
fun ScannerFilterRssiPreview() {
  BleRadarTheme {
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
