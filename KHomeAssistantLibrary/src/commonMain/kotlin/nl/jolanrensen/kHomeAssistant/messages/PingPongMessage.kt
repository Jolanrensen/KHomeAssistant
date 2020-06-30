package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Ping(
    override var id: Int = 0,
    override val type: String = "ping"
) : Message()

@Serializable
data class Pong(
    override var id: Int = 0,
    override val type: String = "pong"
) : ResultMessage() {
    override val result: JsonObject? = null
    override val success: Boolean = true
}