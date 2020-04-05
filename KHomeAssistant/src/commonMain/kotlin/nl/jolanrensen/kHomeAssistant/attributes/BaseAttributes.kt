package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

interface BaseAttributes {
    val friendly_name: String

    /** Used to get unsupported attributes with the get function below. */
    var fullJsonObject: JsonObject

    /** Allows the unsupported attributes to be retrieved using entity.getAttributes()["attribute_id"] */
    operator fun get(attribute_id: String) = fullJsonObject[attribute_id]
}

@Serializable
class DefaultAttributes(
        override val friendly_name: String = ""
) : BaseAttributes {
    override var fullJsonObject: JsonObject = JsonObject(mapOf())
}


@OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
fun <A : BaseAttributes> attributesFromJson(json: JsonElement, serializer: KSerializer<A>): A =
        Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true)).fromJson(serializer, json).apply {
            fullJsonObject = json.jsonObject
        }

