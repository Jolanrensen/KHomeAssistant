package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import nl.jolanrensen.kHomeAssistant.JsonSerializable


@Serializable
data class AuthResponse(
        val type: String = "auth",
        val ha_version: String = "",
        val message: String = ""
): JsonSerializable {

    val isAuthRequired get() = type == "auth_required"
    val isAuthOk get() = type == "auth_ok"
    val isAuthInvalid get() = type == "auth_invalid"
}


@Serializable
data class AuthMessage(
        val type: String = "auth",
        val access_token: String
) : JsonSerializable

