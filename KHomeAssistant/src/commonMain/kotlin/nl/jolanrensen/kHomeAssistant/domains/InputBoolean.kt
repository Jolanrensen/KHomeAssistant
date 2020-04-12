package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.entities.getValue

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
    ) : ToggleEntity(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputBoolean
    ) {
        // Attributes
        // read only
        val editable: Boolean? by this

    }
}

/** Access the InputBoolean Domain */
typealias InputBooleanDomain = InputBoolean

val KHomeAssistantContext.InputBoolean: InputBooleanDomain
    get() = InputBooleanDomain.also { it.kHomeAssistant = kHomeAssistant }