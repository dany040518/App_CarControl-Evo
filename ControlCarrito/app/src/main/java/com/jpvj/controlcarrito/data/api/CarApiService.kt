package com.jpvj.controlcarrito.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class MoveRequest(
    @SerializedName("direction")
    val direction: String
)

data class HealthcheckResponse(
    @SerializedName("status")
    val status: String
)

interface CarApiService {

    @GET("api/v1/healthcheck")
    suspend fun getHealthcheck(): Response<HealthcheckResponse>

    @POST("api/v1/move")
    suspend fun move(@Body request: MoveRequest): Response<Unit>
}
