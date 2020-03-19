package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.StateResult

@Serializable
data class EventMessage(
        override val id: Int,
        override val type: String = "event",
        val event: Event
) : Message()


@Serializable
data class Event(
        val event_type: String, // aka "state_changed"
        val data: JsonObject, // EventDataStateChanged, EventDataCallService etc
        val origin: String, // aka "LOCAL"
        val time_fired: String, // TODO change to datetime
        val context: Context
) {
    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): Event = Json(JsonConfiguration(
                isLenient = true,
                ignoreUnknownKeys = true
        )).parse(serializer(), json)
    }
}

@Serializable
data class EventDataStateChanged(
        val entity_id: String,
        val old_state: StateResult,
        val new_state: StateResult
) {
    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): EventDataStateChanged = Json(JsonConfiguration(
                isLenient = true,
                ignoreUnknownKeys = true
        )).parse(serializer(), json)
    }
}

@Serializable
data class EventDataCallService(
        val domain: String,
        val service: String,
        val service_data: JsonObject
) {
    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): EventDataCallService = Json(JsonConfiguration(
                isLenient = true,
                ignoreUnknownKeys = true
        )).parse(serializer(), json)
    }
}

/** TODO maybe expand with either one of these
component_loaded
core_config_updated
device_registry_updated
entity_registry_updated
homeassistant_close
homeassistant_start
homeassistant_stop
lovelace_updated
panels_updated
persistent_notifications_updated
platform_discovered
service_registered
service_removed
sonoff_state
state_changed
themes_updated
time_changed
user_removed
 * */