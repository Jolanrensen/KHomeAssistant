package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.entities.LightEntity

object LightDomain : Domain {
    override lateinit var kHomeAssistant: KHomeAssistant
    override val domainName = "light"

    fun test() {}

    override fun Entity(name: String): LightEntity {
        require(::kHomeAssistant.isInitialized) { "Please initialize kHomeAssistant before calling this. This can be easily done by using 'Light.' from a KHomeAssistantContext instead of using 'LightDomain.'." }
        return kHomeAssistant.LightEntity(name = name)
    }
}

val KHomeAssistantContext.Light: LightDomain
    get() = LightDomain.also {
        it.kHomeAssistant = kHomeAssistant
    }

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
