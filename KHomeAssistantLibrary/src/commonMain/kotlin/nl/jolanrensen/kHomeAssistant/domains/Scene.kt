package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.TimeSpan
import kotlinx.serialization.json.*
import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.SceneEntityState
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.plus

/**
 * https://www.home-assistant.io/integrations/scene/
 */
class Scene(override var getKHass: () -> KHomeAssistant?) : Domain<Scene.Entity> {
    override val domainName = "scene"

    override fun checkContext() = require(getKHass() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Scene.' from a KHomeAssistantContext instead of using Scene directly.""".trimMargin()
    }

    /** Making sure Scene acts as a singleton. */
    override fun equals(other: Any?) = other is Scene
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload all scenes in Home Assistant */
    suspend fun reload(): ResultMessage = callService("reload")

    /**
     * With the scene.apply service you are able to apply a scene without first defining it via configuration. Instead, you pass the states as part of the service data.
     * @param data the entity's states and attributes that define this scene.
     * @param transition smoothen the transition to the scene.
     */
    suspend fun apply(data: SceneEntityState<out Any>, transition: TimeSpan? = null): ResultMessage =
        apply(listOf(data), transition)

    /**
     * With the scene.apply service you are able to apply a scene without first defining it via configuration. Instead, you pass the states as part of the service data.
     * @param data the entities' states and attributes that define this scene.
     * @param transition smoothen the transition to the scene.
     */
    suspend fun apply(data: Iterable<SceneEntityState<out Any>>, transition: TimeSpan? = null): ResultMessage =
        callService(
            serviceName = "apply",
            data = json {
                transition?.let {
                    if (it < TimeSpan.ZERO)
                        throw IllegalArgumentException("incorrect transition $it")
                    "transition" to transition
                }
                "entities" to getEntitiesObject(data)
            }
        )

    /**
     * Create a new scene without having to configure it by calling the scene.create service. This scene will be discarded after reloading the configuration.
     * @param sceneId the ID that will belong to the scene. Can be used to overwrite scene.
     * @param data the entities' states and attributes that define this scene.
     * @param snapshotEntities entities here will have their current state and attributes copied over to the scene. */
    suspend fun create(sceneId: String, data: Iterable<SceneEntityState<out Any>>, snapshotEntities: Iterable<BaseEntity<*>>? = null): ResultMessage =
        callService(
            serviceName = "create",
            data = json {
                "scene_id" to sceneId.toLowerCase().replace(" ", "_")
                "entities" to getEntitiesObject(data)

                snapshotEntities?.let {
                    "snapshot_entities" to JsonArray(it.map { JsonPrimitive(it.entityID) })
                }

            }
        )

    private fun getEntitiesObject(data: Iterable<SceneEntityState<out Any>>): JsonObject = json {
        for (sceneEntityState in data) {
            @Suppress("UNCHECKED_CAST")
            sceneEntityState as SceneEntityState<Any>
            sceneEntityState.entity.entityID to json {
                "state" to sceneEntityState.entity.stateToString(sceneEntityState.state)
            } + sceneEntityState.attributes
        }
    }

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : DefaultEntity(
        getKHass = getKHass,
        name = name,
        domain = Scene(getKHass)
    ) {

        init {
            attributes += arrayOf(
                ::entities
            )
        }

        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated(message = "state will always be \"scening\"", level = DeprecationLevel.WARNING)
        override var state: String
            get() = super.state
            set(value) {
                super.state = value
            }

        /** All entities that are affected by this scene (as [DefaultEntity]s) */
        val entities: List<DefaultEntity>
            get() = rawAttributes["entity_id"]!!
                .jsonArray
                .content
                .map {
                    val (domain, name) = it.content.split(".")
                    Domain(domain)[name]
                }

        /** Activate this scene.
         * @param transition Transition duration it takes to bring devices to the state defined in the scene. */
        suspend fun turnOn(transition: TimeSpan? = null): ResultMessage = callService(
            serviceName = "turn_on",
            data = json {
                transition?.let {
                    if (it < TimeSpan.ZERO)
                        throw IllegalArgumentException("incorrect transition $it")
                    "transition" to transition.seconds
                }
            }
        )

    }
}

val HasKHassContext.Scene: Scene
    get() = Scene(getKHass)