package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.helper.*

/** Do not use directly! Always use Light. */
object Light : Domain<Light.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "light"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Light.' from a KHomeAssistantContext instead of using LightDomain directly.""".trimMargin()
    }

    /** Does the same as LightEntity() */
    override fun Entity(name: String): Entity = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
            override val kHomeAssistant: () -> KHomeAssistant?,
            override val name: String
    ) : ToggleEntity<Entity.Attributes>(
            kHomeAssistant = kHomeAssistant,
            name = name,
            domain = Light // TODO check?
    ) {

        @Serializable
        data class Attributes(
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
        ) : BaseAttributes {
            override var fullJsonObject: JsonObject = JsonObject(mapOf())
        }

        override val attributesSerializer: KSerializer<Attributes> = Attributes.serializer()

        //
//        fun onTestCase(callback: suspend Entity.() -> Unit) {
//            registerStateListener({ it == OnOff.ON }, callback)
//        }
        enum class Flash(val value: String) {
            SHORT("short"), LONG("long")
        }

        suspend fun turnOn(
                transition: Int? = null,
                profile: String? = null,
                hs_color: HSColor? = null,
                xy_color: XYColor? = null,
                rgb_color: RGBColor? = null,
                white_value: Int? = null,
                color_temp: Int? = null,
                kelvin: Int? = null,
                color_name: String? = null,
                brightness: Int? = null,
                brightness_pct: Float? = null,
                brightness_step: Float? = null,
                brightness_step_pct: Float? = null,
                flash: Flash? = null,
                effect: String? = null
        ) {
            val attributes = getAttributes()
            val data = hashMapOf<String, JsonElement>().apply {
                transition?.let {
                    if (it < 0)
                        throw IllegalArgumentException("incorrect transition $it")
                    this["transition"] = JsonPrimitive(it)
                }
                profile?.let {
                    this["profile"] = JsonPrimitive(it)
                }
                hs_color?.let {
                    if (it.isEmpty() || it.size > 2 || it.h !in 0f..360f || it.s !in 0f..100f)
                        throw IllegalArgumentException("incorrect hs_color $it")
                    this["hs_color"] = JsonArray(it.map { JsonPrimitive(it) })
                }
                xy_color?.let {
                    if (it.isEmpty() || it.size > 2)
                        throw IllegalArgumentException("incorrect xy_color $it")
                    this["xy_color"] = JsonArray(it.map { JsonPrimitive(it) })
                }
                rgb_color?.let {
                    if (it.isEmpty() || it.size > 3 || it.any { it !in 0..255 })
                        throw IllegalArgumentException("incorrect rgb_color $it")
                    this["rgb_color"] = JsonArray(it.map { JsonPrimitive(it) })
                }
                white_value?.let {
                    if (it !in 0..255)
                        throw IllegalArgumentException("incorrect white_value $it")
                    this["white_value"] = JsonPrimitive(it)
                }
                color_temp?.let {
                    if (attributes.min_mireds == null || attributes.max_mireds == null)
                        throw IllegalArgumentException("mireds not supported for this device")
                    if (it !in attributes.min_mireds..attributes.max_mireds)
                        throw IllegalArgumentException("incorrect color_temp $it")
                    this["color_temp"] = JsonPrimitive(it)
                }
                kelvin?.let {
                    if (it < 0)
                        throw IllegalArgumentException("incorrect kelvin $it")
                    this["kelvin"] = JsonPrimitive(it)
                }
                color_name?.let {
                    // TODO check color name https://www.w3.org/TR/css-color-3/#svg-color
                    this["color_name"] = JsonPrimitive(it)
                }
                brightness?.let {
                    if (it !in 0..255)
                        throw IllegalArgumentException("incorrect brightness $it")
                    this["brightness"] = JsonPrimitive(it)
                }
                brightness_pct?.let {
                    if (it !in 0f..100f)
                        throw IllegalArgumentException("incorrect brightness_pct $it")
                    this["brightness_pct"] = JsonPrimitive(it)
                }
                brightness_step?.let {
                    if (it !in -255f..255f)
                        throw IllegalArgumentException("incorrect brightness_step $it")
                    this["brightness_step"] = JsonPrimitive(it)
                }
                brightness_step_pct?.let {
                    if (it !in -100f..100f)
                        throw IllegalArgumentException("incorrect brightness_step_pct $it")
                    this["brightness_step_pct"] = JsonPrimitive(it)
                }
                flash?.let {
                    this["flash"] = JsonPrimitive(it.value)
                }
                effect?.let {
                    if (attributes.effect_list == null || it !in attributes.effect_list)
                        throw IllegalArgumentException("incorrect effect $it")
                    this["effect"] = JsonPrimitive(it)
                }
            }
            callService(
                    serviceName = "turn_on",
                    data = data
            )
        }

        suspend fun turnOff(transition: Int? = null) {
            val data = hashMapOf<String, JsonElement>().apply {
                transition?.let {
                    if (it < 0)
                        throw IllegalArgumentException("incorrect transition $it")
                    this["transition"] = JsonPrimitive(it)
                }
            }
            callService(
                    serviceName = "turn_off",
                    data = data
            )
        }


    }
}

/** Access the Light Domain */
typealias LightDomain = Light

val KHomeAssistantContext.Light: LightDomain
    get() = LightDomain.also { it.kHomeAssistant = kHomeAssistant }
