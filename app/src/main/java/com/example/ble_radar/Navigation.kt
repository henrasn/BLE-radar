package com.example.ble_radar

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.ble_radar.ui.scanner.ScannerScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Scanner)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Scanner> {
          ScannerScreen()
        }
      },
  )
}
