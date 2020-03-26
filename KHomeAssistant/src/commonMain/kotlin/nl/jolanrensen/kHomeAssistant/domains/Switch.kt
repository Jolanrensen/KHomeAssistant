package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity

/** Do not use directly! Always use Switch. */
object Switch : Domain<Switch.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "switch"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Switch.' from a KHomeAssistantContext instead of using SwitchDomain directly.""".trimMargin()
    }

    /** Constructor of Switch.Entity with right context */
    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
            override val kHomeAssistant: () -> KHomeAssistant?,
            override val name: String
    ) : BaseEntity<OnOff, Entity.Attributes>(
            kHomeAssistant = kHomeAssistant,
            domain = Switch,
            name = name
    ), ToggleEntity, KHomeAssistantContext {

        @Serializable
        open class Attributes(
                override val friendly_name: String
        ) : BaseAttributes {
            override var fullJsonObject: JsonObject = JsonObject(mapOf())
        }

        override val attributesSerializer = Attributes.serializer()

        override fun getStateValue(state: OnOff) = state.stateValue

        override fun parseStateValue(stateValue: String) = try {
            OnOff.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        fun onTurnOn(callback: suspend ToggleEntity.() -> Unit) {
//        TODO("Not yet implemented")
        }


        override suspend fun turnOn() {
            callService("turn_on")
        }

        override suspend fun turnOff() {
            callService("turn_off")
        }

        override suspend fun toggle() {
            callService("toggle")
        }

        override suspend fun isOn() = getState() == OnOff.ON

        override suspend fun isOff() = getState() == OnOff.OFF

        override suspend fun isUnavailable() = getState() == OnOff.UNAVAILABLE

    }
}

/** Access the SwitchDomain */
typealias SwitchDomain = Switch

val KHomeAssistantContext.Switch: SwitchDomain
    get() = SwitchDomain.also { it.kHomeAssistant = kHomeAssistant }