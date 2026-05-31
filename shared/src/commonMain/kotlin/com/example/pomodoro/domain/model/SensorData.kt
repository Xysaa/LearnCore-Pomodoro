package com.example.pomodoro.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * ESP32 mengirim 5 tipe JSON via BLE Notify:
 *
 * 1. STATUS (heartbeat setiap 5 detik):
 *    {"type":"status","running":bool,"paused":bool,"pausedBySensor":bool,
 *     "phase":"focus/short/long/idle","secsLeft":int,"sess":int,"cycles":int,
 *     "vol":int,"adc":int,"airBad":bool,"user":bool,"cm":float,"dfOk":bool}
 *
 * 2. AIR (setiap 8 detik):
 *    {"type":"air","adc":int,"airBad":bool}
 *
 * 3. USER (event saat terdeteksi/pergi):
 *    {"type":"user","present":bool,"cm":float}
 *
 * 4. SENSOR (update periodik sensor):
 *    {"type":"sensor","cm":float,"user":bool,"adc":int,"airBad":bool}
 *
 * 5. MSG (pesan teks dari ESP32, misal penolakan start):
 *    {"type":"msg","msg":"..."}
 */
sealed class EspMessage {

    /** Status lengkap timer + sensor */
    data class Status(
        val running: Boolean,
        val paused: Boolean,
        val pausedBySensor: Boolean,
        val phase: EspPhase,
        val secsLeft: Int,
        val sess: Int,
        val cycles: Int,
        val vol: Int,
        val adc: Int,           // ADC raw MQ-135 (0–4095)
        val airBad: Boolean,
        val userPresent: Boolean,
        val distanceCm: Float,
        val dfOk: Boolean
    ) : EspMessage()

    /** Update kualitas udara */
    data class Air(
        val adc: Int,
        val airBad: Boolean
    ) : EspMessage()

    /** Event kehadiran user */
    data class User(
        val present: Boolean,
        val distanceCm: Float
    ) : EspMessage()

    /** Update sensor periodik */
    data class Sensor(
        val distanceCm: Float,
        val userPresent: Boolean,
        val adc: Int,
        val airBad: Boolean
    ) : EspMessage()

    /** Pesan teks dari ESP32 (penolakan start, info, dst) */
    data class Msg(val text: String) : EspMessage()

    /** JSON tidak dikenali / error parse */
    object Unknown : EspMessage()
}

enum class EspPhase { IDLE, FOCUS, SHORT_REST, LONG_REST }

private val jsonParser = Json { ignoreUnknownKeys = true }

/** Parse raw JSON string dari BLE notify → EspMessage */
fun parseEspMessage(raw: String): EspMessage {
    return try {
        val obj = jsonParser.decodeFromString(JsonObject.serializer(), raw)
        when (obj["type"]?.jsonPrimitive?.contentOrNull) {
            "status" -> EspMessage.Status(
                running        = obj["running"]?.jsonPrimitive?.booleanOrNull       ?: false,
                paused         = obj["paused"]?.jsonPrimitive?.booleanOrNull         ?: false,
                pausedBySensor = obj["pausedBySensor"]?.jsonPrimitive?.booleanOrNull ?: false,
                phase          = when (obj["phase"]?.jsonPrimitive?.contentOrNull) {
                    "focus" -> EspPhase.FOCUS
                    "short" -> EspPhase.SHORT_REST
                    "long"  -> EspPhase.LONG_REST
                    else    -> EspPhase.IDLE
                },
                secsLeft    = obj["secsLeft"]?.jsonPrimitive?.intOrNull     ?: 0,
                sess        = obj["sess"]?.jsonPrimitive?.intOrNull         ?: 1,
                cycles      = obj["cycles"]?.jsonPrimitive?.intOrNull       ?: 4,
                vol         = obj["vol"]?.jsonPrimitive?.intOrNull          ?: 20,
                adc         = obj["adc"]?.jsonPrimitive?.intOrNull          ?: 0,
                airBad      = obj["airBad"]?.jsonPrimitive?.booleanOrNull   ?: false,
                userPresent = (obj["userPresent"] ?: obj["user"])
                                  ?.jsonPrimitive?.booleanOrNull            ?: true,
                distanceCm  = obj["cm"]?.jsonPrimitive?.contentOrNull
                                  ?.toFloatOrNull()                         ?: -1f,
                dfOk        = obj["dfOk"]?.jsonPrimitive?.booleanOrNull     ?: false
            )
            "air" -> EspMessage.Air(
                adc    = obj["adc"]?.jsonPrimitive?.intOrNull             ?: 0,
                airBad = obj["airBad"]?.jsonPrimitive?.booleanOrNull      ?: false
            )
            "user" -> EspMessage.User(
                present    = obj["present"]?.jsonPrimitive?.booleanOrNull ?: true,
                distanceCm = obj["cm"]?.jsonPrimitive?.contentOrNull
                                 ?.toFloatOrNull()                        ?: -1f
            )
            "sensor" -> EspMessage.Sensor(
                distanceCm  = obj["cm"]?.jsonPrimitive?.contentOrNull
                                  ?.toFloatOrNull()                       ?: -1f,
                userPresent = (obj["user"])?.jsonPrimitive?.booleanOrNull  ?: false,
                adc         = obj["adc"]?.jsonPrimitive?.intOrNull         ?: 0,
                airBad      = obj["airBad"]?.jsonPrimitive?.booleanOrNull  ?: false
            )
            "msg" -> EspMessage.Msg(
                text = obj["msg"]?.jsonPrimitive?.contentOrNull ?: ""
            )
            else -> EspMessage.Unknown
        }
    } catch (e: Exception) {
        EspMessage.Unknown
    }
}

/** Perintah yang dikirim App → ESP32 via BLE Write */
object EspCommand {
    fun start(
        focusMin: Int = 25,
        shortMin: Int = 5,
        longMin: Int  = 15,
        cycles: Int   = 4,
        volume: Int   = 20
    ) = """{"cmd":"start","focus":$focusMin,"short":$shortMin,"long_":$longMin,"cycles":$cycles,"vol":$volume}"""

    fun pause()  = """{"cmd":"pause"}"""
    fun resume() = """{"cmd":"resume"}"""
    fun reset()  = """{"cmd":"reset"}"""

    fun config(
        focusMin: Int,
        shortMin: Int,
        longMin: Int,
        cycles: Int,
        volume: Int
    ) = """{"cmd":"config","focus":$focusMin,"short":$shortMin,"long_":$longMin,"cycles":$cycles,"vol":$volume}"""

    fun volume(value: Int) = """{"cmd":"volume","value":$value}"""
    fun audio(track: Int)  = """{"cmd":"audio","track":$track}"""
}
