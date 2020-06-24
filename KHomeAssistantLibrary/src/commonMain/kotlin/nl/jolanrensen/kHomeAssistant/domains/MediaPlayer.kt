package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.parseUtc
import com.soywiz.klock.seconds
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.cast
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer.MediaContentType.*
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer.MediaPlayerState.*
import nl.jolanrensen.kHomeAssistant.domains.MediaPlayer.SupportedFeatures.*
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.HASS_DATE_FORMAT
import nl.jolanrensen.kHomeAssistant.helper.UnsupportedFeatureException
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.math.max

/**
 * https://www.home-assistant.io/integrations/media_player/
 *
 * TODO get attributes from here https://github.com/home-assistant/core/blob/dev/homeassistant/components/media_player/__init__.py
 * */
class MediaPlayer(kHassInstance: KHomeAssistant) : Domain<MediaPlayer.Entity>, KHomeAssistant by kHassInstance {
    override val domainName = "media_player"

    /** Making sure MediaPlayer acts as a singleton. */
    override fun equals(other: Any?) = other is MediaPlayer
    override fun hashCode(): Int = domainName.hashCode()

    // TODO maybe. All services also work with "all" as entity_id to control all media players

    enum class MediaPlayerState(val value: String) {
        OFF("off"),
        ON("on"),
        PLAYING("playing"),
        PAUSED("paused"),
        IDLE("idle")

    }

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

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    @Suppress("RemoveExplicitTypeArguments")
    @OptIn(ExperimentalStdlibApi::class)
    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<MediaPlayerState>(
        kHassInstance = kHassInstance,
        name = name,
        domain = MediaPlayer(kHassInstance)
    ) {

        init {
            this.hassAttributes += arrayOf(
                ::media_content_type,
                ::media_content_id,
                ::media_duration,
                ::media_position_updated_at,
                ::media_image_url,
                ::media_image_remotely_accessible,
                ::media_image_hash,
                ::media_title,
                ::media_artist,
                ::media_album_name,
                ::media_album_artist,
                ::media_track,
                ::media_series_title,
                ::media_season,
                ::media_episode,
                ::media_channel,
                ::media_playlist,
                ::app_id,
                ::app_name,
                ::source_list,
                ::sound_mode_list,
                ::supported_features,
                ::device_class,
                ::volume_level,
                ::is_volume_muted,
                ::media_position,
                ::source,
                ::sound_mode,
                ::shuffle
            )
        }

        /** state can also be writable. */
        override var state: MediaPlayerState
            get() = super.state
            set(value) {
                runBlocking { switchTo(value) }
            }


        // ----- Attributes -----
        // read only

        /** Content type of current playing media. */
        val media_content_type: String by attrsDelegate()

        /** Content ID of current playing media. */
        val media_content_id: String by attrsDelegate()

        /** Duration of current playing media. */
        val media_duration: TimeSpan
            get() {
                val value: Float = rawAttributes[::media_duration.name]!!.cast()!!
                return value.seconds
            }

        /** When was the position of the current playing media valid. */
        val media_position_updated_at: DateTime
            get() {
                val value: String = rawAttributes[::media_position_updated_at.name]!!.cast()!!
                return value.let { HASS_DATE_FORMAT.parseUtc(it) }
            }

        /** Image url of current playing media. */
        val media_image_url: String by attrsDelegate()

        /** If the image url is remotely accessible. */
        val media_image_remotely_accessible: Boolean by attrsDelegate()

        /** Hash value for media image. */
        val media_image_hash: String by attrsDelegate()

        /** Title of current playing media. */
        val media_title: String by attrsDelegate()

        /** Artist of current playing media, music track only. */
        val media_artist: String by attrsDelegate()

        /** Album name of current playing media, music track only. */
        val media_album_name: String by attrsDelegate()

        /** Album artist of current playing media, music track only. */
        val media_album_artist: String by attrsDelegate()

        /** Track number of current playing media, music track only. */
        var media_track: Int // TODO check
            get() = attrsDelegate<Int>().getValue(this, ::media_track)
            @Deprecated(level = DeprecationLevel.WARNING, message = "Should work but use with caution!")
            set(value) {
                runBlocking {
                    while (media_track < value)
                        mediaNextTrack()
                    while (media_track > value)
                        mediaPreviousTrack()
                }
            }

        /** Title of series of current playing media, TV show only. */
        val media_series_title: String by attrsDelegate()

        /** Season of current playing media, TV show only. */
        val media_season: Int by attrsDelegate() // TODO check

        /** Episode of current playing media, TV show only. */
        val media_episode: String by attrsDelegate()

        /** Channel currently playing. */
        val media_channel: Int by attrsDelegate() // TODO check

        /** Title of playlist currently playing. */
        val media_playlist: String by attrsDelegate()

        /** ID of the currently running app. */
        val app_id: String by attrsDelegate()

        /** Name of the currently running app. */
        val app_name: String by attrsDelegate()

        /** List of available input sources. */
        val source_list: List<String> by attrsDelegate(listOf())

        /** List of available sound modes. */
        val sound_mode_list: List<String> by attrsDelegate(listOf())

        /** Set of supported features. */
        val supported_features: Set<SupportedFeatures>
            get() = buildSet {
                val value: Int = rawAttributes[::supported_features.name]!!.cast<Int>()!!
                SupportedFeatures.values().forEach {
                    if (it.value and value == it.value)
                        add(it)
                }
            }

        /** The class of the device as set by configuration, changing the device state and icon that is displayed on the UI (see below). It does not set the unit_of_measurement.*/
//        val device_class: String by attrsDelegate()


        // read / write

        /** Volume level of the media player (0f..1f). */
        var volume_level: Float
            get() = attrsDelegate<Float>().getValue(this, ::volume_level)
            set(value) {
                runBlocking { volumeSet(value) }
            }

        /** Boolean if volume is currently muted. */
        var is_volume_muted: Boolean
            get() = attrsDelegate<Boolean>().getValue(this, ::is_volume_muted)
            set(value) {
                runBlocking { volumeMute(value) }
            }

        /** Position of current playing media. */
        var media_position: TimeSpan
            get() {
                val value: Float = rawAttributes[::media_position.name]!!.cast()!!
                return value.seconds
            }
            set(value) {
                runBlocking { mediaSeek(value) }
            }

        /** Name of the current input source. */
        var source: String
            get() = attrsDelegate<String>().getValue(this, ::source)
            set(value) {
                runBlocking { selectSource(value) }
            }

        /** Name of the current sound mode. */
        var sound_mode: String
            get() = attrsDelegate<String>().getValue(this, ::sound_mode)
            set(value) {
                runBlocking { selectSoundMode(value) }
            }

        /** Boolean if shuffle is enabled. */
        var shuffle: Boolean
            get() = attrsDelegate<Boolean>().getValue(this, ::shuffle)
            set(value) {
                runBlocking { shuffleSet(value) }
            }


        // some helper attributes

        var isOn: Boolean
            get() = state != OFF
            set(value) {
                runBlocking {
                    if (value) turnOn() else turnOff()
                }
            }

        var isOff: Boolean
            get() = state == OFF
            set(value) {
                runBlocking {
                    if (value) turnOff() else turnOn()
                }
            }

        var isPlaying: Boolean
            get() = state == PLAYING
            set(value) {
                runBlocking {
                    if (value) mediaPlay() else mediaPause()
                }
            }

        var isPaused: Boolean
            get() = state == PAUSED
            set(value) {
                runBlocking {
                    if (value) mediaPause() else mediaPlay()
                }
            }

        var isIdle: Boolean
            get() = state == IDLE || state == ON
            set(value) {
                runBlocking {
                    if (value) mediaStop() else mediaPlay()
                }
            }

        private fun checkIfSupported(vararg supportedFeatures: SupportedFeatures) {
            supportedFeatures.forEach {
                if (it !in supported_features)
                    throw UnsupportedFeatureException("Unfortunately the media player $name does not support ${it.name}.")
            }
        }


        /** Turns on media player */
        suspend inline fun turnOn(async: Boolean = false): ResultMessage {
            val result = callService("turn_on")
            if (!async) suspendUntilStateChanged({ it != OFF })
            return result
        }

        /** Turns off media player */
        suspend inline fun turnOff(async: Boolean = false): ResultMessage {
            val result = callService("turn_off")
            if (!async) suspendUntilStateChangedTo(OFF)
            return result
        }

        /** Toggles media player */
        suspend inline fun toggle(async: Boolean = false): ResultMessage {
            val oldState = state
            val result = callService("toggle")
            if (!async) suspendUntilStateChanged({
                when (oldState) {
                    ON, PLAYING, PAUSED, IDLE -> it == OFF
                    OFF -> it == ON || it == PLAYING || it == PAUSED || it == IDLE
                }
            })
            return result
        }

        suspend inline fun switchTo(state: MediaPlayerState, async: Boolean = false) {
            when (state) {
                ON -> turnOn(async)
                OFF -> turnOff(async)
                PLAYING -> mediaPlay(async)
                PAUSED -> mediaPause(async)
                IDLE -> mediaStop(async)
            }
        }

        suspend fun volumeUp(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_VOLUME_STEP, SUPPORT_VOLUME_SET)
            val currentVolumeLevel = volume_level
            val result = callService("volume_up")

            try {
                if (!async) suspendUntilAttributeChanged(::volume_level, { it > currentVolumeLevel })
            } catch (e: Exception) {
            }

            return result
        }

