package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import nl.jolanrensen.kHomeAssistant.JsonSerializable

@Serializable
data class Context(
    val id: String,
    val parent_id: String? = null,
    val user_id: String? = null
) : JsonSerializable