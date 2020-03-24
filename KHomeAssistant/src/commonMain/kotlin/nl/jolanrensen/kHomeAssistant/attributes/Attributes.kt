package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject

//@Serializable
abstract class Attributes {
    abstract val friendly_name: String
    abstract var fullJsonObject: JsonObject

    /** Allows the unsupported attributes to be retrieved using entity.getAttributes()["attribute_id"] */
    operator fun get(attribute_id: String) = fullJsonObject[attribute_id]
}

@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
fun <A: Attributes> attributesFromJson(json: String, serializer: KSerializer<A>): A = Json(JsonConfiguration(
        ignoreUnknownKeys = true,
        isLenient = true
)).parse(serializer, json).apply {
    fullJsonObject = Json.parseJson(json).jsonObject
}

