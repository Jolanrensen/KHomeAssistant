package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Fan.SupportedFeatures.*
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.UnsupportedFeatureException
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

class Fan(override val kHassInstance: KHomeAssistant) : Domain<Fan.Entity> {
    override val domainName: String = "fan"

    /** Making sure Fan acts as a singleton. */
    override fun equals(other: Any?) = other is Fan
    override fun hashCode(): Int = domainName.hashCode()

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_DIRECTION(1),
        SUPPORT_SET_SPEED(2),
        SUPPORT_OSCILLATE(4)
    }

    enum class Speed(val value: String) {
        OFF("off"),
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high")
    }

    enum class Direction(val value: String) {
        FORWARD("forward"),
        REVERSE("reverse")
    }

    interface HassAttributes : BaseHassAttributes {

        // Read only

        /** Get the list of available speeds. @see [speedList]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("speedList"))
        val speed_list: List<String>

        /** Set of supported features. @see [supportedFeatures]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        val supported_features: Int

        // Read / Write

        /** The current direction of the fan. @see [currentDirection]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("currentDirection"))
        var current_direction: String

        /** True if the fan is on. @see [state] */
        var is_on: Boolean

        /** True if the fan is oscillating (rotating). */
        var oscillating: Boolean

        /** The current speed. One of the values in [speed_list]. @see [speed_]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("speed_"))
        var speed: String

        // Helper

        /** Get the list of available speeds. */
        val speedList: List<Speed>
            get() = speed_list.map { value -> Speed.values().find { it.value == value }!! }


        /** Set of supported features. */
        @OptIn(ExperimentalStdlibApi::class)
        val supportedFeatures: Set<SupportedFeatures>
            get() = buildSet {
                val value = supported_features
                SupportedFeatures.values().forEach {
                    if (it.value and value == it.value)
                        add(it)
                }
            }

        /** The current direction of the fan. */
        var currentDirection: Direction
            get() = Direction.values().find { it.value == current_direction }!!
            set(value) {
                current_direction = value.value
            }

        /** The current speed. */
        var speed_: Speed
            get() = Speed.values().find { it.value == speed }!!
            set(value) {
                speed = value.value
            }

    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity<HassAttributes>( // TODO check whether the state of a fan is actually on/off
        kHassInstance = kHassInstance,
        name = name,
        domain = Fan(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()
        override val additionalToStringAttributes: Array<Attribute<*>> = super.additionalToStringAttributes +
                getHassAttributesHelpers<HassAttributes>()

        /** Some attributes can be set using service calls. For those, we define a setter-companion to getValue. */
        @Suppress("UNCHECKED_CAST")
        override suspend fun <V : Any?> setValue(
            propertyName: String,
            value: V
        ) {
            when (propertyName) {
                ::current_direction.name -> setDirection(value as String)
                ::is_on.name -> switchTo(value as Boolean)
                ::oscillating.name -> oscillate(value as Boolean)
                ::speed.name -> setSpeed(value as String)
            }
        }

        // Attributes
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("speedList"))
        override val speed_list: List<String> by attrsDelegate(listOf())
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        override val supported_features: Int by attrsDelegate()

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("currentDirection"))
        override var current_direction: String by attrsDelegate()
        override var is_on: Boolean by attrsDelegate()
        override var oscillating: Boolean by attrsDelegate()
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("speed_"))
        override var speed: String by attrsDelegate()

        private fun checkIfSupported(vararg supportedFeatures: SupportedFeatures) {
            supportedFeatures.forEach {
                if (it !in supportedFeatures)
                    throw UnsupportedFeatureException("Unfortunately the fan $name does not support ${it.name}.")
            }
        }

        /** Sets the speed for fan device. */
        suspend fun setSpeed(speed: Speed, async: Boolean = false): ResultMessage = setSpeed(speed.value, async)

        /** Sets the speed for fan device. */
        suspend fun setSpeed(speedName: String, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SET_SPEED)
            if (speedName !in speed_list)
                throw IllegalArgumentException("speed $speedName is not supported by the fan $name.")
            val result = callService(
                serviceName = "set_speed",
                data = json { "speed" to speedName }
            )
            if (!async) suspendUntilAttributeChangedTo(::speed, speedName)
            return result
        }

        /** Sets the rotation for fan device. */
        suspend fun setDirection(direction: Direction, async: Boolean = false): ResultMessage =
            setDirection(direction.value, async)

        /** Sets the rotation for fan device. */
        suspend fun setDirection(directionName: String, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_DIRECTION)
            val result = callService(
                serviceName = "set_direction",
                data = json { "direction" to directionName }
            )
            if (!async) suspendUntilAttributeChangedTo(::current_direction, directionName)
            return result
        }

        /** Sets the oscillation for fan device */
        suspend fun oscillate(oscillating: Boolean, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_OSCILLATE)
            val result = callService(
                serviceName = "oscillating",
                data = json { "oscillating" to oscillating }
            )
            if (!async) suspendUntilAttributeChangedTo(::oscillating, oscillating)
            return result
        }

    }
}

/** Access the Fan domain. */
val KHomeAssistant.Fan: Fan
    get() = Fan(this)