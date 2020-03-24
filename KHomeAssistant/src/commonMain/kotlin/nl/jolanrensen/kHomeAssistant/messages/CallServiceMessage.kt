package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.JsonSerializable

@Serializable
data class CallServiceMessage(
        override var id: Int = 0,
        override val type: String = "call_service",
        val domain: String,
        val service: String,
        val service_data: JsonObject = JsonObject(mapOf())
) : Message()