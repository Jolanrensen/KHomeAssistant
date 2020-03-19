package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
abstract class Attributes {
    abstract val friendly_name: String
    abstract val jsonObject: JsonObject

    /** Allows the unsupported attributes to be retrieved using entity.getAttributes()["attribute_id"] */
    operator fun get(attribute_id: String) = jsonObject[attribute_id]
}

