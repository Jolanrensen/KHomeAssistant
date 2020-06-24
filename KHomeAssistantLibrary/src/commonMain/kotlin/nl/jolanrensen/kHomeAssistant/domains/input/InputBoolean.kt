package nl.jolanrensen.kHomeAssistant.domains.input

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import kotlin.reflect.KProperty


/**
 * https://www.home-assistant.io/integrations/input_boolean/
 */
class InputBoolean(kHassInstance: KHomeAssistant) : Domain<InputBoolean.Entity>, KHomeAssistant by kHassInstance {
    override val domainName = "input_boolean"

    /** Making sure InputBoolean acts as a singleton. */
    override fun equals(other: Any?) = other is InputBoolean
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_boolean configuration */
    suspend fun reload() = callService("reload")

    // TODO check https://www.home-assistant.io/integrations/input_boolean/ you also need area ids maybe?

    override fun Entity(name: String) =
        Entity(
            kHassInstance = this,
            name = name
        )

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputBoolean(kHassInstance)
    ) {
        /** Delegate so you can control an InputBoolean like a local variable
         * Simply type "var yourBoolean by InputBoolean.Entity("your_boolean")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = state == OnOff.ON
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            runBlocking { switchTo(value) }
        }

        init {
            this.hassAttributes += ::editable
        }

        // Attributes
        // read only
        val editable: Boolean by attrsDelegate()

    }
}

/** Access the InputBoolean Domain */
val KHomeAssistant.InputBoolean: InputBoolean
    get() = InputBoolean(this)