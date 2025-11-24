package com.jpvj.controlcarrito.data.mqtt

import android.app.Application
import android.util.Log
import com.jpvj.controlcarrito.AppConfig
import com.jpvj.controlcarrito.SslSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject

class MqttManager(
    private val application: Application
) {

    private val tag = "AWS-IoT-MQTT"
    private val publishTag = "MQTT"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Distancia en cm (float nullable)
    private val _distanceFlow = MutableStateFlow<Float?>(null)
    val distanceFlow = _distanceFlow.asStateFlow()

    // Estado del carro (ADELANTE, ATRAS, STOP, etc.)
    private val _statusFlow = MutableStateFlow("STOP")
    val statusFlow = _statusFlow.asStateFlow()

    // Estado de luces (true = encendidas, false = apagadas, null = desconocido)
    private val _lightsFlow = MutableStateFlow<Boolean?>(null)
    val lightsFlow = _lightsFlow.asStateFlow()

    // Para trackear último estado de luces (opcional)
    var lastLightsState: Boolean = false

    private val TOPIC_CMD = "carro/instrucciones"
    private val TOPIC_DIST = AppConfig.MQTT_TOPIC_DISTANCE

    // Cliente MQTT
    private val client: MqttAndroidClient by lazy {
        MqttAndroidClient(
            application.applicationContext,
            AppConfig.MQTT_BROKER_URI,      // ssl://xxxx.amazonaws.com:8883
            AppConfig.MQTT_CLIENT_ID_ANDROID
        )
    }

    fun connectAndSubscribe() {
        Log.d(tag, "Iniciando conexión MQTT TLS a AWS IoT...")

        val options = MqttConnectOptions().apply {
            isCleanSession = true
            isAutomaticReconnect = true
            socketFactory = SslSocketFactory.getSocketFactory(application)
        }

        // Callback GLOBAL del cliente MQTT
        client.setCallback(object : MqttCallback {

            override fun connectionLost(cause: Throwable?) {
                Log.w(tag, "Conexión MQTT perdida: ${cause?.message}")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val payload = message.toString()
                Log.d(tag, "MQTT messageArrived: topic=$topic payload=$payload")

                if (topic == TOPIC_DIST) {
                    procesarTelemetria(payload)
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // No lo necesitamos por ahora
            }
        })

        try {
            client.connect(options, null, object : IMqttActionListener {

                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(tag, "MQTT conectado exitosamente a AWS IoT")
                    subscribeTelemetry()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(tag, "Error al conectar a AWS IoT MQTT: ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            Log.e(tag, "EXCEPCIÓN conectando MQTT AWS IoT: ${e.message}")
        }
    }

    private fun subscribeTelemetry() {
        val qos = 1

        try {
            client.subscribe(
                TOPIC_DIST,
                qos,
                null,
                object : IMqttActionListener {

                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(tag, "Suscrito a topic: $TOPIC_DIST")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(tag, "Falló suscripción: ${exception?.message}")
                    }
                }
            )
        } catch (e: MqttException) {
            Log.e(tag, "Error suscribiendo a AWS IoT: ${e.message}")
        }
    }

    /**
     * Procesa el JSON de telemetría enviado por la ESP32, ejemplo:
     * {"distancia": 23, "estado":"ADELANTE", "luces":1, "ts":12345}
     */
    private fun procesarTelemetria(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)

            // ---- Distancia ----
            val distancia = if (obj.has("distancia") && !obj.isNull("distancia")) {
                obj.getDouble("distancia").toFloat()
            } else {
                null
            }

            // ---- Estado ----
            // Normalizamos a mayúsculas para evitar líos de comparación
            val estadoRaw = obj.optString("estado", "STOP")
            val estado = estadoRaw.uppercase()

            // ---- Luces ----
            val lucesBool = when (obj.optInt("luces", -1)) {
                1 -> true
                0 -> false
                else -> null
            }

            scope.launch {
                _distanceFlow.emit(distancia)
                _statusFlow.emit(estado)
                if (lucesBool != null) {
                    _lightsFlow.emit(lucesBool)
                    lastLightsState = lucesBool
                }
            }

        } catch (e: Exception) {
            Log.e(tag, "Error parseando JSON telemetría: ${e.message}")

            // Fallback: podría venir solo la distancia en texto plano
            val fallback = jsonStr.toFloatOrNull()
            scope.launch { _distanceFlow.emit(fallback) }
        }
    }

    fun publishCommand(command: String) {

        when (command) {
            "LUCES_ON"  -> lastLightsState = true
            "LUCES_OFF" -> lastLightsState = false
        }

        if (!client.isConnected) {
            Log.e(publishTag, "publishCommand: Cliente MQTT NO conectado")
            return
        }

        try {
            client.publish(
                TOPIC_CMD,
                command.toByteArray(),
                1,
                false,
                null,
                object : IMqttActionListener {

                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(publishTag, "Comando publicado en $TOPIC_CMD → $command")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.e(
                            publishTag,
                            "Error al publicar comando: ${exception?.message}"
                        )
                    }
                }
            )
            Log.d(publishTag, "Comando enviado (publish invocado): $command")

        } catch (e: MqttException) {
            Log.e(publishTag, "EXCEPCIÓN en publishCommand: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            if (client.isConnected) client.disconnect()
        } catch (e: Exception) {
            Log.e(tag, "Error al desconectar MQTT: ${e.message}")
        }
    }
}