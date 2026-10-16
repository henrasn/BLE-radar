package com.example.ble_radar.di

import com.example.ble_radar.data.DataRepository
import com.example.ble_radar.data.DefaultDataRepository
import com.example.ble_radar.data.local.BleLocalDataSource
import com.example.ble_radar.data.local.BleLocalDataSourceImpl
import com.example.ble_radar.data.repository.BleDeviceRepository
import com.example.ble_radar.data.repository.BleDeviceRepositoryImpl
import com.example.ble_radar.data.scanner.AndroidBleScannerDataSource
import com.example.ble_radar.data.scanner.BleScannerDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds
  @Singleton
  abstract fun bindBleScannerDataSource(
    impl: AndroidBleScannerDataSource,
  ): BleScannerDataSource

  @Binds
  @Singleton
  abstract fun bindBleLocalDataSource(
    impl: BleLocalDataSourceImpl,
  ): BleLocalDataSource

  @Binds
  @Singleton
  abstract fun bindBleDeviceRepository(
    impl: BleDeviceRepositoryImpl,
  ): BleDeviceRepository

  @Binds
  @Singleton
  abstract fun bindDataRepository(
    impl: DefaultDataRepository,
  ): DataRepository
}
