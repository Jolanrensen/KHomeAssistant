package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.messages.Context

/**
 * This serializable class is about the same as an Entity and is wat is used to get the data for entities
 * */
@Serializable
data class StateResult(
    val entity_id: String,
    var state: String,
    var attributes: JsonObject,
    val last_changed: String,
    val last_updated: String,
    val context: Context
) {
    companion object {
        fun fromJson(json: String): StateResult = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.decodeFromString(serializer(), json)
    }
}