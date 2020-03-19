package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable

@Serializable
data class SubscribeToEventMessage(
        override val id: Int,
        override val type: String = "subscribe_events"
) : Message()

@Serializable
data class SubscribeToEventOfTypeMessage(
        override val id: Int,
        override val type: String = "subscribe_events",
        val event_type: String? = null // "state_changed" is an option
) : Message()