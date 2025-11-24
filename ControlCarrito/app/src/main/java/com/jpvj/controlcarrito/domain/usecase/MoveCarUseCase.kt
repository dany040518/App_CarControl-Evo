package com.jpvj.controlcarrito.domain.usecase

import com.jpvj.controlcarrito.domain.model.MoveDirection
import com.jpvj.controlcarrito.domain.repository.CarRepository

class MoveCarUseCase(
    private val repository: CarRepository
) {
    suspend operator fun invoke(direction: MoveDirection) =
        repository.move(direction)
}
