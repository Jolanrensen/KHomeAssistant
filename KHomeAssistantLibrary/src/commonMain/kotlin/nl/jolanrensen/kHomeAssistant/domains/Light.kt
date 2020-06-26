package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Light.SupportedFeatures.*
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty


/**
 *
 * https://www.home-assistant.io/integrations/light/
 * */
class Light(override val kHassInstance: KHomeAssistant) : Domain<Light.Entity> {
    override val domainName = "light"

    /** Making sure Light acts as a singleton. */
    override fun equals(other: Any?) = other is Light
    override fun hashCode(): Int = domainName.hashCode()

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

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BaseHassAttributes {
        // read only

        /** Minimum color temperature in mireds. */
        val min_mireds: Int

        /** Maximum color temperature in mireds. */
        val max_mireds: Int

        /** List of supported effects. */
        val effect_list: List<String>

        /** Set of supported features. @see [supportedFeatures] */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        val supported_features: Int

        // read / write

        /** Integer between 0 and 255 for how bright the light should be, where 0 means the light is off, 1 is the minimum brightness and 255 is the maximum brightness supported by the light. */
        var brightness: Int

        /** An HSColor containing two floats representing the hue and saturation of the color you want the light to be. Hue is scaled 0-360, and saturation is scaled 0-100. @see [color]. */
        var hs_color: HSColor

        /** An RGBColor containing three integers between 0 and 255 representing the RGB color you want the light to be. Note that the specified RGB value will not change the light brightness, only the color. @see [color]. */
        var rgb_color: RGBColor

        /** An XYColor containing two floats representing the xy color you want the light to be. @see [color].  */
        var xy_color: XYColor

        /** Integer between 0 and 255 for how bright a dedicated white LED should be. */
        var white_value: Int

        // write only
        /** String with the name of one of the built-in profiles (relax, energize, concentrate, reading) or one of the custom profiles defined in light_profiles.csv in the current working directory. Light profiles define an xy color and a brightness. If a profile is given and a brightness then the profile brightness will be overwritten. */
        var profile: String
            @Deprecated("'profile' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'profile' is write only")
            set(_) = error("must be overridden")

        /** An integer in mireds representing the color temperature you want the light to be. */
        var color_temp: Int
            @Deprecated("'color_temp' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_temp' is write only")
            set(_) = error("must be overridden")

        /** Alternatively, you can specify the color temperature in Kelvin. */
        var kelvin: Int
            @Deprecated("'kelvin' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'kelvin' is write only")
            set(_) = error("must be overridden")

        /** A human-readable string of a color name, such as blue or goldenrod. All CSS3 color names are supported. */
        var color_name: String
            @Deprecated("'color_name' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_name' is write only")
            set(_) = error("must be overridden")

        /** Alternatively, you can specify brightness in percent (a number between 0 and 100), where 0 means the light is off, 1 is the minimum brightness and 100 is the maximum brightness supported by the light. */
        var brightness_pct: Float
            @Deprecated("'brightness_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_pct' is write only")
            set(_) = error("must be overridden")

        /** Change brightness by an amount. Should be between -255..255. */
        var brightness_step: Float
            @Deprecated("'brightness_step' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step' is write only")
            set(_) = error("must be overridden")

        /** Change brightness by a percentage. Should be between -100..100. */
        var brightness_step_pct: Float
            @Deprecated("'brightness_step_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step_pct' is write only")
            set(_) = error("must be overridden")

        /** Tell light to flash, can be either value "short" or "long". @see [flash_] */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("flash_"))
        var flash: String
            @Deprecated("'flash' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'flash' is write only")
            set(_) = error("must be overridden")

        /** Applies an effect such as colorloop or random. */
        var effect: String
            @Deprecated("'effect' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'effect' is write only")
            set(_) = error("must be overridden")


        // Helper getter/setters
        /** RGBA Color representing the color of the light. The easiest way to control the light's color. The A component is ignored.
         * You can find a lot of colors in [com.soywiz.korim.color.Colors]
         * */
        var color: RGBA
            get() = rgb_color.run { RGBA(r, g, b) }
            set(value) {
                rgb_color = value.run { RGBColor(r, g, b) }
            }

        /** Set of supported features. */
        @OptIn(ExperimentalStdlibApi::class)
        val supportedFeatures: Set<SupportedFeatures>
            get() = buildSet {
                val value = supported_features
                values().forEach {
                    if (it.value and value == it.value)
                        add(it)
                }
            }

