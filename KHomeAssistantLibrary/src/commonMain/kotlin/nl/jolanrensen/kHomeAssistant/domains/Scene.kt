package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.TimeSpan
import kotlinx.serialization.json.*
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.SceneEntityState
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.DefaultEntity
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.plus

/**
 * https://www.home-assistant.io/integrations/scene/
 */
class Scene(kHassInstance: KHomeAssistant) : Domain<Scene.Entity>, KHomeAssistant by kHassInstance {
    override val domainName = "scene"

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
    suspend fun create(
        sceneId: String,
        data: Iterable<SceneEntityState<out Any>>,
        snapshotEntities: Iterable<nl.jolanrensen.kHomeAssistant.entities.Entity<*>>? = null
    ): ResultMessage =
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

    override fun Entity(name: String): Entity = Entity(kHassInstance = this, name = name)

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<String>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Scene(kHassInstance)
    ) {

        override val hassAttributes: Array<Attribute<*>> = super.hassAttributes + ::entity_id


        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated(message = "state will always be \"scening\"", level = DeprecationLevel.WARNING)
        override var state: String
            get() = super.state
            set(value) {
                super.state = value
            }

        /** All entities that are affected by this scene (as [DefaultEntity]s) */
        val entity_id: List<DefaultEntity>
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

val KHomeAssistant.Scene: Scene
    get() = Scene(this)