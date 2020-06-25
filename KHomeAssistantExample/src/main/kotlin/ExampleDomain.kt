import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage


// The state can be any type, including enums. Just make sure to implement the getStateValue() and parseStateValue() in your Entity class.
enum class ExampleState(val stateValue: String) {
    STATE1("state1"), STATE2("state1")
}

class Example(override val kHassInstance: KHomeAssistant) : Domain<Example.Entity> {
    override val domainName: String = "example"

    /** Making sure Example acts as a singleton. */
    override fun equals(other: Any?) = other is Example
    override fun hashCode(): Int = domainName.hashCode()

    suspend fun exampleDomainServiceCall() = callService("")


    // Constructor for Entity with the right context
    override fun Entity(name: String) = Entity(kHassInstance, name)

    /** These are the attributes that get parsed from Home Assistant for your entity when calling getAttributes()
     * The names must thus exactly match those of Home Assistant. */
    interface HassAttributes : BaseHassAttributes {
        // read only
        val test_attribute: Int
        val some_other_attribute: List<String>
    }


    /** This class defines your entity, it can be instantiated via YourDomain.YourEntity(name: String)
     * Define all the service calls on your entity inside here
     * This also includes listeners for state changes
     */
    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<ExampleState, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Example(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        // TODO add examples for read/write and write only attributes

        /** Define how to convert your state type into a Home Assistant string state */
        override fun stateToString(state: ExampleState): String = state.stateValue

        /** Define how to convert a Home Assistant string state into your state type */
        override fun stringToState(stateValue: String): ExampleState? = try {
            ExampleState.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        // Attributes
        override val test_attribute: Int by attrsDelegate()
        override val some_other_attribute: List<String> by attrsDelegate()

        fun exampleListener(callback: Entity.() -> Unit) {

        }

        /** Simple service calls can be defined like this */
        suspend fun exampleEntityServiceCall(): ResultMessage = callService("")


        /** Want to add data? sure! */
        suspend fun exampleEntityServiceCallWithData(someValue: Int? = null, someOtherValue: String? = null) {
            val attributes = rawAttributes

            // Don't forget to check the data if you want more redundancy, otherwise just add it to a Map<String, JsonElement> or JsonObject
            val data = json {

                // if someValue isn't null, check and add it to the data
                someValue?.let {
                    if (it !in 0..100)
                        throw IllegalArgumentException("incorrect someValue $it")
                    "some_value" to it
                }

                // same story for the other value
                someOtherValue?.let {

                    // you can also perform checks with the attributes
                    // for instance checking whether some string is supported by the device
                    if (it.isEmpty() || it !in some_other_attribute)
                        throw IllegalArgumentException("incorrect someOtherValue $it")
                    "some_other_value" to it
                }
            }
            callService(
                serviceName = "some_service",
                data = data
            )
        }

        /** Want to add a whole lot of options and you want to type less? Sure, let's just use Serialization again */
        @Serializable
        inner class callSomeService(
            val val1: Int? = null,
            val val2: Int? = null,
            val val3: Int? = null,
            val val4: Int? = null
        ) {
            init {
                runBlocking {
                    val1?.let {
                        // Check
                    }
                    // .. check all values

                    // Serialize and call the service
                    val data = Json(
                        JsonConfiguration.Stable.copy(encodeDefaults = false)
                    ).toJson(serializer(), this@callSomeService)

                    callService(
                        serviceName = "some_service_thing",
                        data = data.jsonObject
                    )
                }
            }
        }

        // Example function that uses the state
        suspend fun isInState1(): Boolean {
            return state == ExampleState.STATE1
        }
    }
}

/** Access your domain, and set the context correctly */
val KHomeAssistant.Example: Example
    get() = Example(this)