package com.jpvj.controlcarrito.presentation.main

data class MainUiState(
    val statusText: String = "STOP",
    val distanceText: String = "-- cm",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lightsOn: Boolean = false
)

