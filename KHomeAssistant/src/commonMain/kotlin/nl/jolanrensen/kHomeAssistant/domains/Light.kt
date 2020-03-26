package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.helper.HSColor
import nl.jolanrensen.kHomeAssistant.helper.RGBColor
import nl.jolanrensen.kHomeAssistant.helper.XYColor

/** Do not use directly! Always use Light. */
object Light : Domain<Light.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "light"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Light.' from a KHomeAssistantContext instead of using LightDomain directly.""".trimMargin()
    }

    /** Does the same as LightEntity() */
    override fun Entity(name: String): Entity = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
            override val kHomeAssistant: () -> KHomeAssistant?,
            override val name: String
    ) : BaseEntity<OnOff, Entity.Attributes>(
            kHomeAssistant = kHomeAssistant,
            name = name,
            domain = Light // TODO check?
    ), ToggleEntity {

        @Serializable
        data class Attributes(
                override val friendly_name: String,
                val min_mireds: Int? = null,
                val max_mireds: Int? = null,
                val effect_list: List<String>? = null,
                val brightness: Int? = null,
                val hs_color: HSColor? = null,
                val rgb_color: RGBColor? = null,
                val xy_color: XYColor? = null,
                val white_value: Int? = null,
                val supported_features: Int
        ) : BaseAttributes {
            override var fullJsonObject: JsonObject = JsonObject(mapOf())
        }

        override val attributesSerializer: KSerializer<Attributes> = Attributes.serializer()

        override fun getStateValue(state: OnOff): String = state.stateValue

        override fun parseStateValue(stateValue: String): OnOff? = try {
            OnOff.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        fun onTurnOn(callback: suspend Entity.() -> Unit) {
            registerStateListener({ it == OnOff.ON }, callback)
        }

        override suspend fun turnOn() {
            callService("turn_on")
        }

        suspend fun turnOn(brightness: Int /* TODO ETC */) {
            callService(
                    serviceName = "turn_on",
                    data = mapOf(
                            "brightness" to JsonPrimitive(brightness)
                    )
            )
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

/** Access the Light Domain */
typealias LightDomain = Light

val KHomeAssistantContext.Light: LightDomain
    get() = LightDomain.also { it.kHomeAssistant = kHomeAssistant }
