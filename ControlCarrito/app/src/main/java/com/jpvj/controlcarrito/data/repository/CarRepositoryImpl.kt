package com.jpvj.controlcarrito.data.repository

import com.jpvj.controlcarrito.data.api.ApiClient
import com.jpvj.controlcarrito.data.api.MoveRequest
import com.jpvj.controlcarrito.data.mqtt.MqttManager
import com.jpvj.controlcarrito.domain.model.HealthStatus
import com.jpvj.controlcarrito.domain.model.MoveDirection
import com.jpvj.controlcarrito.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow

class CarRepositoryImpl(
    private val mqttManager: MqttManager
) : CarRepository {

    override suspend fun getHealthStatus(): HealthStatus {
        val response = ApiClient.carApiService.getHealthcheck()
        return if (response.isSuccessful) {
            val body = response.body()
            HealthStatus(
                isAlive = body?.status?.equals("ok", ignoreCase = true) == true,
                rawMessage = body?.status ?: "unknown"
            )
        } else {
            HealthStatus(
                isAlive = false,
                rawMessage = "HTTP ${response.code()}"
            )
        }
    }

    override suspend fun move(direction: MoveDirection) {

        val cmd = when (direction) {
            MoveDirection.FORWARD  -> "ADELANTE"
            MoveDirection.BACKWARD -> "ATRAS"
            MoveDirection.LEFT     -> "IZQUIERDA"
            MoveDirection.RIGHT    -> "DERECHA"
            MoveDirection.STOP     -> "STOP"

            MoveDirection.LIGHTS -> {
                val turningOn = !mqttManager.lastLightsState
                if (turningOn) "LUCES_ON" else "LUCES_OFF"
            }
        }

        mqttManager.publishCommand(cmd)
    }

    override val distanceFlow: Flow<Float?>
        get() = mqttManager.distanceFlow
}