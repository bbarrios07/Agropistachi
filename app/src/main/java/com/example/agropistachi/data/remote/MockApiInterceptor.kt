package com.example.agropistachi.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import com.google.gson.Gson

class MockApiInterceptor : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url().toString()

        // Simulate network latency (200ms)
        try { Thread.sleep(200) } catch (_: Exception) {}

        val jsonResponse = when {
            url.contains("api/v1/scada/sync") -> {
                val now = System.currentTimeMillis()
                val mockSync = SyncDataResponse(
                    status = "ONLINE_OK",
                    farmName = "Agrícola FRUTIS",
                    location = "Sector Buin, Región Metropolitana",
                    telemetry = listOf(
                        TelemetryDto("C1", soilHumidityPercent = 18.2, temperatureCelsius = -1.5, relativeHumidityPercent = 88.0, solarRadiationWM2 = 120.0, tankLevelPercent = 42.0, flowRateLps = 0.0, timestamp = now),
                        TelemetryDto("C2", soilHumidityPercent = 34.5, temperatureCelsius = 1.2, relativeHumidityPercent = 75.0, solarRadiationWM2 = 210.0, tankLevelPercent = 12.0, flowRateLps = 14.5, timestamp = now),
                        TelemetryDto("C3", soilHumidityPercent = 28.0, temperatureCelsius = 3.8, relativeHumidityPercent = 68.0, solarRadiationWM2 = 350.0, tankLevelPercent = 85.0, flowRateLps = 22.1, timestamp = now),
                        TelemetryDto("C4", soilHumidityPercent = 11.4, temperatureCelsius = -0.8, relativeHumidityPercent = 91.0, solarRadiationWM2 = 90.0, tankLevelPercent = 12.0, flowRateLps = 2.0, timestamp = now)
                    ),
                    actuators = listOf(
                        ActuatorDto("C1", electrovalvulaOpen = false, bombaImpulsionActive = false, sistemaAntiHeladasActive = true, timestamp = now),
                        ActuatorDto("C2", electrovalvulaOpen = true, bombaImpulsionActive = true, sistemaAntiHeladasActive = false, timestamp = now),
                        ActuatorDto("C3", electrovalvulaOpen = true, bombaImpulsionActive = true, sistemaAntiHeladasActive = false, timestamp = now),
                        ActuatorDto("C4", electrovalvulaOpen = false, bombaImpulsionActive = false, sistemaAntiHeladasActive = true, timestamp = now)
                    ),
                    alerts = listOf(
                        AlertDto(
                            id = 101,
                            cuartelId = "C1",
                            cuartelName = "Cuartel 1 - Paltos Hass",
                            type = "HELADA",
                            severity = "CRITICAL",
                            message = "Temperatura crítica en terreno: -1.5°C. Riesgo severo de helada en follaje.",
                            timestamp = now - 1000 * 60 * 15
                        ),
                        AlertDto(
                            id = 102,
                            cuartelId = "C2",
                            cuartelName = "Cuartel 2 - Cerezos Lapins",
                            type = "NIVEL_CRITICO_AGUA",
                            severity = "WARNING",
                            message = "Estanque acumulador en nivel bajo (12%). Se requiere carga preventiva.",
                            timestamp = now - 1000 * 60 * 45
                        ),
                        AlertDto(
                            id = 103,
                            cuartelId = "C4",
                            cuartelName = "Cuartel 4 - Cítricos Lemon",
                            type = "FUGA_OBSTRUCCION",
                            severity = "CRITICAL",
                            message = "Caída anómala de caudal (2.0 L/s). Posible obstrucción de filtros o fuga en matriz.",
                            timestamp = now - 1000 * 60 * 5
                        )
                    )
                )
                gson.toJson(mockSync)
            }
            url.contains("api/v1/actuators/control") -> {
                val mockResp = ActuatorControlResponse(
                    success = true,
                    message = "Comando ejecutado exitosamente en servidor SCADA Buin",
                    timestamp = System.currentTimeMillis()
                )
                gson.toJson(mockResp)
            }
            else -> "{}"
        }

        val mediaType = MediaType.parse("application/json")
        val responseBody = ResponseBody.create(mediaType, jsonResponse)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()
    }
}
