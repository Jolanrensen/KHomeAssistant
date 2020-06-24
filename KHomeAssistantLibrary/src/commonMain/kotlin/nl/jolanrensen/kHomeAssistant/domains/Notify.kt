package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.plus
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

/**
 * https://www.home-assistant.io/integrations/notify/
 */
class Notify(kHassInstance: KHomeAssistant) : Domain<Nothing>, KHomeAssistant by kHassInstance {
    override val domainName = "notify"

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
val KHomeAssistant.Notify: Notify
    get() = Notify(this)