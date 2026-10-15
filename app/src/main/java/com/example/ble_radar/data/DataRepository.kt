package com.example.ble_radar.data

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DataRepository {
  val data: Flow<List<String>>
}

class DefaultDataRepository @Inject constructor() : DataRepository {
  override val data: Flow<List<String>> = flow { emit(listOf("Android")) }
}
