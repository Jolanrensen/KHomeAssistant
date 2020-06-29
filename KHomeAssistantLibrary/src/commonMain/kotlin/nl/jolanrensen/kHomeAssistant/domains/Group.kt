package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.OnOff.*
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.WriteOnlyException
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.toJson

/**
 * https://www.home-assistant.io/integrations/group/
 */
class Group(override val kHassInstance: KHomeAssistant) : Domain<Group.Entity> {
    override val domainName = "group"

    /** Making sure Group acts as a singleton. */
    override fun equals(other: Any?) = other is Group
    override fun hashCode(): Int = domainName.hashCode()

    /** Reloads all groups. */
    suspend fun reload(): ResultMessage = callService("reload")

    // TODO test whether you can do anything with the newly created group
    /**
     * Create a user group.
     * @param groupName Group id and part of entity id.
     * @param friendlyName Readable name of the group.
     * @param icon Name of icon for the group. (like "mdi:camera")
     * @param entities List of all members in the group.
     * @param all Enable this option if the group should only turn on when all entities are on.
     * @return [ResultMessage]
     */
    suspend fun set(
        groupName: String,
        friendlyName: String? = null,
        icon: String? = null,
        entities: Iterable<BaseEntity<*, *>>,
        all: Boolean = false,
        async: Boolean = false
    ): ResultMessage {
        val result = callService(
            serviceName = "set",
            data = json {
                "object_id" to groupName.toLowerCase().replace(" ", "_")
                friendlyName?.let { "name" to it }
                icon?.let { "icon" to it }
                "entities" to entities.map { it.entityID }.toJson()
                "all" to all
            }
        )
        if (!async) kHassInstance.suspendUntilEntityExists(this, groupName)
        return result
    }

