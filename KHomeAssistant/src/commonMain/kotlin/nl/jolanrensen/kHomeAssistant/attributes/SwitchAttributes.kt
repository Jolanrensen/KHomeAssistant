package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject

@Serializable
data class SwitchAttributes(
        override val friendly_name: String
) : Attributes() {

    override lateinit var jsonObject: JsonObject

    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): SwitchAttributes = Json(JsonConfiguration(
                ignoreUnknownKeys = true,
                isLenient = true
        )).parse(serializer(), json).apply {
            jsonObject = Json.parseJson(json).jsonObject
        }
    }
}