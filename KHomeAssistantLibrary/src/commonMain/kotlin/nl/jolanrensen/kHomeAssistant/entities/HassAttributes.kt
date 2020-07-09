package nl.jolanrensen.kHomeAssistant.entities

/** Alias for [HassAttributes]. */
typealias BaseHassAttributes = HassAttributes

/**
 * Base attributes for any entity.
 * Some attributes might not be present on an entity, which will throw an exception.
 * These attributes correspond directly to the attributes found in Home Assistant.
 * Helper functions or getter/setters are also allowed and encouraged to be defined in the interface
 * as long as they are implemented. Abstract attributes must correspond in name and type directly to Home Assistant.
 **/
interface HassAttributes {
    /** Name of the entity as displayed in the UI. */
    val friendly_name: String

    /** Is true if the entity is hidden. */
    val hidden: Boolean

    /** URL used as picture for entity. */
    val entity_picture: String

    /** Icon used for this entity. Usually of the kind "mdi:icon" */
    val icon: String

    /** For switches with an assumed state two buttons are shown (turn off, turn on) instead of a switch. If assumed_state is false you will get the default switch icon. */
    val assumed_state: Boolean

    /** Defines the initial state for automations, on or off. */
    val initial_state: String

    /** ID given by Home Assistant if the entity was created from the UI instead of the yaml. */
    val id: String
}