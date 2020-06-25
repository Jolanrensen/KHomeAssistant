package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.entities.Entity
import nl.jolanrensen.kHomeAssistant.entities.HassAttributes

/**
 * Used in [nl.jolanrensen.kHomeAssistant.domains.Scene] to describe the state and attributes
 * of the given [entity] in this scene.
 * [attributes] will not be checked as not all attributes in KHomeAssistant entities correspond
 * directly to Home Assistant attributes, but you can look inside the [entity] to find out about
 * certain attributes and their types.
 *
 * example:
 * ```
 * SceneEntityState(
 *      entity = Light["some_light"],
 *      state = ON,
 *      attributes = json {
 *          "brightness" to 100
 *          "color" to "green"
 *      }
 * )
 * ```
 *
 * @param entity the entity to describe the [state] and [attributes] for
 * @param state the state of the [entity] in the scene
 * @param attributes the attributes for the [entity] in the scene (will not be checked)
 */
class SceneEntityState<StateType : Any, AttrsType : HassAttributes>(
    val entity: Entity<StateType, AttrsType>,
    val state: StateType,
    val attributes: AttrsType.() -> Unit = {},
    val additionalAttributes: JsonObject = json {  }
)