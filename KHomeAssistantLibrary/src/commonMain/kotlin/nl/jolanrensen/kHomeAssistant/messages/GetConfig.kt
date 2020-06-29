package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import nl.jolanrensen.kHomeAssistant.JsonSerializable


@Serializable
data class GetConfig(
    override var id: Int = 0,
    override val type: String = "get_config"
) : Message()

@Serializable
data class GetConfigResult(
    override var id: Int = 0,
    override val type: String,
    override val success: Boolean,
    override val result: HassConfig
) : ResultMessage()

/** See https://www.home-assistant.io/docs/configuration/basic/ */
@Serializable
data class HassConfig(
    val location_name: String? = null,
    val name: String? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val elevation: Int? = null,
    val unit_system: UnitSystem? = null,
    val temperature_unit: String? = null,
    val time_zone: String? = null,
    val external_url: String? = null,
    val internal_url: String? = null,
    val components: List<String> = listOf(),
    val config_dir: String = "/config",
    val whitelist_external_dirs: List<String> = listOf(),
    val version: String,
    val config_source: String,
    val safe_mode: Boolean,
    val state: String
) : JsonSerializable

@Serializable
data class UnitSystem(
    val length: String = "km",
    val mass: String = "g",
    val pressure: String = "Pa",
    val temperature: String = "°C",
    val volume: String = "L"
) : JsonSerializable