package com.jpvj.controlcarrito.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jpvj.controlcarrito.data.mqtt.MqttManager
import com.jpvj.controlcarrito.data.repository.CarRepositoryImpl
import com.jpvj.controlcarrito.domain.model.MoveDirection
import com.jpvj.controlcarrito.domain.usecase.GetHealthStatusUseCase
import com.jpvj.controlcarrito.domain.usecase.MoveCarUseCase
import com.jpvj.controlcarrito.domain.usecase.ObserveDistanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val mqttManager = MqttManager(application)
    private val repository = CarRepositoryImpl(mqttManager)

    private val getHealthStatusUseCase = GetHealthStatusUseCase(repository)
    private val moveCarUseCase = MoveCarUseCase(repository)
    private val observeDistanceUseCase = ObserveDistanceUseCase(repository)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        // 1) Conectar a AWS IoT
        mqttManager.connectAndSubscribe()

        // 2) Observar distancia (vía caso de uso / repositorio)
        viewModelScope.launch {
            observeDistanceUseCase().collectLatest { distance ->
                _uiState.value = _uiState.value.copy(
                    distanceText = distance?.let { String.format("%.1f cm", it) } ?: "-- cm"
                )
            }
        }

        // 3) Observar estado del carro (ADELANTE, STOP, etc.) directamente desde MQTT
        viewModelScope.launch {
            mqttManager.statusFlow.collectLatest { status ->
                _uiState.value = _uiState.value.copy(
                    // Ajusta el nombre del campo si tu MainUiState lo llama distinto
                    statusText = status
                )
            }
        }

        // 4) Observar estado de luces (0/1 → false/true) desde la telemetría
        viewModelScope.launch {
            mqttManager.lightsFlow.collectLatest { lightsOn ->
                if (lightsOn != null) {
                    _uiState.value = _uiState.value.copy(
                        lightsOn = lightsOn
                    )
                }
            }
        }
    }

    fun move(direction: MoveDirection) {
        viewModelScope.launch {
            try {
                moveCarUseCase(direction)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            }
        }
    }

    fun toggleLights() {
        // Estado optimista en UI
        val newLightsOn = !_uiState.value.lightsOn
        _uiState.value = _uiState.value.copy(lightsOn = newLightsOn)

        // Movimiento / comando de luces (el ESP32 publica luego la telemetría real)
        move(MoveDirection.LIGHTS)
    }

    override fun onCleared() {
        super.onCleared()
        mqttManager.disconnect()
    }
}