package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer

@Serializable
abstract class Message {
    abstract val id: Int
    abstract val type: String
}

@Serializable
abstract class ResultMessage : Message() {
    // type: String = "result"
    abstract val success: Boolean
    abstract val result: Any?
    // maybe more
}

@Serializable
data class MessageBase(
        override val id: Int,
        override val type: String
) : Message()

@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified M : Message> M.toJson() = Json(JsonConfiguration(
        ignoreUnknownKeys = true,
        isLenient = true
)).stringify(serializer(), this)


@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified M : Message> fromJson(json: String): M = Json(JsonConfiguration(
        ignoreUnknownKeys = true,
        isLenient = true
)).parse(M::class.serializer(), json)