package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.json.JsonObject


interface Attributes {
    val friendly_name: String

    val jsonObject: JsonObject

    /** Allows the unsupported attributes to be retrieved using entity.getAttributes()["attribute_id"] */
    operator fun get(attribute_id: String) = jsonObject[attribute_id]
}

