package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity

/**
 * https://www.home-assistant.io/integrations/input_boolean/
 */
object InputBoolean : Domain<InputBoolean.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "input_boolean"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputBoolean.' from a KHomeAssistantContext instead of using InputBoolean directly.""".trimMargin()
    }

    /** Reload input_boolean configuration */
    suspend fun reload() = callService("reload")

    // TODO check https://www.home-assistant.io/integrations/input_boolean/ you also need area ids maybe?

    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity<Entity.Attributes>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputBoolean
    ) {
        @Serializable
        data class Attributes(
            override val friendly_name: String,
            val editable: Boolean
        ) : BaseAttributes {
            override var fullJsonObject = JsonObject(mapOf())
        }

        override val attributesSerializer: KSerializer<Attributes> = Attributes.serializer()
    }
}

/** Access the InputBoolean Domain */
typealias InputBooleanDomain = InputBoolean
val KHomeAssistantContext.InputBoolean: InputBooleanDomain
    get() = InputBooleanDomain.also { it.kHomeAssistant = kHomeAssistant }