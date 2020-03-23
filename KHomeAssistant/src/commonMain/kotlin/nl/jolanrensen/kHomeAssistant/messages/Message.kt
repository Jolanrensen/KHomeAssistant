package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
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
) : Message(), JsonSerializable

@Serializable
data class ResultMessageBase(
        override var id: Int = 0,
        override val success: Boolean,
        override val result: JsonObject? = null,
        override val type: String
) : ResultMessage(), JsonSerializable