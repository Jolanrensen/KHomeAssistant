import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
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

    suspend fun exampleDomainServiceCall() = callService("")


    // Constructor for Entity with the right context
    override fun Entity(name: String) = Entity(kHomeAssistant, name)


    /** This class defines your entity, it can be instantiated via YourDomain.YourEntity(name: String)
     * Define all the service calls on your entity inside here
     * This also includes listeners for state changes
     */
    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant? = { null },
        override val name: String
    ) : BaseEntity<ExampleState>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = Example
    ) {
        /** These are the attributes that get parsed from Home Assistant for your entity when calling getAttributes()
         * The names must thus exactly match those of Home Assistant. */
        // Attributes
        // read only
        val test_attribute: Int? by attrsDelegate
        val some_other_attribute: List<String>? by attrsDelegate

        // TODO add examples for read/write and write only attributes

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

        /** Simple service calls can be defined like this */
        suspend fun exampleEntityServiceCall() {
            callService("")
        }

        /** Want to add data? sure! */
        suspend fun exampleEntityServiceCallWithData(someValue: Int? = null, someOtherValue: String? = null) {
            val attributes = rawAttributes

            // Don't forget to check the data if you want more redundancy, otherwise just add it to a Map<String, JsonElement> or JsonObject
            val data = hashMapOf<String, JsonElement>().apply {

                // if someValue isn't null, check and add it to the data
                someValue?.let {
                    if (it !in 0..100)
                        throw IllegalArgumentException("incorrect someValue $it")
                    this["some_value"] = JsonPrimitive(it)
                }

                // same story for the other value
                someOtherValue?.let {

                    // you can also perform checks with the attributes
                    // for instance checking whether some string is supported by the device
                    if (it.isEmpty() || it !in some_other_attribute!!)
                        throw IllegalArgumentException("incorrect someOtherValue $it")
                    this["some_other_value"] = JsonPrimitive(it)
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
typealias ExampleDomain = Example

val KHomeAssistantContext.Example: ExampleDomain
    get() = ExampleDomain.also { it.kHomeAssistant = kHomeAssistant }