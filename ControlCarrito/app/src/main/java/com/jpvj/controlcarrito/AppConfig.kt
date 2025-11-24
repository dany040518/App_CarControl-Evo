package com.jpvj.controlcarrito

object AppConfig {

    const val ESP32_BASE_URL = "http://172.20.10.3/"
    const val AWS_IOT_ENDPOINT_HOST ="a2hfirfjvweeu1-ats.iot.us-east-1.amazonaws.com"
    const val MQTT_BROKER_URI = "ssl://a2hfirfjvweeu1-ats.iot.us-east-1.amazonaws.com:8883"
    const val MQTT_CLIENT_ID_ANDROID = "android-controller-client"
    const val MQTT_TOPIC_DISTANCE = "carro/telemetria/distancia"
}
