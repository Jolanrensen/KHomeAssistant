package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import nl.jolanrensen.kHomeAssistant.JsonSerializable
import nl.jolanrensen.kHomeAssistant.StateResult

@Serializable
data class FetchStateMessage(
        override var id: Int = 0,
        override val type: String = "get_states"
) : Message(), JsonSerializable

@Serializable
data class FetchStateResponse(
        override var id: Int = 0,
        override val type: String = "result",
        override val success: Boolean,
        override val result: List<StateResult>? = null
) : ResultMessage(), JsonSerializable



