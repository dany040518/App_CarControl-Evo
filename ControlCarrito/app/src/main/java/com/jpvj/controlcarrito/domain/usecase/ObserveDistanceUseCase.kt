package com.jpvj.controlcarrito.domain.usecase

import com.jpvj.controlcarrito.domain.repository.CarRepository

class ObserveDistanceUseCase(
    private val repository: CarRepository
) {
    operator fun invoke() = repository.distanceFlow
}
