package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.HasKHassContext
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

    /** Send a notification. This is dependent on which service you use.
     *
     * @param serviceName the serviceName you use to notify, for instance "mobile_app_your_phone", by default use "notify".
     * @param message Body of the notification. Not optional; needs to be specified either here or in data.
     * @param title Title of the notification. Optional.
     * @param target Some platforms allow specifying a recipient that will receive the notification. See your platform page if it is supported. Optional.
     * @param data On platforms that have extended functionality. See your platform page if it is supported. Optional.
     * @return result
     * @throws IllegalArgumentException if [message] is not present as argument or in [data].
     * */
    suspend fun notify(
        message: String? = null,
        serviceName: String = "notify",
        title: String? = null,
        target: Array<String>? = null,
        data: JsonObject = json { }
    ): ResultMessage = callService(
        serviceName = serviceName,
        data = data + json {
            message?.let { "message" to it }
            title?.let { "title" to it }
            target?.let { "target" to JsonArray(target.map(::JsonPrimitive)) }
        }.also { if ("message" !in it) throw IllegalArgumentException("'message' is a required attribute") }
    )

    override fun Entity(name: String): Nothing = throw DomainHasNoEntityException()

}

/** Access the Notify Domain */
val HasKHassContext.Notify: Notify
    get() = Notify(getKHomeAssistant)