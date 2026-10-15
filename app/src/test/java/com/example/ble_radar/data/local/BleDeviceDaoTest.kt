package com.example.ble_radar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BleDeviceDaoTest {

  private lateinit var database: BleDatabase
  private lateinit var dao: BleDeviceDao

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, BleDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = database.bleDeviceDao()
  }

  @After
  fun closeDb() {
    database.close()
  }

  @Test
  fun upsert_insertsNewDeviceAndUpdatesExistingBasedOnMac() = runTest {
    val device1 = BleDeviceEntity(
      macAddress = "E4:5F:01:9B:4A:2C",
      name = "Nordic Dongle",
      rssi = -60,
      estimatedDistance = "~3.0m",
      lastSeenTimestamp = 1000L,
    )
    dao.upsert(device1)

    val list1 = dao.getAllDevices().first()
    assertEquals(1, list1.size)
    assertEquals(-60, list1[0].rssi)

    // Upsert with same MAC address updates the entity instead of inserting duplicate
    val device1Updated = BleDeviceEntity(
      macAddress = "E4:5F:01:9B:4A:2C",
      name = "Nordic Dongle",
      rssi = -35,
      estimatedDistance = "~0.5m",
      lastSeenTimestamp = 2000L,
    )
    dao.upsert(device1Updated)

    val list2 = dao.getAllDevices().first()
    assertEquals(1, list2.size)
    assertEquals(-35, list2[0].rssi)
    assertEquals("~0.5m", list2[0].estimatedDistance)
    assertEquals(2000L, list2[0].lastSeenTimestamp)
  }

  @Test
  fun getDeviceByMac_returnsCorrectDevice() = runTest {
    dao.upsert(
      BleDeviceEntity(
        macAddress = "AA:BB:CC:11:22:33",
        name = "Beacon Tag",
        rssi = -45,
        estimatedDistance = "~1.2m",
        lastSeenTimestamp = 500L,
      ),
    )

    val item = dao.getDeviceByMac("AA:BB:CC:11:22:33")
    assertNotNull(item)
    assertEquals("Beacon Tag", item?.name)
  }

  @Test
  fun clearAll_deletesAllRecords() = runTest {
    dao.upsert(
      BleDeviceEntity(
        macAddress = "MAC1",
        name = "Dev1",
        rssi = -50,
        estimatedDistance = "~2m",
        lastSeenTimestamp = 1L,
      ),
    )
    dao.upsert(
      BleDeviceEntity(
        macAddress = "MAC2",
        name = "Dev2",
        rssi = -60,
        estimatedDistance = "~4m",
        lastSeenTimestamp = 2L,
      ),
    )

    assertEquals(2, dao.getAllDevices().first().size)

    dao.clearAll()
    assertEquals(0, dao.getAllDevices().first().size)
  }
}
