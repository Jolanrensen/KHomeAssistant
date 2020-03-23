package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

interface JsonSerializable

@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified S : JsonSerializable> S.toJson() = Json(JsonConfiguration(
        ignoreUnknownKeys = true,
        isLenient = true
)).stringify(serializer(), this)


@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
inline fun <reified S : JsonSerializable> fromJson(json: String): S = Json(JsonConfiguration(
        ignoreUnknownKeys = true,
        isLenient = true
)).parse(S::class.serializer(), json)

//@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
//inline fun <reified S : JsonSerializable>  fromJson(json: String, serializer: KSerializer<S>): S = Json(JsonConfiguration(
//        ignoreUnknownKeys = true,
//        isLenient = true
//)).parse(serializer, json)