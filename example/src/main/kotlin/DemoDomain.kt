import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.attributes.BaseAttributes
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity

// The state can be any type, including enums. Just make sure to implement the getStateValue() and parseStateValue() in your Entity class.
enum class ExampleState(val stateValue: String) {
    STATE1("state1"), STATE2("state1")
}

object Example : Domain<Example.Entity> {
    override val domainName: String = "example"
    override var kHomeAssistant: () -> KHomeAssistant? = { null }

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'Example.' from a KHomeAssistantContext instead of using ExampleDomain directly.""".trimMargin()
    }

    suspend fun exampleDomainServiceCall() {
        callService("")
    }

    // Constructor for Entity with the right context
    override fun Entity(name: String) = Entity(kHomeAssistant, name)


    /** This class defines your entity, it can be instantiated via YourDomain.YourEntity(name: String)
     * Define all the service calls on your entity inside here
     * This also includes listeners for state changes
    */
    class Entity(
            override val kHomeAssistant: () -> KHomeAssistant? = { null },
            override val name: String
    ) : BaseEntity<ExampleState, Entity.Attributes>(
            kHomeAssistant = kHomeAssistant,
            name = name,
            domain = Example
    ) {
        /** These are the attributes that get parsed from Home Assistant for your entity when calling getAttributes() */
        @Serializable
        data class Attributes(
                override val friendly_name: String = "",
                val testAttribute: Int? = null
                /** Add attributes here like `val Attribute: Type? = null` */
        ) : BaseAttributes {
            override var fullJsonObject: JsonObject = JsonObject(mapOf())
        }

        /** This is used to deserialize your attributes from Home Assistant */
        override val attributesSerializer: KSerializer<Attributes> = Attributes.serializer()

        /** Define how to convert your state type into a Home Assistant string state */
        override fun getStateValue(state: ExampleState): String = state.stateValue

        /** Define how to convert a Home Assistant string state into your state type */
        override fun parseStateValue(stateValue: String): ExampleState? = try {
            ExampleState.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        fun exampleListener(callback: Entity.() -> Unit) {

        }

        suspend fun exampleEntityServiceCall() {
            callService("")
        }

        // Example function that uses the state
        suspend fun isInState1(): Boolean {
            return getState() == ExampleState.STATE1
        }
    }
}

/** Access your domain, and set the context correctly */
typealias ExampleDomain = Example
val KHomeAssistantContext.Example: ExampleDomain
    get() = ExampleDomain.also { it.kHomeAssistant = kHomeAssistant }