    /**
     * Create a user group. Alias for [set].
     * @param groupName Group id and part of entity id.
     * @param friendlyName Readable name of the group.
     * @param icon Name of icon for the group. (like "mdi:camera")
     * @param entities List of all members in the group.
     * @param all Enable this option if the group should only turn on when all entities are on.
     * @return pair of newly created [Group.Entity] and [ResultMessage]
     */
    suspend fun create(
        groupName: String,
        friendlyName: String? = null,
        icon: String? = null,
        entities: Iterable<BaseEntity<*, *>>,
        all: Boolean = false,
        async: Boolean = false
    ): ResultMessage = set(
        groupName = groupName,
        friendlyName = friendlyName,
        icon = icon,
        entities = entities,
        all = all,
        async = async
    )

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BaseHassAttributes {
        // Read only

        /** The order of this group. */
        val order: Int

        /** True if the automation is not user-defined. */
        val auto: Boolean

        // Read / Write

        /** All entities that are affected by this scene (as [DefaultEntity]s) @see [getEntities] or [Entity.entities]. */
        var entity_id: List<String>

        /** Name of icon for the group. (like "mdi:camera") */
        override var icon: String


        // Write only / Helper

        /** Readable name of the group. */
        var friendlyName: String
            @Deprecated("'friendlyName' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'friendlyName' is write only")
            set(_) = error("must be overridden")

        /** Enable this option if the group should only turn on when all entities are on. */
        var all: Boolean
            @Deprecated("'all' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'all' is write only")
            set(_) = error("must be overridden")


        // Helper

        /** All entities that are in this group (as [DefaultEntity]s) */
        fun getEntities(kHassInstance: KHomeAssistant): List<DefaultEntity> =
            entity_id.map {
                val (domain, name) = it.split(".")
                kHassInstance.Domain(domain)[name]
            }

        /** Replace all the members in this group. */
        fun replaceEntities(entities: List<BaseEntity<*, *>>) {
            entity_id = entities.map { it.entityID }
        }
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : ToggleEntity<HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Group(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        @Suppress("UNCHECKED_CAST")
        override suspend fun <V> setValue(propertyName: String, value: V) {
            when (propertyName) {
                ::entity_id.name -> update(
                    entities = (value as List<String>).map {
                        val (domain, name) = it.split(".")
                        kHassInstance.Domain(domain)[name]
                    }
                )
                ::icon.name -> update(icon = value as String)
                ::friendlyName.name -> update(friendlyName = value as String)
                ::all.name -> update(all = value as Boolean)
            }
        }


        // Attributes
        override val order: Int by attrsDelegate()
        override val auto: Boolean by attrsDelegate()
        override var entity_id: List<String> by attrsDelegate(listOf())
        override var icon: String by attrsDelegate()
        override var friendlyName: String
            @Deprecated("'friendlyName' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'friendlyName' is write only")
            set(value) = attrsDelegate<String>().setValue(this, ::friendlyName, value)
        override var all: Boolean
            @Deprecated("'all' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'all' is write only")
            set(value) = attrsDelegate<Boolean>().setValue(this, ::all, value)

        /** All entities that are in this group (as [DefaultEntity]s) */
        var entities: List<BaseEntity<*, *>>
            get() = getEntities(kHassInstance)
            set(value) = replaceEntities(value)

        /** Remove this user group. */
        suspend fun remove(): ResultMessage = domain.callService(
            serviceName = "remove",
            data = json { "object_id" to name }
        )

        /**
         * Update this group.
         * @param friendlyName Readable name of the group.
         * @param icon Name of icon for the group. (like "mdi:camera")
         * @param entities List that will replace the members in this group.
         * @param addEntities List of members that will be added to this group.
         * @param all Enable this option if the group should only turn on when all entities are on.
         * @return [ResultMessage]
         */
        suspend fun set(
            friendlyName: String? = null,
            icon: String? = null,
            entities: Iterable<BaseEntity<*, *>>? = null,
            addEntities: Iterable<BaseEntity<*, *>>? = null,
            all: Boolean? = null
        ): ResultMessage = domain.callService(
            serviceName = "set",
            data = json {
                "object_id" to name
                friendlyName?.let { "name" to it }
                icon?.let { "icon" to it }
                if (entities != null && addEntities != null)
                    throw IllegalArgumentException("You can't add and replace entities in the group at the same time.")
                entities?.let { "entities" to it.map { it.entityID }.toJson() }
                addEntities?.let { "add_entities" to it.map { it.entityID }.toJson() }
                all?.let { "all" to it }
            }
        )

        /**
         * Update this group. Alias for [set].
         * @param friendlyName Readable name of the group.
         * @param icon Name of icon for the group. (like "mdi:camera")
         * @param entities List that will replace the members in this group.
         * @param addEntities List of members that will be added to this group.
         * @param all Enable this option if the group should only turn on when all entities are on.
         * @return [ResultMessage]
         */
        suspend fun update(
            friendlyName: String? = null,
            icon: String? = null,
            entities: Iterable<BaseEntity<*, *>>? = null,
            addEntities: Iterable<BaseEntity<*, *>>? = null,
            all: Boolean? = null
        ): ResultMessage = set(
            friendlyName = friendlyName,
            icon = icon,
            entities = entities,
            addEntities = addEntities,
            all = all
        )

        /**
         * Add members to the group.
         * @param entities mebers to be added to the group.
         * @return result
         */
        suspend fun addEntities(vararg entities: BaseEntity<*, *>): ResultMessage =
            update(addEntities = entities.toList())

        /** Turns on the group using the [HomeAssistant] domain. */
        override suspend fun turnOn(async: Boolean): ResultMessage {
            val result = callService(kHassInstance.HomeAssistant, "turn_on")
            if (!async) suspendUntilStateChangedTo(ON)
            return result
        }

        /** Turns off the group using the [HomeAssistant] domain. */
        override suspend fun turnOff(async: Boolean): ResultMessage {
            val result = callService(kHassInstance.HomeAssistant, "turn_off")
            if (!async) suspendUntilStateChangedTo(OFF)
            return result
        }

        /** Toggles the group using the [HomeAssistant] domain. */
        override suspend fun toggle(async: Boolean): ResultMessage {
            val oldState = state
            val result = callService(kHassInstance.HomeAssistant, "toggle")
            if (!async) suspendUntilStateChangedTo(
                when (oldState) { // TODO
                    ON -> OFF
                    OFF -> ON
                    UNAVAILABLE, UNKNOWN -> ON
                }
            )
            return result
        }

    }
}

val KHomeAssistant.Group: Group
    get() = Group(this)