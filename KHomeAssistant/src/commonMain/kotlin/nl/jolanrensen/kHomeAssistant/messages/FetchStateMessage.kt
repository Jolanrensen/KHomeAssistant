package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.StateResult

@Serializable
data class FetchStateMessage(
        override val id: Int,
        override val type: String = "get_states"
) : Message()

@Serializable
data class FetchStateResponse(
        override val id: Int,
        override val type: String = "result",
        override val success: Boolean,
        override val result: List<StateResult>? = null
) : ResultMessage()



