package nl.jolanrensen.kHomeAssistant.domains.input

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import kotlin.reflect.KProperty


/**
 * https://www.home-assistant.io/integrations/input_boolean/
 */
class InputBoolean(override val kHassInstance: KHomeAssistant) : Domain<InputBoolean.Entity> {
    override val domainName = "input_boolean"

    /** Making sure InputBoolean acts as a singleton. */
    override fun equals(other: Any?) = other is InputBoolean
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_boolean configuration */
    suspend fun reload() = callService("reload")

    // TODO check https://www.home-assistant.io/integrations/input_boolean/ you also need area ids maybe?

    override fun Entity(name: String) = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BaseHassAttributes {
        // read only
        /** Is true when the input boolean is editable. */
        val editable: Boolean
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity<HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputBoolean(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        /** Delegate so you can control an InputBoolean like a local variable
         * Simply type "var yourBoolean by InputBoolean.Entity("your_boolean")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = state == OnOff.ON
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            runBlocking { switchTo(value) }
        }

        override val editable: Boolean by attrsDelegate()


        // Attributes


    }
}

/** Access the InputBoolean Domain */
val KHomeAssistant.InputBoolean: InputBoolean
    get() = InputBoolean(this)