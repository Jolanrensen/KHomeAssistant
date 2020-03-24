package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.LightEntity

/** Do not use directly! Always use Light. */
object LightDomain : Domain<LightEntity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "light"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Light.' from a KHomeAssistantContext instead of using LightDomain directly.""".trimMargin()
    }

    /** Does the same as LightEntity() */
    override fun Entity(name: String) = LightEntity(kHomeAssistant = kHomeAssistant, name = name)
}

/** Access the Light Domain */
val KHomeAssistantContext.Light get() = LightDomain.also { it.kHomeAssistant = kHomeAssistant }

// TODO do I want this?

//val WithKHomeAssistant.Light: Domain
//    get() {
//        return object : Domain {
//            override val kHomeAssistant = this@Light.kHomeAssistant
//            override val domainName = "light"
//
//            fun test() {
//
//            }
//
//            override fun createEntity(name: String): LightEntity = kHomeAssistant.LightEntity(name = name)
//        }
//    }
