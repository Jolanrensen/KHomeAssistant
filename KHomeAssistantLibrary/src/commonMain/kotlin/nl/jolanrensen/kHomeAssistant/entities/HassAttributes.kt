package nl.jolanrensen.kHomeAssistant.entities

/** Alias for [HassAttributes]. */
typealias BaseHassAttributes = HassAttributes

/**
 * Base attributes for any entity.
 * Some attributes might not be present on an entity, which will throw an exception.
 * These attributes correspond directly to the attributes found in Home Assistant.
 **/
interface HassAttributes {
    /** Name of the entity as displayed in the UI. */
    val friendly_name: String

    /** Is true if the entity is hidden. */
    val hidden: Boolean

    /** URL used as picture for entity. */
    val entity_picture: String

    /** Icon used for this enitity. Usually of the kind "mdi:icon" */
    val icon: String

    /** For switches with an assumed state two buttons are shown (turn off, turn on) instead of a switch. If assumed_state is false you will get the default switch icon. */
    val assumed_state: Boolean

    /** The class of the device as set by configuration, changing the device state and icon that is displayed on the UI (see below). It does not set the unit_of_measurement.*/
    val device_class: String

    /** Defines the units of measurement, if any. This will also influence the graphical presentation in the history visualisation as continuous value. Sensors with missing unit_of_measurement are showing as discrete values. */
    val unit_of_measurement: String?

    /** Defines the initial state for automations, on or off. */
    val initial_state: String

    /** ID given by Home Assistant if the entity was created from the UI instead of the yaml. */
    val id: String
}