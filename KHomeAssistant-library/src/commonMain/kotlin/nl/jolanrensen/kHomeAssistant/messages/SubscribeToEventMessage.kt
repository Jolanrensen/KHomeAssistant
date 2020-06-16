package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import nl.jolanrensen.kHomeAssistant.JsonSerializable

@Serializable
data class SubscribeToEventMessage(
        override var id: Int = 0,
        override val type: String = "subscribe_events"
) : Message(), JsonSerializable

@Serializable
data class SubscribeToEventOfTypeMessage(
        override var id: Int = 0,
        override val type: String = "subscribe_events",
        val event_type: String? = null // "state_changed" is an option
) : Message(), JsonSerializable