package nl.jolanrensen.kHomeAssistant.attributes

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import nl.jolanrensen.kHomeAssistant.helper.HSColor
import nl.jolanrensen.kHomeAssistant.helper.RGBColor
import nl.jolanrensen.kHomeAssistant.helper.XYColor

@Serializable
data class LightAttributes(
        override val friendly_name: String,
        val min_mireds: Int? = null,
        val max_mireds: Int? = null,
        val effect_list: List<String>? = null,
        val brightness: Int? = null,
        val hs_color: HSColor? = null,
        val rgb_color: RGBColor? = null,
        val xy_color: XYColor? = null,
        val white_value: Int? = null,
        val supported_features: Int
) : Attributes {

    companion object {
        @OptIn(ImplicitReflectionSerializer::class, UnstableDefault::class)
        fun fromJson(json: String): LightAttributes = Json(JsonConfiguration(
                ignoreUnknownKeys = true,
                isLenient = true
        )).parse(serializer(), json)
    }

}