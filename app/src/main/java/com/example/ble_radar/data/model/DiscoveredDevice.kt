package com.example.ble_radar.data.model

enum class BleSignalLevel(
  val label: String,
  val distanceRange: String,
  val fullDescription: String,
) {
  // -10 to -30 dBm Very Strong (Very Close) < 1 meter
  VERY_STRONG(
    label = "Very Strong (Very Close)",
    distanceRange = "< 1 meter",
    fullDescription = "Very Strong (Very Close) < 1 meter",
  ),

  // -30 to -50 dBm Strong (Close) 1 – 3 meters
  STRONG(
    label = "Strong (Close)",
    distanceRange = "1 – 3 meters",
    fullDescription = "Strong (Close) 1 – 3 meters",
  ),

  // -50 to -70 dBm Moderate / Good 3 – 10 meters
  MODERATE(
    label = "Moderate / Good",
    distanceRange = "3 – 10 meters",
    fullDescription = "Moderate / Good 3 – 10 meters",
  ),

  // -70 to -80 dBm Weak 10 – 20 meters
  WEAK(
    label = "Weak",
    distanceRange = "10 – 20 meters",
    fullDescription = "Weak 10 – 20 meters",
  ),

  // -80 to -90 dBm Very Weak / Intermittent > 20 meters (Out of range threshold)
  VERY_WEAK(
    label = "Very Weak / Intermittent",
    distanceRange = "> 20 meters",
    fullDescription = "Very Weak / Intermittent > 20 meters (Out of range threshold)",
  ),

  // < -90 dBm Signal Lost (Disconnected / Out of range)
  LOST(
    label = "Signal Lost",
    distanceRange = "Disconnected",
    fullDescription = "Signal Lost (Disconnected / Out of range)",
  );

  companion object {
    fun fromRssi(rssi: Int): BleSignalLevel {
      return when {
        rssi >= -30 -> VERY_STRONG
        rssi >= -50 -> STRONG
        rssi >= -70 -> MODERATE
        rssi >= -80 -> WEAK
        rssi >= -90 -> VERY_WEAK
        else -> LOST
      }
    }
  }
}

data class DiscoveredDevice(
  val macAddress: String,
  val name: String,
  val rssi: Int,
  val estimatedDistance: String,
  val lastSeenTimestamp: Long = System.currentTimeMillis(),
) {
  val isUnknown: Boolean
    get() = name.isBlank() || name.equals("Unknown Device", ignoreCase = true)

  val signalLevel: BleSignalLevel
    get() = BleSignalLevel.fromRssi(rssi)

  companion object {
    /**
     * Determines distance classification based on RSSI ranges:
     * -10 to -30 dBm: Very Strong (Very Close) < 1 meter
     * -30 to -50 dBm: Strong (Close) 1 – 3 meters
     * -50 to -70 dBm: Moderate / Good 3 – 10 meters
     * -70 to -80 dBm: Weak 10 – 20 meters
     * -80 to -90 dBm: Very Weak / Intermittent > 20 meters
     * < -90 dBm: Signal Lost (Disconnected / Out of range)
     */
    fun calculateDistance(rssi: Int): String {
      return BleSignalLevel.fromRssi(rssi).distanceRange
    }
  }
}
