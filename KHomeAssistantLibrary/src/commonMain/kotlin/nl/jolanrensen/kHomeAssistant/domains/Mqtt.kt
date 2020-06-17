package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage

/**
 * https://www.home-assistant.io/docs/mqtt/service/
 */
class Mqtt(override var kHomeAssistant: () -> KHomeAssistant?) : Domain<Nothing> {
    override val domainName = "mqtt"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Mqtt.' from a KHomeAssistantContext instead of using Mqtt directly.""".trimMargin()
    }

    /** Making sure Mqtt acts as a singleton. */
    override fun equals(other: Any?) = other is Mqtt
    override fun hashCode(): Int = domainName.hashCode()

    override fun Entity(name: String): Nothing = throw DomainHasNoEntityException()

    /** Publish a message to an MQTT topic
     * @param topic Topic to publish payload to.
     * @param payload Payload to publish.
     * @param payload_template Template to render as payload value.
     * @param qos Quality of Service to use.
     * @param retain If message should have the retain flag set. (default: false)
     * @return result
     * */
    suspend fun publish(
        topic: String,
        payload: String? = null,
        payload_template: String? = null,
        qos: Int? = null,
        retain: Boolean = false
    ): ResultMessage = callService(
        serviceName = "publish",
        data = json {
            "topic" to topic
            if (payload != null && payload_template != null || payload == null && payload_template == null)
                throw IllegalArgumentException("You need to include either payload or payload_template, but not both.\n")

            payload?.let { "payload" to it }
            payload_template?.let { "payload_template" to it }
            qos?.let { "qos" to it }
            "retain" to retain
        }
    )

    /** Listen to the specified topic matcher and dumps all received messages within a specific duration into
     * the file mqtt_dump.txt in your configuration folder. This is useful when debugging a problem.
     * @param topic Topic to dump. Can contain a wildcard (# or +).
     * @param duration Duration that we will listen for messages. Default is 5 seconds.
     * @return result
     * */
    suspend fun dump(
        topic: String,
        duration: TimeSpan = 5.seconds
    ): ResultMessage = callService(
        serviceName = "dump",
        data = json {
            "topic" to topic
            "duration" to duration.seconds
        }
    )
}

/** Access the Mqtt Domain. */
val HasContext.Mqtt: Mqtt
    get() = Mqtt(getKHomeAssistant)