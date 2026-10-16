package com.example.ble_radar.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble_radar.R
import com.example.ble_radar.theme.RadarOnSurface
import com.example.ble_radar.theme.RadarOnSurfaceVariant
import com.example.ble_radar.theme.RadarOutline
import com.example.ble_radar.theme.RadarOutlineVariant
import com.example.ble_radar.theme.RadarPrimary
import com.example.ble_radar.theme.RadarPrimaryFixedDim
import com.example.ble_radar.theme.RadarSurfaceContainer
import com.example.ble_radar.ui.scanner.BleDeviceItem
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BleDeviceCard(
  device: BleDeviceItem,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  showLastSaved: Boolean = false,
) {
  val clickModifier = if (onClick != null) {
    Modifier.clickable(onClick = onClick)
  } else {
    Modifier
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .then(clickModifier),
    shape = RoundedCornerShape(12.dp),
    color = RadarSurfaceContainer,
    shadowElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
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
              text = if (device.macAddress != null) stringResource(R.string.label_mac_prefix) else stringResource(R.string.label_uuid_prefix),
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
              text = stringResource(R.string.unit_dbm, device.rssi),
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
              text = stringResource(R.string.label_est_distance, device.estimatedDistance),
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = RadarOnSurfaceVariant,
            )
          }
        }
      }

      if (showLastSaved) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(RadarOutlineVariant.copy(alpha = 0.20f)),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = RadarPrimaryFixedDim,
            modifier = Modifier.size(15.dp),
          )

          val formattedTimestamp = formatLastSavedTime(device.lastSeenTimestamp)
          Text(
            text = stringResource(R.string.label_last_saved, formattedTimestamp),
            style = MaterialTheme.typography.labelSmall,
            color = RadarOnSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun formatLastSavedTime(timestamp: Long): String {
  val zoneId = ZoneId.systemDefault()
  val itemDate = Instant.ofEpochMilli(timestamp).atZone(zoneId)
  val now = ZonedDateTime.now(zoneId)
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
  val timeString = itemDate.format(timeFormatter)

  return when {
    itemDate.toLocalDate().isEqual(now.toLocalDate()) -> {
      stringResource(R.string.time_today, timeString)
    }
    itemDate.toLocalDate().isEqual(now.toLocalDate().minusDays(1)) -> {
      stringResource(R.string.time_yesterday, timeString)
    }
    else -> {
      itemDate.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
    }
  }
}