        /** Tell light to flash, can be either value Flash.SHORT or Flash.LONG. */
        var flash_: Flash
            @Deprecated("'flash' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'flash' is write only")
            set(value) {
                flash = value.value
            }
    }

    @OptIn(ExperimentalStdlibApi::class)
    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity<HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Light(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        /** Some attributes can be set using the turn_on command. For those, we define a setter-companion to getValue. */
        @Suppress("UNCHECKED_CAST")
        override fun <V : Any?> setValue(
            property: KProperty<*>,
            value: V
        ) {
            runBlocking {
                when (property.name) {
                    ::brightness.name -> {
                        turnOn(brightness = value as Int)
                        suspendUntilAttributeChangedTo(::brightness, value)
                    }
                    ::hs_color.name -> {
                        turnOn(hs_color = value as HSColor)
                        suspendUntilAttributeChangedTo(::hs_color, value)
                    }
                    ::rgb_color.name -> {
                        turnOn(rgb_color = value as RGBColor)
                        suspendUntilAttributeChangedTo(::rgb_color, value)
                    }
                    ::xy_color.name -> {
                        turnOn(xy_color = value as XYColor)
                        suspendUntilAttributeChangedTo(::xy_color, value)
                    }
                    ::white_value.name -> {
                        turnOn(white_value = value as Int)
                        suspendUntilAttributeChangedTo(::white_value, value)
                    }
                }
                Unit
            }
        }

        // ----- Attributes -----
        override val min_mireds: Int by attrsDelegate()
        override val max_mireds: Int by attrsDelegate()
        override val effect_list: List<String> by attrsDelegate(listOf())
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        override val supported_features: Int by attrsDelegate(0)
        override var brightness: Int by attrsDelegate()
        override var hs_color: HSColor by attrsDelegate()
        override var rgb_color: RGBColor by attrsDelegate()
        override var white_value: Int by attrsDelegate()
        override var xy_color: XYColor by attrsDelegate()
        override var profile: String
            @Deprecated("'profile' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'profile' is write only")
            set(value) {
                runBlocking {
                    turnOn(profile = value)
                }
            }
        override var color_temp: Int
            @Deprecated("'color_temp' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_temp' is write only")
            set(value) {
                runBlocking {
                    turnOn(color_temp = value)
                }
            }
        override var kelvin: Int
            @Deprecated("'kelvin' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'kelvin' is write only")
            set(value) {
                runBlocking {
                    turnOn(kelvin = value)
                }
            }
        override var color_name: String
            @Deprecated("'color_name' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'color_name' is write only")
            set(value) {
                runBlocking {
                    turnOn(color_name = value)
                }
            }
        override var brightness_pct: Float
            @Deprecated("'brightness_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_pct' is write only")
            set(value) {
                runBlocking {
                    turnOn(brightness_pct = value)
                }
            }
        override var brightness_step: Float
            @Deprecated("'brightness_step' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step' is write only")
            set(value) {
                runBlocking {
                    turnOn(brightness_step = value)
                }
            }
        override var brightness_step_pct: Float
            @Deprecated("'brightness_step_pct' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'brightness_step_pct' is write only")
            set(value) {
                runBlocking {
                    turnOn(brightness_step_pct = value)
                }
            }
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("flash_"))
        override var flash: String
            @Deprecated("'flash' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'flash' is write only")
            set(value) {
                flash_ = Flash.values().find { it.value == value }!!
            }
        override var effect: String
            @Deprecated("'effect' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'effect' is write only")
            set(value) {
                runBlocking {
                    turnOn(effect = value)
                }
            }




        private fun checkIfSupported(supportedFeature: SupportedFeatures) {
            if (supportedFeature !in supportedFeatures)
                throw UnsupportedFeatureException("Unfortunately the light $name does not support ${supportedFeature.name}.")
        }


        suspend fun turnOn(
            transition: TimeSpan? = null,
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
            effect: String? = null,
            async: Boolean = false
        ): ResultMessage {
            val result = callService(
                serviceName = "turn_on",
                data = json {
                    transition?.let {
                        checkIfSupported(SUPPORT_TRANSITION)
                        if (it < TimeSpan.ZERO)
                            throw IllegalArgumentException("incorrect transition $it")
                        "transition" to it.seconds
                    }
                    profile?.let {
                        "profile" to it
                    }
                    hs_color?.let {
                        checkIfSupported(SUPPORT_COLOR)
                        if (it.isEmpty() || it.size > 2 || it.h !in 0f..360f || it.s !in 0f..100f)
                            throw IllegalArgumentException("incorrect hs_color $it")
                        "hs_color" to JsonArray(it.map(::JsonPrimitive))
                    }
                    xy_color?.let {
                        checkIfSupported(SUPPORT_COLOR)
                        if (it.isEmpty() || it.size > 2)
                            throw IllegalArgumentException("incorrect xy_color $it")
                        "xy_color" to JsonArray(it.map(::JsonPrimitive))
                    }
                    rgb_color?.let {
                        checkIfSupported(SUPPORT_COLOR)
                        if (it.isEmpty() || it.size > 3 || it.any { it !in 0..255 })
                            throw IllegalArgumentException("incorrect rgb_color $it")
                        "rgb_color" to JsonArray(it.map(::JsonPrimitive))
                    }
                    color?.let {
                        checkIfSupported(SUPPORT_COLOR)
                        "rgb_color" to JsonArray(
                            listOf(it.r, it.g, it.b).map(::JsonPrimitive)
                        )
                    }
                    white_value?.let {
                        checkIfSupported(SUPPORT_WHITE_VALUE)
                        if (it !in 0..255)
                            throw IllegalArgumentException("incorrect white_value $it")
                        "white_value" to it
                    }
                    color_temp?.let {
                        checkIfSupported(SUPPORT_COLOR_TEMP)
                        try {
                            min_mireds
                            max_mireds
                        } catch (e: Exception) {
                            throw IllegalArgumentException("mireds not supported for this device", e)
                        }
                        if (it !in min_mireds..max_mireds)
                            throw IllegalArgumentException("incorrect color_temp $it")
                        "color_temp" to it
                    }
                    kelvin?.let {
                        checkIfSupported(SUPPORT_COLOR_TEMP)
                        if (it < 0)
                            throw IllegalArgumentException("incorrect kelvin $it")
                        "kelvin" to it
                    }
                    color_name?.let {
                        checkIfSupported(SUPPORT_COLOR)
                        if (it !in Colors.colorsByName)
                            throw IllegalArgumentException("incorrect color_name $it")
                        "color_name" to it
                    }
                    brightness?.let {
                        checkIfSupported(SUPPORT_BRIGHTNESS)
                        if (it !in 0..255)
                            throw IllegalArgumentException("incorrect brightness $it")
                        "brightness" to it
                    }
                    brightness_pct?.let {
                        checkIfSupported(SUPPORT_BRIGHTNESS)
                        if (it !in 0f..100f)
                            throw IllegalArgumentException("incorrect brightness_pct $it")
                        "brightness_pct" to it
                    }
                    brightness_step?.let {
                        checkIfSupported(SUPPORT_BRIGHTNESS)
                        if (it !in -255f..255f)
                            throw IllegalArgumentException("incorrect brightness_step $it")
                        "brightness_step" to it
                    }
                    brightness_step_pct?.let {
                        checkIfSupported(SUPPORT_BRIGHTNESS)
                        if (it !in -100f..100f)
                            throw IllegalArgumentException("incorrect brightness_step_pct $it")
                        "brightness_step_pct" to it
                    }
                    flash?.let {
                        checkIfSupported(SUPPORT_FLASH)
                        "flash" to it.value
                    }
                    effect?.let {
                        checkIfSupported(SUPPORT_EFFECT)
                        if (it !in effect_list)
                            throw IllegalArgumentException("incorrect effect $it")
                        "effect" to it
                    }
                }
            )

            if (!async) suspendUntilStateChangedTo(OnOff.ON, transition ?: 1.seconds)

            return result
        }


        /**
         * @param transition time in seconds in which to turn off
         */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun turnOff(transition: TimeSpan, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "turn_off",
                data = json {
                    transition.let {
                        if (it < TimeSpan.ZERO)
                            throw IllegalArgumentException("incorrect transition $it")
                        "transition" to it
                    }
                }
            )
            if (!async) suspendUntilStateChangedTo(OnOff.OFF, transition + 1.seconds)
            return result
        }
    }

}

/** Access the Light Domain */
val KHomeAssistant.Light: Light
    get() = Light(this)


