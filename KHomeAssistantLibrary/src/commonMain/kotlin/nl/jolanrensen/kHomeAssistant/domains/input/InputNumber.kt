package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.cast
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

/**
 * https://www.home-assistant.io/integrations/input_number/
 */
class InputNumber(override val kHassInstance: KHomeAssistant) : Domain<InputNumber.Entity> {
    override val domainName = "input_number"

    /** Making sure InputNumber acts as a singleton. */
    override fun equals(other: Any?) = other is InputNumber
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_number configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String) = Entity(kHassInstance = kHassInstance, name = name)

    enum class InputNumberMode(val stateValue: String) {
        BOX("box"), SLIDER("slider")
    }

    interface HassAttributes : BaseHassAttributes {
        // read only

        /** Minimum value. */
        val min: Float

        /** Maximum value. */
        val max: Float

        /** Initial value when Home Assistant starts. */ // TODO different than initial_state?
        val initial: Float

        /** Step value. Smallest value 0.001. */
        val step: Float

        /** Can specify box or slider. @see [mode_]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("mode_"))
        val mode: String

        /** Is true if this input number is editable. */
        val editable: Boolean

        // Helpers

        /** Can specify box or slider. */
        val mode_: InputNumberMode
            get() = InputNumberMode.values().find { it.stateValue == mode } ?: InputNumberMode.SLIDER
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<Float, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputNumber(kHassInstance)
    ), HassAttributes {
        /** Delegate so you can control an InputNumber like a local variable
         * Simply type "var yourFloat by InputNumber.Entity("your_float")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = state
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            state = value
        }

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        override fun stringToState(stateValue: String) = stateValue.toFloatOrNull()

        override fun stateToString(state: Float) = state.toString()

        /** [state] can also be writable. */
        override var state: Float
            get() = super.state
            set(value) {
                runBlocking { setValue(value) }
            }

        // Attributes
        override val min: Float by attrsDelegate(Float.MIN_VALUE)
        override val max: Float by attrsDelegate(Float.MIN_VALUE)
        override val initial: Float by attrsDelegate()
        override val step: Float by attrsDelegate(1f)
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("mode_"))
        override val mode: String by attrsDelegate()
        override val editable: Boolean by attrsDelegate()

        /** Decrement the value by 'step'. */
        suspend fun decrement() = callService("decrement")

        /** Increment the value by 'step'. */
        suspend fun increment() = callService("increment")

        /** Set the state value. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun setValue(value: Float, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_value",
                data = buildJsonObject {
                    value.let {
                        if (it !in min..max)
                            throw IllegalArgumentException("incorrect value $it")
                        put("value", it)
                    }
                }
            )
            if (!async) suspendUntilStateChangedTo(value)
            return result
        }
    }
}

/** Access the InputNumber Domain. */
val KHomeAssistant.InputNumber: InputNumber
    get() = InputNumber(this)