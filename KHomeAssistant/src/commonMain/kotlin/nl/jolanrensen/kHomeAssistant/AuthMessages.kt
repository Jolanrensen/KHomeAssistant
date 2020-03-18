package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


@Serializable
data class AuthResponse(
        val type: String = "auth",
        val ha_version: String = "",
        val message: String = ""
) {

    val isAuthRequired get() = type == "auth_required"
    val isAuthOk get() = type == "auth_ok"
    val isAuthInvalid get() = type == "auth_invalid"

    companion object {
        @OptIn(UnstableDefault::class)
        fun fromJson(json: String): AuthResponse = Json(JsonConfiguration(ignoreUnknownKeys = true)).parse(serializer(), json)
    }
}


@Serializable
data class AuthMessage(
        val type: String = "auth",
        val access_token: String
) {
    fun toJson() = Json(JsonConfiguration.Stable)
            .stringify(serializer(), this)
}

