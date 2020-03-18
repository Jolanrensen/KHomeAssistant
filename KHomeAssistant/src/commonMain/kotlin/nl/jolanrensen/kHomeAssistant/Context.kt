package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.Serializable

@Serializable
data class Context(
        val id: String,
        val parent_id: String? = null,
        val user_id: String? = null
)