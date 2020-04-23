package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Light.SupportedFeatures.*
import nl.jolanrensen.kHomeAssistant.entities.AttributesDelegate
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.helper.*
import kotlin.reflect.KProperty


/** Do not use directly! Always use Light.
 *
 * https://www.home-assistant.io/integrations/light/
 * */
object Light : Domain<Light.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "light"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Light.' from a KHomeAssistantContext instead of using Light directly.""".trimMargin()
    }


    enum class Flash(val value: String) {
        SHORT("short"), LONG("long")
    }

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_BRIGHTNESS(1),
        SUPPORT_COLOR_TEMP(2),
        SUPPORT_EFFECT(4),
        SUPPORT_FLASH(8),
        SUPPORT_COLOR(16),
        SUPPORT_TRANSITION(32),
        SUPPORT_WHITE_VALUE(128)
    }

    override fun Entity(name: String): Entity = Entity(kHomeAssistant = kHomeAssistant, name = name)

    @OptIn(ExperimentalStdlibApi::class)
    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = Light
    ) {

        init {
            attributes += arrayOf(
                ::min_mireds,
                ::max_mireds,
                ::effect_list,
                ::supported_features,
                ::brightness,
                ::hs_color,
                ::rgb_color,
                ::xy_color,
                ::white_value
            )
        }

        /** Some attributes can be set using the turn_on command. For those, we define a setter-companion to getValue. */
        @Suppress("UNCHECKED_CAST")
        operator fun <V : Any?> AttributesDelegate.setValue(
            thisRef: BaseEntity<*>?,
            property: KProperty<*>,
            value: V
        ) {
            runBlocking {
                when (property.name) {
                    ::brightness.name -> turnOn(brightness = value as Int)
                    ::hs_color.name -> turnOn(hs_color = value as HSColor)
                    ::rgb_color.name -> turnOn(rgb_color = value as RGBColor)
                    ::xy_color.name -> turnOn(xy_color = value as XYColor)
                    ::white_value.name -> turnOn(white_value = value as Int)
                }
                Unit
            }
        }

        // ----- Attributes -----
        // read only

        /** Minimum color temperature in mireds. */
        val min_mireds: Int? by attrsDelegate

        /** Maximum color temperature in mireds. */
        val max_mireds: Int? by attrsDelegate

        /** List of supported effects. */
        val effect_list: List<String>? by attrsDelegate

        /** Set of supported features. */
        val supported_features: Set<SupportedFeatures>
            get() = buildSet {
                val value: Int? = rawAttributes[::supported_features.name]?.cast()
                SupportedFeatures.values().forEach {
                    if (it.value and value!! == it.value)
                        add(it)
                }
            }

        // read / write

        /** RGBA Color representing the color of the light. The easiest way to control the light's color. The A component is ignored.
         * You can find a lot of colors in com.soywiz.korim.color.Colors
         * */
        var color: RGBA?
            get() = rgb_color?.run { RGBA(r, g, b) }
            set(value) {
                runBlocking { turnOn(color = value!!) }
            }

        /** Integer between 0 and 255 for how bright the light should be, where 0 means the light is off, 1 is the minimum brightness and 255 is the maximum brightness supported by the light. */
        var brightness: Int? by attrsDelegate

        /** An HSColor containing two floats representing the hue and saturation of the color you want the light to be. Hue is scaled 0-360, and saturation is scaled 0-100. */
        var hs_color: HSColor? by attrsDelegate

        /** An RGBColor containing three integers between 0 and 255 representing the RGB color you want the light to be. Note that the specified RGB value will not change the light brightness, only the color. */
        var rgb_color: RGBColor? by attrsDelegate

        /** An XYColor containing two floats representing the xy color you want the light to be. */
        var xy_color: XYColor? by attrsDelegate

        /** Integer between 0 and 255 for how bright a dedicated white LED should be. */
        var white_value: Int? by attrsDelegate

        // write only

        /** String with the name of one of the built-in profiles (relax, energize, concentrate, reading) or one of the custom profiles defined in light_profiles.csv in the current working directory. Light profiles define an xy color and a brightness. If a profile is given and a brightness then the profile brightness will be overwritten. */
        var profile: String
            @Deprecated("'profile' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'profile' is write only")
            set(value) {
                runBlocking { turnOn(profile = value) }
            }

        /** An integer in mireds representing the color temperature you want the light to be. */
        var color_temp: Int
            @Deprecated("'color_temp' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_temp' is write only")
            set(value) {
                runBlocking { turnOn(color_temp = value) }
            }

        /** Alternatively, you can specify the color temperature in Kelvin. */
        var kelvin: Int
            @Deprecated("'kelvin' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'kelvin' is write only")
            set(value) {
                runBlocking { turnOn(kelvin = value) }
            }

        /** A human-readable string of a color name, such as blue or goldenrod. All CSS3 color names are supported. */
        var color_name: String
            @Deprecated("'color_name' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_name' is write only")
            set(value) {
                runBlocking { turnOn(color_name = value) }
            }

        /** Alternatively, you can specify brightness in percent (a number between 0 and 100), where 0 means the light is off, 1 is the minimum brightness and 100 is the maximum brightness supported by the light. */
        var brightness_pct: Float
            @Deprecated("'brightness_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_pct' is write only")
            set(value) {
                runBlocking { turnOn(brightness_pct = value) }
            }

        /** Change brightness by an amount. Should be between -255..255. */
        var brightness_step: Float
            @Deprecated("'brightness_step' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step' is write only")
            set(value) {
                runBlocking { turnOn(brightness_step = value) }
            }

        /** Change brightness by a percentage. Should be between -100..100. */
        var brightness_step_pct: Float
            @Deprecated("'brightness_step_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step_pct' is write only")
            set(value) {
                runBlocking { turnOn(brightness_step_pct = value) }
            }

        /** Tell light to flash, can be either value Flash.SHORT or Flash.LONG. */
        var flash: Flash
            @Deprecated("'flash' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'flash' is write only")
            set(value) {
                runBlocking { turnOn(flash = value) }
            }

        /** Applies an effect such as colorloop or random. */
        var effect: String
            @Deprecated("'effect' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'effect' is write only")
            set(value) {
                runBlocking { turnOn(effect = value) }
            }

        private fun checkIfSupported(supportedFeature: SupportedFeatures) {
            if (supportedFeature !in supported_features)
                throw UnsupportedFeatureException("Unfortunately the light $name does not support ${supportedFeature.name}.")
        }


        suspend fun turnOn(
            transition: Int? = null,
            profile: String? = null,
            hs_color: HSColor? = null,
            xy_color: XYColor? = null,
            rgb_color: RGBColor? = null,
            color: RGBA? = null, // A is ignored
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
        ) = callService(
            serviceName = "turn_on",
            data = buildMap<String, JsonElement> {
                transition?.let {
                    checkIfSupported(SUPPORT_TRANSITION)
                    if (it < 0)
                        throw IllegalArgumentException("incorrect transition $it")
                    this["transition"] = JsonPrimitive(it)
                }
                profile?.let {
                    this["profile"] = JsonPrimitive(it)
                }
                hs_color?.let {
                    checkIfSupported(SUPPORT_COLOR)
                    if (it.isEmpty() || it.size > 2 || it.h !in 0f..360f || it.s !in 0f..100f)
                        throw IllegalArgumentException("incorrect hs_color $it")
                    this["hs_color"] = JsonArray(it.map { JsonPrimitive(it) })
                }
                xy_color?.let {
                    checkIfSupported(SUPPORT_COLOR)
                    if (it.isEmpty() || it.size > 2)
                        throw IllegalArgumentException("incorrect xy_color $it")
                    this["xy_color"] = JsonArray(it.map { JsonPrimitive(it) })
                }
                rgb_color?.let {
                    checkIfSupported(SUPPORT_COLOR)
                    if (it.isEmpty() || it.size > 3 || it.any { it !in 0..255 })
                        throw IllegalArgumentException("incorrect rgb_color $it")
                    this["rgb_color"] = JsonArray(it.map { JsonPrimitive(it) })
                }
                color?.let {
                    checkIfSupported(SUPPORT_COLOR)
                    this["rgb_color"] = JsonArray(
                        listOf(
                            JsonPrimitive(it.r),
                            JsonPrimitive(it.g),
                            JsonPrimitive(it.b)
                        )
                    )
                }
                white_value?.let {
                    checkIfSupported(SUPPORT_WHITE_VALUE)
                    if (it !in 0..255)
                        throw IllegalArgumentException("incorrect white_value $it")
                    this["white_value"] = JsonPrimitive(it)
                }
                color_temp?.let {
                    checkIfSupported(SUPPORT_COLOR_TEMP)
                    if (min_mireds == null || max_mireds == null)
                        throw IllegalArgumentException("mireds not supported for this device")
                    if (it !in min_mireds!!..max_mireds!!)
                        throw IllegalArgumentException("incorrect color_temp $it")
                    this["color_temp"] = JsonPrimitive(it)
                }
                kelvin?.let {
                    checkIfSupported(SUPPORT_COLOR_TEMP)
                    if (it < 0)
                        throw IllegalArgumentException("incorrect kelvin $it")
                    this["kelvin"] = JsonPrimitive(it)
                }
                color_name?.let {
                    checkIfSupported(SUPPORT_COLOR)
                    if (it !in Colors.colorsByName)
                        throw IllegalArgumentException("incorrect color_name $it")
                    this["color_name"] = JsonPrimitive(it)
                }
                brightness?.let {
                    checkIfSupported(SUPPORT_BRIGHTNESS)
                    if (it !in 0..255)
                        throw IllegalArgumentException("incorrect brightness $it")
                    this["brightness"] = JsonPrimitive(it)
                }
                brightness_pct?.let {
                    checkIfSupported(SUPPORT_BRIGHTNESS)
                    if (it !in 0f..100f)
                        throw IllegalArgumentException("incorrect brightness_pct $it")
                    this["brightness_pct"] = JsonPrimitive(it)
                }
                brightness_step?.let {
                    checkIfSupported(SUPPORT_BRIGHTNESS)
                    if (it !in -255f..255f)
                        throw IllegalArgumentException("incorrect brightness_step $it")
                    this["brightness_step"] = JsonPrimitive(it)
                }
                brightness_step_pct?.let {
                    checkIfSupported(SUPPORT_BRIGHTNESS)
                    if (it !in -100f..100f)
                        throw IllegalArgumentException("incorrect brightness_step_pct $it")
                    this["brightness_step_pct"] = JsonPrimitive(it)
                }
                flash?.let {
                    checkIfSupported(SUPPORT_FLASH)
                    this["flash"] = JsonPrimitive(it.value)
                }
                effect?.let {
                    checkIfSupported(SUPPORT_EFFECT)
                    if (effect_list == null || it !in effect_list!!)
                        throw IllegalArgumentException("incorrect effect $it")
                    this["effect"] = JsonPrimitive(it)
                }
            }
        )


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
}


typealias LightDomain = Light

/** Access the Light Domain */
val KHomeAssistantContext.Light: LightDomain
    get() = LightDomain.also { it.kHomeAssistant = kHomeAssistant }


