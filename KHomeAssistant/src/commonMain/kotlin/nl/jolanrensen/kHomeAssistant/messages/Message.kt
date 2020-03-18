package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer


interface Message {
    val id: Int
}

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