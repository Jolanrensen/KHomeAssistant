package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity

/**
 * https://www.home-assistant.io/integrations/input_number/
 */
object InputNumber : Domain<InputNumber.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "input_number"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputNumber.' from a KHomeAssistantContext instead of using InputNumber directly.""".trimMargin()
    }

    /** Reload input_number configuration */
    suspend fun reload() = callService("reload")


    override fun Entity(name: String) = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<Float>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputNumber
    ) {
        // initial: 30
        //editable: false
        //min: 0
        //max: 100
        //step: 0.5
        //mode: slider
        //unit_of_measurement: m
        //friendly_name: Input number test
        //icon: mdi:home

        // Attributes
        // read only

        val initial: Float? by attrsDelegate

        val editable: Boolean? by attrsDelegate

    }
}

/** Access the InputNumber Domain */
typealias InputNumberDomain = InputNumber

val KHomeAssistantContext.InputNumber: InputNumberDomain
    get() = InputNumberDomain.also { it.kHomeAssistant = kHomeAssistant }