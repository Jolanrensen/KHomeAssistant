package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.helper.*
import kotlin.reflect.KProperty

/** Do not use directly! Always use Light.
 *
 * https://www.home-assistant.io/integrations/light/
 * */
object Light : Domain<LightEntity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "light"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Light.' from a KHomeAssistantContext instead of using Light directly.""".trimMargin()
    }

    override fun Entity(name: String): LightEntity = LightEntity(kHomeAssistant = kHomeAssistant, name = name)


}

/** Access the Light Domain */
typealias LightDomain = Light

val KHomeAssistantContext.Light: LightDomain
    get() = LightDomain.also { it.kHomeAssistant = kHomeAssistant }


class LightEntity(
    override val kHomeAssistant: () -> KHomeAssistant?,
    override val name: String
) : ToggleEntity<LightEntity.Attributes>(
    kHomeAssistant = kHomeAssistant,
    name = name,
    domain = Light // TODO check?
) {

    /** Some attributes can also be set using the turn_on command. For those, we define a setter-companion to getValue */
    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified V : Any?> setValue(
        thisRef: BaseEntity<*, *>?,
        property: KProperty<*>,
        value: V
    ) {
        runBlocking {
            when (property.name) {
                ::brightness.name -> turnOnWithData(brightness = value as Int)
                ::hs_color.name -> turnOnWithData(hs_color = value as HSColor)
                ::rgb_color.name -> turnOnWithData(rgb_color = value as RGBColor)
                ::xy_color.name -> turnOnWithData(xy_color = value as XYColor)
                ::white_value.name -> turnOnWithData(white_value = value as Int)
            }
            Unit
        }
    }

    // Attributes
    val min_mireds: Int? by this
    val max_mireds: Int? by this
    val effect_list: List<String>? by this
    val supported_features: Int? by this

    var brightness: Int? by this
    var hs_color: HSColor? by this
    var rgb_color: RGBColor? by this
    var xy_color: XYColor? by this
    var white_value: Int? by this


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
        override var fullJsonObject = JsonObject(mapOf())
    }

    override val attributesSerializer: KSerializer<Attributes> = Attributes.serializer()

    enum class Flash(val value: String) {
        SHORT("short"), LONG("long")
    }


    @Serializable
    inner class turnOnWithData(
        val transition: Int? = null,
        val profile: String? = null,
        val hs_color: HSColor? = null,
        val xy_color: XYColor? = null,
        val rgb_color: RGBColor? = null,
        val white_value: Int? = null,
        val color_temp: Int? = null,
        val kelvin: Int? = null,
        val color_name: String? = null,
        val brightness: Int? = null,
        val brightness_pct: Float? = null,
        val brightness_step: Float? = null,
        val brightness_step_pct: Float? = null,
        val flash: Flash? = null,
        val effect: String? = null // tODO add result callback
    ) {

        init {
            runBlocking {
                // First check input
                val attributes = attributes
                transition?.let {
                    if (it < 0)
                        throw IllegalArgumentException("incorrect transition $it")
                }
                profile?.let {
                }
                hs_color?.let {
                    if (it.isEmpty() || it.size > 2 || it.h !in 0f..360f || it.s !in 0f..100f)
                        throw IllegalArgumentException("incorrect hs_color $it")
                }
                xy_color?.let {
                    if (it.isEmpty() || it.size > 2)
                        throw IllegalArgumentException("incorrect xy_color $it")
                }
                rgb_color?.let {
                    if (it.isEmpty() || it.size > 3 || it.any { it !in 0..255 })
                        throw IllegalArgumentException("incorrect rgb_color $it")
                }
                white_value?.let {
                    if (it !in 0..255)
                        throw IllegalArgumentException("incorrect white_value $it")
                }
                color_temp?.let {
                    if (attributes.min_mireds == null || attributes.max_mireds == null)
                        throw IllegalArgumentException("mireds not supported for this device")
                    if (it !in attributes.min_mireds..attributes.max_mireds)
                        throw IllegalArgumentException("incorrect color_temp $it")
                }
                kelvin?.let {
                    if (it < 0)
                        throw IllegalArgumentException("incorrect kelvin $it")
                }
                color_name?.let {
                    // TODO check color name https://www.w3.org/TR/css-color-3/#svg-color
                }
                brightness?.let {
                    if (it !in 0..255)
                        throw IllegalArgumentException("incorrect brightness $it")
                }
                brightness_pct?.let {
                    if (it !in 0f..100f)
                        throw IllegalArgumentException("incorrect brightness_pct $it")
                }
                brightness_step?.let {
                    if (it !in -255f..255f)
                        throw IllegalArgumentException("incorrect brightness_step $it")
                }
                brightness_step_pct?.let {
                    if (it !in -100f..100f)
                        throw IllegalArgumentException("incorrect brightness_step_pct $it")
                }
                flash?.let {
                }
                effect?.let {
                    if (attributes.effect_list == null || it !in attributes.effect_list)
                        throw IllegalArgumentException("incorrect effect $it")
                }

                // Then serialize the json and call the service with the data
                val data = Json(
                    JsonConfiguration.Stable.copy(encodeDefaults = false)
                ).toJson(serializer(), this@turnOnWithData)

                callService(
                    serviceName = "turn_on",
                    data = data.jsonObject
                )
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun turnOff(transition: Int) =
        callService(
            serviceName = "turn_off",
            data = buildMap<String, JsonElement> {
                transition.let {
                    if (it < 0)
                        throw IllegalArgumentException("incorrect transition $it")
                    this["transition"] = JsonPrimitive(it)
                }
            }
        )
}