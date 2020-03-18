package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.Context

@Serializable
data class FetchStateMessage(
        override val id: Int,
        val type: String = "get_states"
) : Message

@Serializable
data class FetchStateResponse(
        override val id: Int,
        val type: String = "result",
        val success: Boolean,
        val result: List<Result>
) : Message

@Serializable
data class Result(
        val entity_id: String,
        val state: String,
        val attributes: JsonObject, // specific to each domain
        val last_changed: String, // TODO change to datetime
        val last_updated: String, // TODO change to datetime
        val context: Context
) {
    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): Result = Json(JsonConfiguration(
                isLenient = true,
                ignoreUnknownKeys = true
        )).parse(serializer(), json)
    }
}


