package com.example.agropistachi.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AgroAlertaApiService {

    @GET("api/v1/scada/sync")
    suspend fun getLatestTelemetryAndAlerts(): Response<SyncDataResponse>

    @POST("api/v1/actuators/control")
    suspend fun controlActuator(@Body request: ActuatorControlRequest): Response<ActuatorControlResponse>
}