        suspend fun volumeDown(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_VOLUME_STEP, SUPPORT_VOLUME_SET)
            val currentVolumeLevel = volume_level
            val result = callService("volume_down")

            try {
                if (!async) suspendUntilAttributeChanged(::volume_level, { it < currentVolumeLevel })
            } catch (e: Exception) {
            }

            return result
        }

        suspend fun volumeSet(volumeLevel: Float, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_VOLUME_SET)
            val result = callService(
                serviceName = "volume_set",
                data = json {
                    volumeLevel.let {
                        if (it !in 0f..1f)
                            throw IllegalArgumentException("incorrect volumeLevel $it")
                        "volume_level" to it
                    }
                }
            )

            if (!async) suspendUntilAttributeChangedTo(::volume_level, volumeLevel)

            return result
        }

        suspend fun volumeMute(mute: Boolean = true, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_VOLUME_MUTE)
            val result = callService(
                serviceName = "volume_mute",
                data = json {
                    "is_volume_muted" to mute
                }
            )

            if (!async) suspendUntilAttributeChangedTo(::is_volume_muted, mute)

            return result
        }

        suspend fun volumeUnmute(async: Boolean = false) = volumeMute(false, async)

        suspend fun mediaPlayPause(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_PAUSE, SUPPORT_PLAY)
            val oldState = state
            val result = callService("media_play_pause")
            if (!async) suspendUntilStateChanged({
                when (oldState) {
                    OFF, PAUSED, IDLE, ON -> it == PLAYING
                    PLAYING -> it == PAUSED
                }
            })
            return result
        }

        suspend fun mediaPlay(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_PLAY)
            val result = callService("media_play")
            if (!async) suspendUntilStateChangedTo(PLAYING)
            return result
        }

        suspend fun mediaPause(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_PAUSE)
            val result = callService("media_pause")
            if (!async) suspendUntilStateChangedTo(PAUSED)
            return result
        }

        suspend fun mediaStop(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_STOP)
            val result = callService("media_stop")
            if (!async) suspendUntilStateChanged({ it == IDLE || it == ON })
            return result
        }

        suspend fun mediaNextTrack(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_NEXT_TRACK)
            val currentTrack = media_track
            val result = callService("media_next_track")

            try {
                if (!async) suspendUntilAttributeChangedTo(::media_track, currentTrack + 1)
            } catch (e: Exception) {
            }

            return result
        }

        suspend fun mediaPreviousTrack(async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_PREVIOUS_TRACK)
            val currentTrack = media_track
            val result = callService("media_previous_track")

            // TODO test if it's skipping to the beginning of the song or the previous one
            try {
                if (!async) suspendUntilAttributeChangedTo(::media_track, max(currentTrack - 1, 1))
            } catch (e: Exception) {
            }

            return result
        }

        suspend fun clearPlaylist(): ResultMessage {
            checkIfSupported(SUPPORT_CLEAR_PLAYLIST)
            return callService("clear_playlist")
        }

        suspend fun mediaSeek(seekPosition: TimeSpan, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SEEK)
            val result = callService(
                serviceName = "media_seek",
                data = json {
                    "seek_position" to seekPosition.seconds
                }
            )

            if (!async) suspendUntilAttributeChangedTo(::media_position, seekPosition)

            return result
        }

        suspend fun playMedia(mediaContentType: String, mediaContentId: String): ResultMessage {
            checkIfSupported(SUPPORT_PLAY_MEDIA)
            return callService(
                serviceName = "play_media",
                data = json {
                    "media_content_id" to mediaContentId
                    "media_content_type" to mediaContentType
                }
            )
        }

        suspend fun playMedia(mediaContentType: MediaContentType, mediaContentId: String) =
            playMedia(mediaContentType.value, mediaContentId)

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

        suspend fun selectSource(source: String, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SELECT_SOURCE)
            val result = callService(
                serviceName = "select_source",
                data = json {
                    source.let {
                        if (it !in source_list)
                            throw IllegalArgumentException("incorrect source $it")
                        "source" to it
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::source, source)
            return result
        }

        suspend fun selectSoundMode(soundMode: String, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SELECT_SOUND_MODE)
            val result = callService(
                serviceName = "select_sound_mode",
                data = json {
                    soundMode.let {
                        if (it !in sound_mode_list)
                            throw IllegalArgumentException("incorrect sound mode $it")
                        "sound_mode" to it
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::sound_mode, soundMode)
            return result
        }

        suspend fun shuffleSet(shuffle: Boolean, async: Boolean = false): ResultMessage {
            checkIfSupported(SUPPORT_SHUFFLE_SET)
            val result = callService(
                serviceName = "shuffle_set",
                data = json {
                    shuffle.let {
                        "shuffle" to it
                    }
                }
            )
            if (!async) suspendUntilAttributeChangedTo(::shuffle, shuffle)
            return result
        }
    }
}


/** Access the MediaPlayer Domain */
val KHomeAssistant.MediaPlayer: MediaPlayer
    get() = MediaPlayer(this)