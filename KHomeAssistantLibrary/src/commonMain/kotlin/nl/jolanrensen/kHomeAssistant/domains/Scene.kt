package nl.jolanrensen.kHomeAssistant.domains

import com.soywiz.klock.TimeSpan
import kotlinx.serialization.json.*
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.SceneEntityState
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.plus

/**
 * https://www.home-assistant.io/integrations/scene/
 */
class Scene(override val kHassInstance: KHomeAssistant) : Domain<Scene.Entity> {
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
    suspend fun apply(
        data: SceneEntityState<*, *, *>,
        transition: TimeSpan? = null
    ): ResultMessage =
        apply(listOf(data), transition)

    /**
     * With the scene.apply service you are able to apply a scene without first defining it via configuration. Instead, you pass the states as part of the service data.
     * @param data the entities' states and attributes that define this scene.
     * @param transition smoothen the transition to the scene.
     */
    suspend fun apply(
        data: Iterable<SceneEntityState<*, *, *>>,
        transition: TimeSpan? = null
    ): ResultMessage =
        callService(
            serviceName = "apply",
            data = buildJsonObject {
                transition?.let {
                    if (it < TimeSpan.ZERO)
                        throw IllegalArgumentException("incorrect transition $it")
                    "transition" to transition
                }
                put("entities", getEntitiesObject(data))
            }
        )

    /**
     * Create a new scene without having to configure it by calling the scene.create service. This scene will be discarded after reloading the configuration.
     * @param sceneId the ID that will belong to the scene. Can be used to overwrite scene.
     * @param data the entities' states and attributes that define this scene.
     * @param snapshotEntities entities here will have their current state and attributes copied over to the scene. */
    suspend fun create(
        sceneId: String,
        data: Iterable<SceneEntityState<*, *, *>>,
        snapshotEntities: Iterable<BaseEntity<*, *>>? = null
    ): ResultMessage = callService(
        serviceName = "create",
        data = buildJsonObject {
            put("scene_id", sceneId.toLowerCase().replace(" ", "_"))
            put("entities", getEntitiesObject(data))

            snapshotEntities?.let {
                put("snapshot_entities", JsonArray(it.map { JsonPrimitive(it.entityID) }))
            }

        }
    )

    @Suppress("UNCHECKED_CAST")
    private fun getEntitiesObject(data: Iterable<SceneEntityState<*, *, *>>): JsonObject =
        buildJsonObject {
            for (sceneEntityState in data) {
                sceneEntityState as SceneEntityState<Any, BaseHassAttributes, BaseEntity<Any, BaseHassAttributes>>
                val entityClone = sceneEntityState.entity.clone().apply {
                    saveToJson = true
                }
                sceneEntityState.attributes.invoke(entityClone)
                put(
                    key = sceneEntityState.entity.entityID,
                    element = buildJsonObject {
                        put("state", sceneEntityState.entity.stateToString(sceneEntityState.state))
                    } + (entityClone.json ?: buildJsonObject { }) + sceneEntityState.additionalAttributes
                )

            }
        }

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BaseHassAttributes {
        // Read only

        /** All entities that are affected by this scene (as [DefaultEntity]s) @see [getEntities] or [Entity.entities]. */
        val entity_id: List<String>

        // Helper

        /** All entities that are affected by this scene (as [DefaultEntity]s) */
        fun getEntities(kHassInstance: KHomeAssistant): List<DefaultEntity> =
            entity_id.map {
                val (domain, name) = it.split(".")
                kHassInstance.Domain(domain)[name]
            }
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<String, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Scene(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        @Suppress("DeprecatedCallableAddReplaceWith")
        @Deprecated(message = "state will always be \"scening\"", level = DeprecationLevel.WARNING)
        override var state: String
            get() = super.state
            set(value) {
                super.state = value
            }

        @Deprecated("You can use the typed version", ReplaceWith("entities"))
        override val entity_id: List<String> by attrsDelegate(listOf())

        /** All entities that are affected by this scene (as [DefaultEntity]s) */
        val entities: List<DefaultEntity>
            get() = getEntities(kHassInstance)

        /** Activate this scene.
         * @param transition Transition duration it takes to bring devices to the state defined in the scene. */
        suspend fun turnOn(transition: TimeSpan? = null): ResultMessage = callService(
            serviceName = "turn_on",
            data = buildJsonObject {
                transition?.let {
                    if (it < TimeSpan.ZERO)
                        throw IllegalArgumentException("incorrect transition $it")
                    put("transition", transition.seconds)
                }
            }
        )

    }
}


val KHomeAssistant.Scene: Scene
    get() = Scene(this)