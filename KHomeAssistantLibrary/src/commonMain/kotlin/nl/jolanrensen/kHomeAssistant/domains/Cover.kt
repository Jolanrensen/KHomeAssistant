package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Cover.DeviceClass.GENERIC
import nl.jolanrensen.kHomeAssistant.domains.Cover.State.*
import nl.jolanrensen.kHomeAssistant.domains.Cover.SupportedFeatures.*
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.UnsupportedFeatureException
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

/**
 * https://www.home-assistant.io/integrations/cover/
 * TODO needs testing
 * */
class Cover(override val kHassInstance: KHomeAssistant) : Domain<Cover.Entity> {
    override val domainName = "cover"

    /** Making sure Cover acts as a singleton. */
    override fun equals(other: Any?) = other is Cover
    override fun hashCode(): Int = domainName.hashCode()

    override fun Entity(name: String) = Entity(kHassInstance, name)

    enum class DeviceClass(val value: String?) {
        GENERIC(null),
        AWNING("awning"),
        BLIND("blind"),
        CURTAIN("curtain"),
        DAMPER("damper"),
        DOOR("door"),
        GARAGE("garage"),
        GATE("gate"),
        SHADE("shade"),
        SHUTTER("shutter"),
        WINDOW("window")
    }

    enum class State(val value: String?) {
        OPENING("opening"),
        OPEN("open"),
        CLOSING("closing"),
        CLOSED("closed"),
        UNKNOWN(null)
    }

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_OPEN(1),
        SUPPORT_CLOSE(2),
        SUPPORT_SET_POSITION(4),
        SUPPORT_STOP(8),
        SUPPORT_OPEN_TILT(16),
        SUPPORT_CLOSE_TILT(32),
        SUPPORT_SET_TILT_POSITION(64),
        SUPPORT_STOP_TILT(128)
    }


    interface HassAttributes : BaseHassAttributes {
        // Read only

        /** Set of supported features. @see [supportedFeatures]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        val supported_features: Int

        /** Describes the type/class of the cover. @see [deviceClass]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("deviceClass"))
        val device_class: String


        // Read write

        /** The current position of cover where 0 means closed and 100 is fully open. */
        var current_cover_position: Int

        /** The current tilt position of the cover where 0 means closed/no tilt and 100 means open/maximum tilt. */
        var current_cover_tilt_position: Int

        // Helper

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

        /** Describes the type/class of the cover. */
        val deviceClass: DeviceClass
            get() = DeviceClass.values()
                .find { it.value == device_class }
                ?: GENERIC


    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<State, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Cover(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()
        override val additionalToStringAttributes: Array<Attribute<*>> =
            super.additionalToStringAttributes + getHassAttributesHelpers<HassAttributes>()

        override fun stateToString(state: State) = state.value
        override fun stringToState(stateValue: String) = State.values().find { it.value == stateValue }

        override var state: State
            get() = super.state
            set(value) {
                runBlocking {
                    when (value) {
                        OPEN, OPENING -> openCover()
                        CLOSED, CLOSING -> closeCover()
                        UNKNOWN -> throw IllegalArgumentException("Can't set state to UNKNOWN")
                    }
                }
            }

        // Attributes

        override suspend fun <V> setValue(propertyName: String, value: V) {
            when (propertyName) {
                ::current_cover_position.name -> setCoverPosition(value as Int)
                ::current_cover_tilt_position.name -> setCoverTiltPosition(value as Int)
            }
        }

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        override val supported_features: Int by attrsDelegate()

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("deviceClass"))
        override val device_class: String by attrsDelegate()
        override var current_cover_position: Int by attrsDelegate()
        override var current_cover_tilt_position: Int by attrsDelegate()


        // Helper

        var isOpen: Boolean
            get() = state == OPEN
            set(value) {
                runBlocking {
                    if (value) openCover()
                    else closeCover()
                }
            }

        var isClosed: Boolean
            get() = state == CLOSED
            set(value) {
                runBlocking {
                    if (value) closeCover()
                    else openCover()
                }
            }

        var isOpening: Boolean
            get() = state == OPENING
            set(value) {
                runBlocking {
                    if (value) openCover()
                    else stopCover()
                }
            }

        var isClosing: Boolean
            get() = state == CLOSING
            set(value) {
                runBlocking {
                    if (value) closeCover()
                    else stopCover()
                }
            }

        private fun checkIfSupported(vararg supportedFeatures: SupportedFeatures) {
            supportedFeatures.forEach {
                if (it !in supportedFeatures)
                    throw UnsupportedFeatureException("Unfortunately the cover $name does not support ${it.name}.")
            }
        }

        /** Open the cover. */
        suspend fun openCover(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_OPEN)
            val result = callService("open_cover")
            if (!async) suspendUntilStateChanged({ it == OPEN || it == OPENING })
            return result
        }

        /** Close the cover. */
        suspend fun closeCover(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_CLOSE)
            val result = callService("close_cover")
            if (!async) suspendUntilStateChanged({ it == CLOSED || it == CLOSING })
            return result
        }

        /** Stop the current action (open, close, set position). */
        suspend fun stopCover(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_STOP)
            val result = callService("stop_cover")
            if (!async) suspendUntilStateChanged({ it != CLOSING && it != OPENING })
            return result
        }

        /** Close or open the cover depending on the current state. */
        suspend fun toggleCover(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_OPEN, SUPPORT_CLOSE)
            val oldState = state
            val result = callService("toggle")
            if (!async) suspendUntilStateChanged({
                when (oldState) {
                    OPEN, OPENING -> it == CLOSING || it == CLOSED
                    CLOSED, CLOSING -> it == OPEN || it == OPENING
                    UNKNOWN -> true
                }
            })
            return result
        }

        /** Open the cover tilt. */
        suspend fun openCoverTilt(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_OPEN_TILT)
            val oldPosition = current_cover_tilt_position
            val result = callService("open_cover_tilt")
            if (!async) suspendUntilAttributeChanged(::current_cover_tilt_position, {
                if (oldPosition == 100) true
                else it > oldPosition
            })
            return result
        }

        /** Close the cover tilt. */
        suspend fun closeCoverTilt(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_CLOSE_TILT)
            val oldPosition = current_cover_tilt_position
            val result = callService("close_cover_tilt")
            if (!async) suspendUntilAttributeChanged(::current_cover_tilt_position, {
                if (oldPosition == 0) true
                else it < oldPosition
            })
            return result
        }

        /** Stop the current tilt action (open, close, set position). */
        suspend fun stopCoverTilt(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_STOP_TILT)
            val result = callService("stop_cover_tilt")
            // TODO not sure how to check
            return result
        }

        /** Close or open the cover tilt depending on the current state. */
        suspend fun toggleCoverTilt(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_OPEN_TILT, SUPPORT_CLOSE_TILT)
            val oldPosition = current_cover_tilt_position
            val result = callService("toggle_tilt")
            // TODO not sure how to check
            return result
        }

        /** Set cover position. */
        suspend fun setCoverPosition(position: Int, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SET_POSITION)
            if (position !in 0..100)
                throw IllegalArgumentException("position $position must be in 0..100")
            val result = callService(
                serviceName = "set_cover_position",
                data = buildJsonObject { put("position", position) }
            )
            if (!async) suspendUntilAttributeChangedTo(::current_cover_position, position)
            return result
        }

        /** Set cover tilt position. */
        suspend fun setCoverTiltPosition(tiltPosition: Int, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SET_TILT_POSITION)
            if (tiltPosition !in 0..100)
                throw IllegalArgumentException("position $tiltPosition must be in 0..100")
            val result = callService(
                serviceName = "set_cover_tilt_position",
                data = buildJsonObject { put("tilt_position", tiltPosition) }
            )
            if (!async) suspendUntilAttributeChangedTo(::current_cover_tilt_position, tiltPosition)
            return result
        }
    }
}