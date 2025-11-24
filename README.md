# App_CarControl-Evo

# CarControl Evo – Android App

Aplicación móvil desarrollada en **Kotlin + MVVM + Clean Architecture**, utilizada para controlar un carro IoT basado en ESP32 mediante **MQTT TLS (AWS IoT Core)** y obtener telemetría en tiempo real.

---

## Características principales

- Interfaz simple para controlar dirección: **adelante, atrás, izquierda, derecha, stop**.
- Control de **luces** (encendido/apagado) vía MQTT.
- Recepción en tiempo real de telemetría desde el carro:
  - Distancia desde el sensor ultrasónico.
  - Estado actual del carro (ADELANTE, STOP, IZQUIERDA…)
- Conexión segura a AWS IoT mediante **TLS + certificados X.509**.
- Arquitectura limpia, separada por capas: Domain, Data, Presentation.
- Uso de **coroutines + StateFlow** para UI reactiva.

---

## Arquitectura del proyecto

El proyecto sigue una combinación de **Clean Architecture + MVVM**.

```
presentation/
 └── main/
     ├── MainActivity.kt
     ├── MainViewModel.kt
     └── MainUiState.kt

domain/
 ├── model/MoveDirection.kt
 ├── repository/CarRepository.kt
 └── usecase/
      ├── MoveCarUseCase.kt
      ├── GetHealthStatusUseCase.kt
      └── ObserveDistanceUseCase.kt

data/
 ├── mqtt/MqttManager.kt
 ├── repository/CarRepositoryImpl.kt
 ├── api/RetrofitService.kt
 └── network/HealthcheckApi.kt
```

---

## Conexión con AWS IoT Core

La app usa **Paho MQTT Android Service** y certificados cargados manualmente.

- Protocol: `MQTTS (TLS)`
- Puerto: `8883`
- Topic comandos: `carro/instrucciones`
- Topic telemetría: `carro/telemetria/distancia`

Formato recibido desde la ESP32:

```json
{
  "distancia": 123,
  "estado": "ADELANTE",
  "luces": 1,
  "ts": 1712345678
}
```

---

## Librerías usadas en la App (Android)

### Core

- **Kotlin Coroutines** → Operaciones asíncronas y `StateFlow`.
- **AndroidX ViewModel** → Manejo del ciclo de vida + lógica UI.
- **StateFlow** → Flujo reactivo hacia la UI.

### MQTT / AWS IoT

- **Paho MQTT Android Client** → Publicación y suscripción TLS.
- **Custom SSLSocketFactory** → Certificados para AWS IoT.

### Networking REST

- **Retrofit + OkHttp** → Healthcheck al servidor HTTP del carro.
- **Gson / JSON** → Parsing de JSON.

---

## Flujo de Datos

### ➤ Comandos → Carro (Android → ESP32)

```
MainActivity → ViewModel → MoveCarUseCase → MqttManager.publishCommand()
```

### ➤ Telemetría ← Carro (ESP32 → Android)

```
ESP32 publica distancia → AWS IoT → MqttManager → ViewModel → UI
```

---

## Instalación y configuración

1. Importar el proyecto en **Android Studio Giraffe o superior**.
2. Pegar los certificados de AWS IoT en la carpeta: `app/src/main/res/raw/`.
3. Configurar `AppConfig.kt` con:
   - Endpoint AWS
   - Certificados
   - Topics MQTT
4. Activar **modo desarrollador** y conectar el teléfono por USB.
5. Ejecutar la app.

---

## Limitaciones actuales

- El carro solo envía **distancia**, **estado** y **luces** (sin velocidad real ni batería).
- Si la conexión WiFi cae, AWS IoT tarda ~5–10 segundos en reconectar.
- La app no muestra todavía logs avanzados ni errores detallados.
- La API REST del carro solo tiene `/healthcheck`.
- No se implementa autenticación JWT para el server HTTP local.

---

## Licencia

Proyecto académico para control remoto IoT – Universidad de La Sabana.

---

## Autores

**Danna Sánchez / Ingeniería Informática**

**Juan Pablo Vargas / Ingeniería Informática**
