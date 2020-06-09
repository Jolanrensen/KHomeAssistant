package nl.jolanrensen.kHomeAssistant.domains.input

import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import kotlin.reflect.KProperty


/**
 * https://www.home-assistant.io/integrations/input_boolean/
 */
class InputBoolean(override var kHomeAssistant: () -> KHomeAssistant?) : Domain<InputBoolean.Entity> {
    override val domainName = "input_boolean"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputBoolean.' from a KHomeAssistantContext instead of using InputBoolean directly.""".trimMargin()
    }

    /** Making sure InputBoolean acts as a singleton. */
    override fun equals(other: Any?) = other is InputBoolean
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_boolean configuration */
    suspend fun reload() = callService("reload")

    // TODO check https://www.home-assistant.io/integrations/input_boolean/ you also need area ids maybe?

    override fun Entity(name: String) =
        Entity(
            kHomeAssistant = kHomeAssistant,
            name = name
        )

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputBoolean(kHomeAssistant)
    ) {
        /** Delegate so you can control an InputBoolean like a local variable
         * Simply type "var yourBoolean by InputBoolean.Entity("your_boolean")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = state == OnOff.ON
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            runBlocking { switchTo(value) }
        }

        init {
            attributes += ::editable
        }

        // Attributes
        // read only
        val editable: Boolean? by attrsDelegate

    }
}

/** Access the InputBoolean Domain */
val KHomeAssistantContext.InputBoolean: InputBoolean
    get() = InputBoolean(kHomeAssistant)