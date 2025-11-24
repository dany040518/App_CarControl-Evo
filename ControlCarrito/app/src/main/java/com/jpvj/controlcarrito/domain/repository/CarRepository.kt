package com.jpvj.controlcarrito.domain.repository

import com.jpvj.controlcarrito.domain.model.HealthStatus
import com.jpvj.controlcarrito.domain.model.MoveDirection
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    suspend fun getHealthStatus(): HealthStatus
    suspend fun move(direction: MoveDirection)

    // flujo con la distancia que viene por MQTT
    val distanceFlow: Flow<Float?>
}
