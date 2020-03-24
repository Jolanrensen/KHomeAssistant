import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.Attributes
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.Entity

// The state can be any type, including enums. Just make sure to implement the getStateValue() and parseStateValue() in your Entity class.
enum class DemoState(val stateValue: String) {
    STATE1("state1"), STATE2("state1")
}

// These are the attributes that get parsed from Home Assistant for your entity.
@Serializable
data class DemoAttributes(
        override val friendly_name: String = ""
        // Add attributes here like `val Attribute: Type? = null`
) : Attributes() {
    override var fullJsonObject = JsonObject(mapOf())
}

// This class defines your entity, it can be instantiated via YourDomain.YourEntity(name: String)
// Define all the service calls on your entity inside here
// This also includes listeners for state changes
class DemoEntity(
        override val kHomeAssistant: () -> KHomeAssistant? = { null },
        override val name: String
) : Entity<DemoState, DemoAttributes>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = DemoDomain
) {
    override val attributesSerializer = DemoAttributes.serializer()
    override fun getStateValue(state: DemoState) = state.stateValue
    override fun parseStateValue(stateValue: String) = try {
        DemoState.values().find { it.stateValue == stateValue }
    } catch (e: Exception) {
        null
    }

    fun exampleListener(callback: DemoEntity.() -> Unit) {

    }

    suspend fun exampleServiceCall() {
        callService("")
    }

    // Example function that uses the state
    suspend fun isInState1(): Boolean {
        return getState() == DemoState.STATE1
    }



}

object DemoDomain : Domain<DemoEntity> {
    override val domainName: String = "yourDomain"
    override var kHomeAssistant: () -> KHomeAssistant? = { null }

    override fun Entity(name: String) = DemoEntity(kHomeAssistant, name)

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Demo.' from a KHomeAssistantContext instead of using DemoDomain directly.""".trimMargin()
    }
}

/** Access your domain, and set the context correctly */
val KHomeAssistantContext.Demo get() = DemoDomain.also { it.kHomeAssistant = kHomeAssistant }