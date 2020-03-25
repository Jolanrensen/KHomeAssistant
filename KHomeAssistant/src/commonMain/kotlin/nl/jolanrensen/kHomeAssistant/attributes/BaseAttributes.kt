package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject

@Serializable
open class BaseAttributes {
    open val friendly_name: String = ""

    /** Used to get unsupported attributes with the get function below. */
    var fullJsonObject: JsonObject = JsonObject(mapOf())

    /** Allows the unsupported attributes to be retrieved using entity.getAttributes()["attribute_id"] */
    operator fun get(attribute_id: String) = fullJsonObject[attribute_id]
}


@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
fun <A : BaseAttributes> attributesFromJson(json: String, serializer: KSerializer<A>): A = Json(JsonConfiguration(
        ignoreUnknownKeys = true,
        isLenient = true
)).parse(serializer, json).apply {
    fullJsonObject = Json.parseJson(json).jsonObject
}

