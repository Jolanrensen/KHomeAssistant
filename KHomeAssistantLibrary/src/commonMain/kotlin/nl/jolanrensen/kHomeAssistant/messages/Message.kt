package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.JsonSerializable

abstract class Message : JsonSerializable {
    abstract var id: Int
    abstract val type: String
}

abstract class ResultMessage : Message(), JsonSerializable {
    abstract override val type: String
    abstract val success: Boolean
    abstract val result: Any?
    // maybe more
}

@Serializable
data class MessageBase(
        override var id: Int = 0,
        override val type: String
) : Message()

@Serializable
data class ResultMessageBase(
        override var id: Int = 0,
        override val type: String,
        override val success: Boolean,
        override val result: JsonObject? = null
) : ResultMessage()