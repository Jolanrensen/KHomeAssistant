package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.helper.plus
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

/**
 * https://www.home-assistant.io/integrations/notify/
 */
class Notify(override var getKHomeAssistant: () -> KHomeAssistant?) : Domain<Nothing> {
    override val domainName = "notify"

    override fun checkContext() = require(getKHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Notify.' from a KHomeAssistantContext instead of using Notify directly.""".trimMargin()
    }

    /** Making sure Notify acts as a singleton. */
    override fun equals(other: Any?) = other is Notify
    override fun hashCode(): Int = domainName.hashCode()

    /** Send a notification.
     * @TODO */
    suspend fun notify(
        serviceName: String,
        message: String,
        title: String? = null,
        target: Array<String>? = null,
        data: JsonObject = json { }
    ): ResultMessage = callService(
        serviceName = serviceName,
        data = data + json {
            message.let { "message" to it }
            title?.let { "title" to it }
            target?.let { "target" to JsonArray(target.map(::JsonPrimitive)) }
        }
    )

    override fun Entity(name: String): Nothing = throw DomainHasNoEntityException()

}

/** Access the Notify Domain */
val HasContext.Notify: Notify
    get() = Notify(getKHomeAssistant)