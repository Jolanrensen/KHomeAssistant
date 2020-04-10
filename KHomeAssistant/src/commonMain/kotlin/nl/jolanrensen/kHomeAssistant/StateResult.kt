package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
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
    val last_changed: String, // TODO change to datetime
    val last_updated: String, // TODO change to datetime
    val context: Context
) {
    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): StateResult = Json(JsonConfiguration(
                isLenient = true,
                ignoreUnknownKeys = true
        )).parse(serializer(), json)
    }
}