package com.example.ble_radar

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Scanner : NavKey
@Serializable data class RadarTarget(val macAddress: String) : NavKey
@Serializable data object History : NavKey
@Serializable data object Main : NavKey
