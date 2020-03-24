package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.SwitchEntity

/** Do not use directly! Always use Switch. */
object SwitchDomain : Domain<SwitchEntity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "switch"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Switch.' from a KHomeAssistantContext instead of using SwitchDomain directly.""".trimMargin()
    }

    /** Does the same as SwitchEntity() */
    override fun Entity(name: String): SwitchEntity {
//        checkContext()
        return SwitchEntity(name = name)
    }
}

/** Access the SwitchDomain */
val KHomeAssistantContext.Switch get() = SwitchDomain.also { it.kHomeAssistant = kHomeAssistant }