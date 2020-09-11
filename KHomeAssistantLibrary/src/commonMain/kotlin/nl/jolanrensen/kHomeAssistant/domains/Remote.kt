package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.toJson

/**
 * https://www.home-assistant.io/integrations/remote/
 *
 */
class Remote(override val kHassInstance: KHomeAssistant) : Domain<Remote.Entity> {
    override val domainName: String = "remote"

    /** Making sure Remote acts as a singleton. */
    override fun equals(other: Any?) = other is Remote
    override fun hashCode(): Int = domainName.hashCode()

    override fun Entity(name: String): Entity = Entity(name = name, kHassInstance = kHassInstance)

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_LEARN_COMMAND(1)
    }

    // Helper commands. These are just hints and might not work for your device!

    // Apple TV
    object AppleTVCommands {
        const val UP = "up"
        const val DOWN = "down"
        const val LEFT = "left"
        const val RIGHT = "right"
        const val MENU = "menu"
        const val TOP_MENU = "top_menu"
        const val SELECT = "select"
    }

    // Roku
    object RokuCommands {
        const val BACK = "back"
        const val BACKSPACE = "backspace"
        const val CHANNEL_DOWN = "channel_down"
        const val CHANNEL_UP = "channel_up"
        const val DOWN = "down"
        const val ENTER = "enter"
        const val FIND_REMOTE = "find_remote"
        const val FORWARD = "forward"
        const val HOME = "home"
        const val INFO = "info"
        const val INPUT_AV1 = "input_av1"
        const val INPUT_HDMI1 = "input_hdmi1"
        const val INPUT_HDMI2 = "input_hdmi2"
        const val INPUT_HDMI3 = "input_hdmi3"
        const val INPUT_HDMI4 = "input_hdmi4"
        const val INPUT_TUNER = "input_tuner"
        const val LEFT = "left"
        const val LITERAL = "literal"
        const val PLAY = "play"
        const val POWER = "power"
        const val REPLAY = "replay"
        const val REVERSE = "reverse"
        const val RIGHT = "right"
        const val SEARCH = "search"
        const val SELECT = "select"
        const val UP = "up"
        const val VOLUME_DOWN = "volume_down"
        const val VOLUME_MUTE = "volume_mute"
        const val VOLUME_UP = "volume_up"
    }


    interface HassAttributes : BaseHassAttributes {
        // Read only

        /** Set of supported features. @see [supportedFeatures]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        val supported_features: Int


        // Helper

        /** Set of supported features. */
        @OptIn(ExperimentalStdlibApi::class)
        val supportedFeatures: Set<SupportedFeatures>
            get() = buildSet {
                val value = supported_features
                SupportedFeatures.values().forEach {
                    if (it.value and value == it.value)
                        add(it)
                }
            }
    }

    class Entity(
        override val name: String,
        override val kHassInstance: KHomeAssistant
    ) : ToggleEntity<HassAttributes>(
        name = name,
        kHassInstance = kHassInstance,
        domain = Remote(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()
        override val additionalToStringAttributes: Array<Attribute<*>> = super.additionalToStringAttributes +
                getHassAttributesHelpers<HassAttributes>()

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        override val supported_features: Int by attrsDelegate()

        /** Turn on with Activity ID or Activity Name to start. */
        suspend fun turnOn(activity: String, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "turn_on",
                data = buildJsonObject { put("activity", activity) }
            )
            if (!async) suspendUntilStateChangedTo(OnOff.ON)
            return result
        }

        /** Sends a command to a device.
         * @see [AppleTVCommands] or [RokuCommands]
         *
         * @param command A command to send.
         * @param device Optional device ID to send command to.
         * @param noRepeats An optional value that specifies the number of times you want to repeat the command(s). If not specified, the command(s) will not be repeated.
         * @param delay An optional value that specifies the time you want to wait in between repeated commands. If not specified, the default of 0.4 seconds will be used.
         * @param hold An optional value that specifies the time you want to have it held before the release is send. If not specified, the release will be send immediately after the press.
         **/
        suspend fun sendCommand(
            command: String,
            device: String? = null,
            noRepeats: Int? = null,
            delay: TimeSpan? = null,
            hold: TimeSpan? = null,
            async: Boolean = false
        ): ResultMessage = sendCommand(
            commands = listOf(command),
            device = device,
            noRepeats = noRepeats,
            delay = delay,
            hold = hold,
            async = async
        )

        /** Sends a list of commands to a device.
         * @see [AppleTVCommands] or [RokuCommands]
         *
         * @param commands A list of commands to send.
         * @param device Optional device ID to send command to.
         * @param noRepeats An optional value that specifies the number of times you want to repeat the command(s). If not specified, the command(s) will not be repeated.
         * @param delay An optional value that specifies the time you want to wait in between repeated commands. If not specified, the default of 0.4 seconds will be used.
         * @param hold An optional value that specifies the time you want to have it held before the release is send. If not specified, the release will be send immediately after the press.
         **/
        suspend fun sendCommand(
            commands: Iterable<String>,
            device: String? = null,
            noRepeats: Int? = null,
            delay: TimeSpan? = null,
            hold: TimeSpan? = null,
            async: Boolean = false
        ): ResultMessage {
            val result = callService(
                serviceName = "send_command",
                data = buildJsonObject {
                    put("command", commands.toList().toJson())
                    device?.let { put("device", it) }
                    noRepeats?.let { put("num_repeats", it) }
                    delay?.let { put("delay_secs", it.seconds) }
                    hold?.let { put("hold_secs", it.seconds) }
                }
            )
            if (!async) delay(((delay ?: 0.seconds) + (hold ?: 0.seconds)) * commands.count() * (noRepeats ?: 1))
            return result
        }

        /** Learns a command from a device.
         *
         * @param command A command to learn.
         * @param device Optional device ID to learn command from.
         * @param alternative Optional. If code must be stored as alternative (useful for discrete remotes).
         * @param timeout Optional. Timeout for the command to be learned.
         */
        suspend fun learnCommand(
            command: String,
            device: String? = null,
            alternative: Boolean? = null,
            timeout: TimeSpan? = null,
            async: Boolean = false
        ): ResultMessage = learnCommand(
            commands = listOf(command),
            device = device,
            alternative = alternative,
            timeout = timeout,
            async = async
        )

        /** Learns a list of commands from a device.
         *
         * @param commands A list of commands to learn.
         * @param device Optional device ID to learn command from.
         * @param alternative Optional. If code must be stored as alternative (useful for discrete remotes).
         * @param timeout Optional. Timeout for the command to be learned.
         */
        suspend fun learnCommand(
            commands: Iterable<String>,
            device: String? = null,
            alternative: Boolean? = null,
            timeout: TimeSpan? = null,
            async: Boolean = false
        ): ResultMessage {
            val result = callService(
                serviceName = "learn_command",
                data = buildJsonObject {
                    put("command", commands.toList().toJson())
                    device?.let { put("device", it) }
                    alternative?.let { put("alternative", it) }
                    timeout?.let { put("timeout", it.seconds) }
                }
            )
            if (!async) delay(timeout ?: 0.seconds)
            return result
        }

    }
}

/** Access the Remote Domain. */
val KHomeAssistant.Remote: Remote
    get() = Remote(this)