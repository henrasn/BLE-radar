package com.example.ble_radar.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BleSignalLevelTest {

  @Test
  fun fromRssi_boundaryValues_mapToExpectedSignalLevels() {
    assertEquals(BleSignalLevel.VERY_STRONG, BleSignalLevel.fromRssi(0))
    assertEquals(BleSignalLevel.VERY_STRONG, BleSignalLevel.fromRssi(-10))
    assertEquals(BleSignalLevel.VERY_STRONG, BleSignalLevel.fromRssi(-25))
    assertEquals(BleSignalLevel.VERY_STRONG, BleSignalLevel.fromRssi(-30))

    assertEquals(BleSignalLevel.STRONG, BleSignalLevel.fromRssi(-31))
    assertEquals(BleSignalLevel.STRONG, BleSignalLevel.fromRssi(-40))
    assertEquals(BleSignalLevel.STRONG, BleSignalLevel.fromRssi(-50))

    assertEquals(BleSignalLevel.MODERATE, BleSignalLevel.fromRssi(-51))
    assertEquals(BleSignalLevel.MODERATE, BleSignalLevel.fromRssi(-60))
    assertEquals(BleSignalLevel.MODERATE, BleSignalLevel.fromRssi(-70))

    assertEquals(BleSignalLevel.WEAK, BleSignalLevel.fromRssi(-71))
    assertEquals(BleSignalLevel.WEAK, BleSignalLevel.fromRssi(-75))
    assertEquals(BleSignalLevel.WEAK, BleSignalLevel.fromRssi(-80))

    assertEquals(BleSignalLevel.VERY_WEAK, BleSignalLevel.fromRssi(-81))
    assertEquals(BleSignalLevel.VERY_WEAK, BleSignalLevel.fromRssi(-85))
    assertEquals(BleSignalLevel.VERY_WEAK, BleSignalLevel.fromRssi(-90))

    assertEquals(BleSignalLevel.LOST, BleSignalLevel.fromRssi(-91))
    assertEquals(BleSignalLevel.LOST, BleSignalLevel.fromRssi(-100))
    assertEquals(BleSignalLevel.LOST, BleSignalLevel.fromRssi(-120))
  }

  @Test
  fun signalLevels_haveExpectedEnglishLabelsAndDistanceRanges() {
    assertEquals("Very Strong (Very Close)", BleSignalLevel.VERY_STRONG.label)
    assertEquals("< 1 meter", BleSignalLevel.VERY_STRONG.distanceRange)
    assertEquals("Very Strong (Very Close) < 1 meter", BleSignalLevel.VERY_STRONG.fullDescription)

    assertEquals("Strong (Close)", BleSignalLevel.STRONG.label)
    assertEquals("1 – 3 meters", BleSignalLevel.STRONG.distanceRange)
    assertEquals("Strong (Close) 1 – 3 meters", BleSignalLevel.STRONG.fullDescription)

    assertEquals("Moderate / Good", BleSignalLevel.MODERATE.label)
    assertEquals("3 – 10 meters", BleSignalLevel.MODERATE.distanceRange)
    assertEquals("Moderate / Good 3 – 10 meters", BleSignalLevel.MODERATE.fullDescription)

    assertEquals("Weak", BleSignalLevel.WEAK.label)
    assertEquals("10 – 20 meters", BleSignalLevel.WEAK.distanceRange)
    assertEquals("Weak 10 – 20 meters", BleSignalLevel.WEAK.fullDescription)

    assertEquals("Very Weak / Intermittent", BleSignalLevel.VERY_WEAK.label)
    assertEquals("> 20 meters", BleSignalLevel.VERY_WEAK.distanceRange)
    assertEquals("Very Weak / Intermittent > 20 meters (Out of range threshold)", BleSignalLevel.VERY_WEAK.fullDescription)

    assertEquals("Signal Lost", BleSignalLevel.LOST.label)
    assertEquals("Disconnected", BleSignalLevel.LOST.distanceRange)
    assertEquals("Signal Lost (Disconnected / Out of range)", BleSignalLevel.LOST.fullDescription)
  }

  @Test
  fun calculateDistance_delegatesToSignalLevelDistanceRange() {
    assertEquals("< 1 meter", DiscoveredDevice.calculateDistance(-20))
    assertEquals("1 – 3 meters", DiscoveredDevice.calculateDistance(-45))
    assertEquals("3 – 10 meters", DiscoveredDevice.calculateDistance(-65))
    assertEquals("10 – 20 meters", DiscoveredDevice.calculateDistance(-75))
    assertEquals("> 20 meters", DiscoveredDevice.calculateDistance(-85))
    assertEquals("Disconnected", DiscoveredDevice.calculateDistance(-95))
  }

  @Test
  fun discoveredDevice_isUnknown_identifiesNamelessAndDefaultDevices() {
    val emptyNameDevice = DiscoveredDevice(
      macAddress = "00:11:22:33:44:55",
      name = "",
      rssi = -60,
      estimatedDistance = "3 – 10 meters",
    )
    assertTrue(emptyNameDevice.isUnknown)

    val blankNameDevice = DiscoveredDevice(
      macAddress = "00:11:22:33:44:55",
      name = "   ",
      rssi = -60,
      estimatedDistance = "3 – 10 meters",
    )
    assertTrue(blankNameDevice.isUnknown)

    val unknownDevice = DiscoveredDevice(
      macAddress = "00:11:22:33:44:55",
      name = "Unknown Device",
      rssi = -60,
      estimatedDistance = "3 – 10 meters",
    )
    assertTrue(unknownDevice.isUnknown)

    val lowerUnknownDevice = DiscoveredDevice(
      macAddress = "00:11:22:33:44:55",
      name = "unknown device",
      rssi = -60,
      estimatedDistance = "3 – 10 meters",
    )
    assertTrue(lowerUnknownDevice.isUnknown)

    val namedDevice = DiscoveredDevice(
      macAddress = "00:11:22:33:44:55",
      name = "Nordic Beacon",
      rssi = -60,
      estimatedDistance = "3 – 10 meters",
    )
    assertFalse(namedDevice.isUnknown)
  }

  @Test
  fun discoveredDevice_signalLevelProperty_computesFromRssi() {
    val device = DiscoveredDevice(
      macAddress = "AA:BB:CC:11:22:33",
      name = "Test Sensor",
      rssi = -42,
      estimatedDistance = "1 – 3 meters",
    )
    assertEquals(BleSignalLevel.STRONG, device.signalLevel)
  }
}
