package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

interface JsonSerializable

inline fun <reified S : JsonSerializable> S.toJson() = Json {
    ignoreUnknownKeys = true
    isLenient = true
}.encodeToString(serializer(), this)


@OptIn(InternalSerializationApi::class)
inline fun <reified S : JsonSerializable> fromJson(json: String): S = Json {
    ignoreUnknownKeys = true
    isLenient = true
}.decodeFromString(S::class.serializer(), json)

@OptIn(InternalSerializationApi::class)
inline fun <reified S : JsonSerializable> fromJson(json: JsonElement): S = Json {
    ignoreUnknownKeys = true
    isLenient = true
}.decodeFromJsonElement(S::class.serializer(), json)

fun <S : JsonSerializable> fromJson(json: String, serializer: KSerializer<S>): S = Json {
    ignoreUnknownKeys = true
    isLenient = true
}.decodeFromString(serializer, json)