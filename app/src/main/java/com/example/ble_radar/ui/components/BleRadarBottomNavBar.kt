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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Radar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ble_radar.R
import com.example.ble_radar.theme.RadarOnSurfaceVariant
import com.example.ble_radar.theme.RadarPrimaryContainer
import com.example.ble_radar.theme.RadarSurfaceContainerLowest
import com.example.ble_radar.theme.RadarSurfaceVariant
import com.example.ble_radar.ui.scanner.ScannerNavTab

@Composable
fun BleRadarBottomNavBar(
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
      val isScannerSelected = selectedTab == ScannerNavTab.SCANNER
      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isScannerSelected) RadarSurfaceVariant.copy(alpha = 0.45f) else RadarSurfaceContainerLowest,
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
            contentDescription = stringResource(R.string.nav_scanner),
            tint = if (isScannerSelected) RadarPrimaryContainer else RadarOnSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.nav_scanner).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = if (isScannerSelected) RadarPrimaryContainer else RadarOnSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      val isHistorySelected = selectedTab == ScannerNavTab.HISTORY
      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isHistorySelected) RadarSurfaceVariant.copy(alpha = 0.45f) else RadarSurfaceContainerLowest,
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
            contentDescription = stringResource(R.string.nav_history),
            tint = if (isHistorySelected) RadarPrimaryContainer else RadarOnSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.nav_history).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
            color = if (isHistorySelected) RadarPrimaryContainer else RadarOnSurfaceVariant,
          )
        }
      }
    }
  }
}
