package com.jpvj.controlcarrito.domain.usecase

import com.jpvj.controlcarrito.domain.repository.CarRepository

class GetHealthStatusUseCase(
    private val repository: CarRepository
) {
    suspend operator fun invoke() = repository.getHealthStatus()
}
