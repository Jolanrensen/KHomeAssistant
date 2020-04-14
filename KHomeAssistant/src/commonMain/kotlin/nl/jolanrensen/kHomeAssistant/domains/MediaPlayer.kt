package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer.MediaContentType.*
import nl.jolanrensen.kHomeAssistant.entities.ToggleEntity
import nl.jolanrensen.kHomeAssistant.entities.getValue
import nl.jolanrensen.kHomeAssistant.helper.UnsupportedFeatureException

/**
 * https://www.home-assistant.io/integrations/media_player/
 *
 * TODO get attributes from here https://github.com/home-assistant/core/blob/dev/homeassistant/components/media_player/__init__.py
 * */
object MediaPlayer : Domain<MediaPlayer.Entity> {
    override var kHomeAssistant: () -> KHomeAssistant? = { null }
    override val domainName = "media_player"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'MediaPlayer.' from a KHomeAssistantContext instead of using MediaPlayer directly.""".trimMargin()
    }

    // TODO maybe. All services also work with "all" as entity_id to control all media players

    enum class MediaContentType(val value: String) {
        MUSIC("music"),
        TVSHOW("tvshow"),
        VIDEO("video"),
        EPISODE("episode"),
        CHANNEL("channel"),
        PLAYLIST("playlist"),
        IMAGE("image"),
        URL("url"),
        GAME("game"),
        APP("app")
    }

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_PAUSE(1),
        SUPPORT_SEEK(2),
        SUPPORT_VOLUME_SET(4),
        SUPPORT_VOLUME_MUTE(8),
        SUPPORT_PREVIOUS_TRACK(16),
        SUPPORT_NEXT_TRACK(32),
        SUPPORT_TURN_ON(128),
        SUPPORT_TURN_OFF(256),
        SUPPORT_PLAY_MEDIA(512),
        SUPPORT_VOLUME_STEP(1024),
        SUPPORT_SELECT_SOURCE(2048),
        SUPPORT_STOP(4096),
        SUPPORT_CLEAR_PLAYLIST(8192),
        SUPPORT_PLAY(16384),
        SUPPORT_SHUFFLE_SET(32768),
        SUPPORT_SELECT_SOUND_MODE(65536)
    }

    override fun Entity(name: String): Entity = Entity(kHomeAssistant = kHomeAssistant, name = name)

    @Suppress("RemoveExplicitTypeArguments")
    @OptIn(ExperimentalStdlibApi::class)
    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : ToggleEntity(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = MediaPlayer
    ) {

        // ----- Attributes -----
        // read only

        val source_list: List<String>? by this

        val sound_mode_list: List<String>? by this

        val media_content_type: String? by this

        val media_title: String? by this

        val sound_mode_raw: String? by this

        /** Set of supported features. */
        val supported_features: Set<SupportedFeatures>
            get() = buildSet {
                val supported_features: Int? by this@Entity
                SupportedFeatures.values().forEach {
                    if (it.value and supported_features!! == it.value)
                        add(it)
                }
            }


        // read / write

        var volume_level: Float?
            get() = getValue(this, ::volume_level)
            set(value) {
                runBlocking { volumeSet(value!!) }
            }

        var is_volume_muted: Boolean?
            get() = getValue(this, ::is_volume_muted)
            set(value) {
                runBlocking { volumeMute(value!!) }
            }

        var source: String?
            get() = getValue(this, ::source)
            set(value) {
                runBlocking { selectSource(value!!) }
            }

        var sound_mode: String?
            get() = getValue(this, ::sound_mode)
            set(value) {
                runBlocking { selectSoundMode(value!!) }
            }

        /*
        source_list: AUX1, Blu-ray CD, Bluetooth, Favorites, Internet Radio, Media Server, Microfoons, Online Music, Pi, Platenspeler, SHIELD, Spotify, TV Audio, Tv Ontvanger, Wii, iPod/USB
        sound_mode_list: MUSIC, MOVIE, GAME, AUTO, STANDARD, VIRTUAL, MATRIX, ROCK ARENA, JAZZ CLUB, VIDEO GAME, MONO MOVIE, DIRECT, PURE DIRECT, DOLBY DIGITAL, DTS SURROUND, MCH STEREO, STEREO, ALL ZONE STEREO
        volume_level: 0.25
        is_volume_muted: false
        media_content_type: channel
        media_title: SHIELD
        source: SHIELD
        sound_mode: STEREO
        sound_mode_raw: Stereo
        friendly_name: denon_avrx2200w
        supported_features: 69004
         */


        private fun checkIfSupported(supportedFeature: SupportedFeatures) {
            if (supportedFeature !in supported_features)
                throw UnsupportedFeatureException("Unfortunately the media player $name does not support ${supportedFeature.name}.")
        }

        suspend fun volumeMute(mute: Boolean = true) =
            callService(
                serviceName = "volume_mute",
                data = buildMap<String, JsonPrimitive> {
                    this["is_volume_muted"] = JsonPrimitive(mute)
                }
            )

        suspend fun volumeUnmute() = volumeMute(false)

        suspend fun volumeSet(volumeLevel: Float) =
            callService(
                serviceName = "volume_set",
                data = buildMap<String, JsonElement> {
                    volumeLevel.let {
                        if (it !in 0f..1f)
                            throw IllegalArgumentException("incorrect volumeLevel $it")
                        this["volume_level"] = JsonPrimitive(it)
                    }
                }
            )

        suspend fun mediaSeek(seekPosition: Float) =
            callService(
                serviceName = "media_seek",
                data = buildMap<String, JsonElement> {
                    this["seek_position"] = JsonPrimitive(seekPosition)
                }
            )

        suspend fun playMedia(mediaContentType: MediaContentType, mediaContentId: String) =
            callService(
                serviceName = "play_media",
                data = buildMap<String, JsonElement> {
                    this["media_content_id"] = JsonPrimitive(mediaContentId)
                    this["media_content_type"] = JsonPrimitive(mediaContentType.value)
                }
            )

        suspend fun playMusic(mediaContentId: String) = playMedia(MUSIC, mediaContentId)
        suspend fun playTvShow(mediaContentId: String) = playMedia(TVSHOW, mediaContentId)
        suspend fun playVideo(mediaContentId: String) = playMedia(VIDEO, mediaContentId)
        suspend fun playEpisode(mediaContentId: String) = playMedia(EPISODE, mediaContentId)
        suspend fun playChannel(mediaContentId: String) = playMedia(CHANNEL, mediaContentId)
        suspend fun playPlaylist(mediaContentId: String) = playMedia(PLAYLIST, mediaContentId)
        suspend fun playImage(mediaContentId: String) = playMedia(IMAGE, mediaContentId)
        suspend fun playUrl(mediaContentId: String) = playMedia(URL, mediaContentId)
        suspend fun playGame(mediaContentId: String) = playMedia(GAME, mediaContentId)
        suspend fun playApp(mediaContentId: String) = playMedia(APP, mediaContentId)

        suspend fun selectSource(source: String) =
            callService(
                serviceName = "select_source",
                data = buildMap<String, JsonElement> {
                    source.let {
                        if (source_list != null && it !in source_list!!)
                            throw IllegalArgumentException("incorrect source $it")
                        this["source"] = JsonPrimitive(it)
                    }
                }
            )

        suspend fun selectSoundMode(soundMode: String) =
            callService(
                serviceName = "select_sound_mode",
                data = buildMap<String, JsonElement> {
                    soundMode.let {
                        if (sound_mode_list != null && it !in sound_mode_list!!)
                            throw IllegalArgumentException("incorrect sound mode $it")
                        this["sound_mode"] = JsonPrimitive(it)
                    }
                }
            )

        suspend fun shuffleSet(shuffle: Boolean) =
            callService(
                serviceName = "shuffle_set",
                data = buildMap<String, JsonElement> {
                    shuffle.let {

                        this["shuffle"] = JsonPrimitive(it)
                    }
                }
            )
    }
}


typealias MediaPlayerDomain = MediaPlayer

/** Access the MediaPlayer Domain */
val KHomeAssistantContext.MediaPlayer: MediaPlayerDomain
    get() = MediaPlayerDomain.also { it.kHomeAssistant = kHomeAssistant }