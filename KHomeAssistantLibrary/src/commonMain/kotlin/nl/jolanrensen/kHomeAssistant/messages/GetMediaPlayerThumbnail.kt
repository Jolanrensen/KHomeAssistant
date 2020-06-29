package nl.jolanrensen.kHomeAssistant.messages

import kotlinx.serialization.Serializable
import nl.jolanrensen.kHomeAssistant.JsonSerializable

@Serializable
data class GetMediaPlayerThumbnail(
    override var id: Int = 0,
    override val type: String = "media_player_thumbnail",
    val entity_id: String
) : Message()

@Serializable
data class GetMediaPlayerThumbnailResult(
    override var id: Int = 0,
    override val type: String = "result",
    override val success: Boolean,
    override val result: EncodedImage? = null
) : ResultMessage()

@Serializable
data class EncodedImage(
    val content_type: String = "image/jpeg",
    val content: String
) : JsonSerializable