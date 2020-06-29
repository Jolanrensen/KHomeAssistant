import com.soywiz.klock.DateTime
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.contentEquals
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.WriteOnlyException
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.toJson

/**
 * This class is an example for a domain implementation.
 * If your domain has no entity you can make it extend `Domain<Nothing>` and make
 * [Entity] throw a [nl.jolanrensen.kHomeAssistant.domains.DomainHasNoEntityException].
 */
class Example(override val kHassInstance: KHomeAssistant) : Domain<Example.Entity> {
    /** The domain name as defined in Home Assistant. */
    override val domainName: String = "example"

    /** Making sure Example acts as a singleton. */
    override fun equals(other: Any?): Boolean = other is Example
    override fun hashCode(): Int = domainName.hashCode()

    /** Service calls on this domain without an entity as argument can be defined like this. */
    suspend fun exampleDomainServiceCall(something: Int): ResultMessage =
        callService(
            serviceName = "some_service",
            data = json {
                "something" to something
            }
        )

    /** Constructor for Entity with the right context */
    override fun Entity(name: String): Entity = Entity(kHassInstance, name)

    /** The state can be of any type, including enums. Just make sure to implement `getStateValue()` and `parseStateValue()` in your Entity class. */
    enum class ExampleState(val stateValue: String) {
        STATE1("state1"), STATE2("state1")
    }

    /**
     * These are the attributes that get parsed from Home Assistant for your entity when calling getAttributes()
     * The names must thus exactly match those of Home Assistant.
     * Options are:
     *   - read only attributes (where the value of the attribute will be read directly from the raw attributes)
     *   - read/write attributes (where the writing part will have to be implemented using a service call)
     *   - write only attributes (where the read part is annotated as deprecated and throws and exception)
     *
     * It's often useful to provide a typed version of a hass attribute. You can do so by providing and implemented
     * getter/setter or function inside the interface and annotating the non-typed version as deprecated.     *
     */
    interface HassAttributes : BaseHassAttributes {
        // read only
        val test_attribute: Int

        // read / write
        @Deprecated(message = "You can use the typed version", replaceWith = ReplaceWith("someOtherAttribute"))
        var some_other_attribute: List<String>

        // write only
        var something: String
            @Deprecated("'something' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'something' is write only")
            set(_) = error("must be overridden")

        // Helpers
        var someOtherAttribute: List<DateTime>
            get() = some_other_attribute.map { DateTime.parse(it).utc }
            set(value) {
                some_other_attribute = value.map { it.toString() }
            }
    }


    /**
     * his class defines your entity, it can be instantiated via YourDomain.Entity(name: String)
     * Define all the service calls on your entity inside here,
     * this also includes listeners for state changes.
     * Don't forget to also implement your [HassAttributes] interface.
     * Instead of inheriting from [nl.jolanrensen.kHomeAssistant.entities.BaseEntity],
     * you can also use [nl.jolanrensen.kHomeAssistant.entities.ToggleEntity] if you know your state
     * is an [nl.jolanrensen.kHomeAssistant.OnOff].
     */
    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<ExampleState, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = Example(kHassInstance)
    ), HassAttributes {

        /** Used to get the values for the hass attributes in the [toString] of this entity. */
        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        /** Also added in [toString]. */
        override val additionalToStringAttributes: Array<Attribute<*>> =
            super.additionalToStringAttributes + ::someHelperThing

        /** Define how to convert your state type into a Home Assistant string state */
        override fun stateToString(state: ExampleState): String = state.stateValue

        /** Define how to convert a Home Assistant string state into your state type */
        override fun stringToState(stateValue: String): ExampleState? = try {
            ExampleState.values().find { it.stateValue == stateValue }
        } catch (e: Exception) {
            null
        }

        // Attributes
        /**
         * All abstract attributes in the [HassAttributes] interface need to be implemented which can be done
         * relatively easily using [attrsDelegate], which will simply take care of it. */

        /** Read only:
         * This attribute will simply be read and parsed from the [rawAttributes] using the delegate. */
        override val test_attribute: Int by attrsDelegate()

        /** Read/write (and non-typed):
         * Same story, however the setter part of the attribute is redirected to [setValue].
         * Unfortunately, we need to define the typed version here as well. */
        @Deprecated(message = "You can use the typed version", replaceWith = ReplaceWith("someOtherAttribute"))
        override var some_other_attribute: List<String> by attrsDelegate()

        /** Write only:
         * We prohibit the use of the getter again and manually set the value using [attrsDelegate], redirecting it again to [setValue].  */
        override var something: String
            @Deprecated("'something' is write only", level = DeprecationLevel.ERROR)
            get() = throw WriteOnlyException("'something' is write only")
            set(value) = attrsDelegate<String>().setValue(this, ::something, value)

        /** You can also add other attributes or helper methods which are not defined in [HassAttributes], however this is discouraged.
         * Sometimes there is no other way (for instance if you need [kHassInstance]), so then you can also make it show up in [toString]
         * by adding it in [additionalToStringAttributes]. */
        val someHelperThing: String
            get() = TODO()

        /** For all `var` attributes using [attrsDelegate] we need to use this method to define their setter. */
        @Suppress("UNCHECKED_CAST")
        override suspend fun <V> setValue(propertyName: String, value: V) {
            when (propertyName) {
                ::some_other_attribute.name -> exampleSetAttrServiceCall(value as List<String>)
                ::something.name -> {
                }
            }
        }

        /** You can provide other helper functions for useful listeners, for instance when an attribute changes to a certain value. */
        fun exampleListener(callback: suspend Entity.() -> Unit): Entity =
            onAttributeChangedTo(::someHelperThing, "test", callback)

        /** A listener for a state change is also possible. */
        fun otherExampleListener(callback: suspend Entity.() -> Unit): Entity =
            onStateChangedToNot(ExampleState.STATE1, callback)

        /** Simple service calls can be defined like this */
        suspend fun exampleEntityServiceCall(): ResultMessage = callService(serviceName = "example")

        /** Simple service call which updates an attribute and waits until it is updated. */
        suspend fun exampleSetAttrServiceCall(value: List<String>, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "asasfsad",
                data = json {
                    "some_other_attribute" to value.toJson()
                }
            )
            if (!async) suspendUntilAttributeChanged(::some_other_attribute, { it.contentEquals(value) })
            return result
        }

        /** Want to add data? sure! */
        suspend fun exampleEntityServiceCallWithData(
            someValue: Int? = null,
            someOtherValue: DateTime? = null
        ) = callService(
            serviceName = "some_service",
            data = json {
                // if someValue isn't null, check and add it to the data
                someValue?.let {
                    if (it !in 0..100) throw IllegalArgumentException("incorrect someValue $it")
                    "some_value" to it
                }

                // same story for the other value
                someOtherValue?.let {

                    // you can also perform checks with the attributes
                    // for instance checking whether some string is supported by the device
                    if (it !in someOtherAttribute) throw IllegalArgumentException("incorrect someOtherValue $it")
                    "some_other_value" to it.toString()
                }
            }
        )


        // Example function that uses the state
        fun isInState1(): Boolean {
            return state == ExampleState.STATE1
        }
    }
}

/** Access your domain, and set the context correctly */
val KHomeAssistant.Example: Example
    get() = Example(this)