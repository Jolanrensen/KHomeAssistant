package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.helper.cast
import kotlin.reflect.KProperty

/**
 * https://www.home-assistant.io/integrations/input_number/
 */
class InputNumber(override var kHomeAssistant: () -> KHomeAssistant?) : Domain<InputNumber.Entity> {
    override val domainName = "input_number"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputNumber.' from a KHomeAssistantContext instead of using InputNumber directly.""".trimMargin()
    }

    /** Making sure InputNumber acts as a singleton. */
    override fun equals(other: Any?) = other is InputNumber
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_number configuration. */
    suspend fun reload() = callService("reload")


    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

    enum class InputNumberMode(val stateValue: String) {
        BOX("box"), SLIDER("slider")
    }

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<Float>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputNumber(kHomeAssistant)
    ) {
        /** Delegate so you can control an InputNumber like a local variable
         * Simply type "var yourFloat by InputNumber.Entity("your_float")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Float = state
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            state = value
        }

        init {
            attributes += arrayOf(
                ::min,
                ::max,
                ::initial,
                ::step,
                ::mode,
                ::editable
            )
        }

        override fun parseStateValue(stateValue: String) = stateValue.toFloatOrNull()

        override fun getStateValue(state: Float) = state.toString()

        /** [state] can also be writable. */
        override var state: Float
            get() = super.state
            set(value) {
                runBlocking { setValue(value) }
            }

        // Attributes
        // read only

        /** Minimum value. */
        val min: Float? by attrsDelegate

        /** Maximum value. */
        val max: Float? by attrsDelegate

        /** Initial value when Home Assistant starts. */ // TODO different than initial_state?
        val initial: Float? by attrsDelegate

        /** Step value. Smallest value 0.001. */
        val step: Float
            get() {
                val value: Float? = attrsDelegate.getValue(this, ::step)
                return value ?: 1f
            }

        /** Can specify box or slider. */
        val mode: InputNumberMode
            get() = rawAttributes[::mode.name]?.cast<String>()
                ?.let { value -> InputNumberMode.values().find { it.stateValue == value } }
                ?: InputNumberMode.SLIDER

        val editable: Boolean? by attrsDelegate


        /** Decrement the value by 'step'. */
        suspend fun decrement() = callService("decrement")

        /** Increment the value by 'step'. */
        suspend fun increment() = callService("increment")

        /** Set the state value. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun setValue(value: Float) =
            callService(
                serviceName = "set_value",
                data = buildMap<String, JsonElement> {
                    value.let {
                        if (it !in min!!..max!!)
                            throw IllegalArgumentException("incorrect value $it")
                        this["value"] = JsonPrimitive(it)
                    }
                }
            )
    }
}

/** Access the InputNumber Domain. */
val KHomeAssistantContext.InputNumber: InputNumber
    get() = InputNumber(kHomeAssistant)