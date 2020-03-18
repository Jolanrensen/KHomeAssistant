package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
        val result: List<JsonObject>
) : Message
