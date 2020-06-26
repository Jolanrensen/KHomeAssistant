package nl.jolanrensen.kHomeAssistant.entities

import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.SceneEntityState
import nl.jolanrensen.kHomeAssistant.cast
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.Scene
import nl.jolanrensen.kHomeAssistant.messages.Context
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.toJson
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

typealias DefaultEntity = Entity<String, HassAttributes>

/** Type returned when using myEntity::myAttribute */
typealias Attribute<A> = KProperty0<A>

/** Type returned when using SomeDomain.Entity::myAttribute */
typealias NonSpecificAttribute<E, A> = KProperty1<E, A>

/** Alias for [Entity]. */
typealias BaseEntity<StateType, AttrsType> = Entity<StateType, AttrsType>

open class Entity<StateType : Any, AttrsType : HassAttributes>(
    open val kHassInstance: KHomeAssistant,
    open val name: String,
    open val domain: Domain<*>
) : HassAttributes {

    init {
        this.checkEntityExists()
    }

//    private var dont

    private var entityExists = false

    @Suppress("UNNECESSARY_SAFE_CALL")
    open fun checkEntityExists() {
        if (entityExists || !(kHassInstance?.loadedInitialStates == true)) return
        kHassInstance.launch {
            if (entityID !in kHassInstance.entityIds)
                throw EntityNotInHassException("The entity_id \"$entityID\" does not exist in your Home Assistant instance.")
            entityExists = true
        }
    }

    /** All attributes as described in Home Assistant. */
    open val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

    /** Given a string stateValue, this method should return the correct StateType */
    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalStdlibApi::class)
    open fun stringToState(stateValue: String): StateType? = try {
        stateValue as StateType
    } catch (e: Exception) {
        throw Exception("Did you forget to override parseStateValue() for ${domain.domainName}?")
    }

    /** This method returns the state for this entity in the original String format */
    open fun stateToString(state: StateType): String? = try {
        state as String
    } catch (e: Exception) {
        throw Exception("Did you forget to override getStateValue() for ${domain.domainName}?")
    }

    /** State as String as defined in Home Assistant. */
    val stateAsString: String // TODO
        get() = stateToString(state)!!

    /** State of the entity. */
    open var state: StateType
        get() = kHassInstance.getState(this)
        //        @Deprecated(
//            level = DeprecationLevel.WARNING,
//            message = "Uses Scene.apply, so it isn't guaranteed to work. Use functions inside a domain to be more certain."
//        )
        set(value) {
            println("Attempting to set state of $entityID using Scene.apply")
            runBlocking {
                kHassInstance.Scene.apply(SceneEntityState(this@Entity, value))
                suspendUntilStateChangedTo(value)
            }
        }
    // set() TODO?

//    suspend fun setState(s: StateType): Unit = TODO()

    /** Get the raw attributes from Home Assistant in json format.
     * Raw attributes can also directly be obtained using yourEntity["raw_attribute"] */
    val rawAttributes: JsonObject
        get() = kHassInstance.getRawAttributes(this)

    /** Helper function to get raw attributes in json format using yourEntity["attribute"]
     * @see [Entity.rawAttributes]
     *
     * For example:
     * ```
     * val attrValue: String = myEntity["my_attribute"]!!.content
     * ``` */
    operator fun get(name: String): JsonElement? = rawAttributes[name]

    /** ONLY USE TEMPORARILY */
    internal var alternativeAttributes: JsonObject? = null

    /** Makes delegated attributes possible for entities.
     * It tries to take the name of the variable as attribute name and cast the result
     * to the type of the variable.
     * @throws IllegalArgumentException when the attribute can't be found
     * */
    fun <V : Any?> attrsDelegate(): AttributesDelegate<V> = object : AttributesDelegate<V> {
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): V =
            (alternativeAttributes ?: rawAttributes)[property.name]
                .let { it ?: throw IllegalArgumentException("Couldn't find attribute ${property.name}") }
                .cast(property.returnType)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) = setValue(property, value)
    }

    /** Makes delegated attributes possible for entities.
     * It tries to take the name of the variable as attribute name and cast the result
     * to the type of the variable.
     * @param default is returned when attribute cannot be found or cast
     * */
    fun <V : Any?> attrsDelegate(default: V): AttributesDelegate<V> = object : AttributesDelegate<V> {
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): V =
            when (val attr = (alternativeAttributes ?: rawAttributes)[property.name]) {
                null -> default
                else -> try {
                    attr.cast<V>(property.returnType)
                } catch (e: Exception) {
                    default
                }
            }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) = setValue(property, value)
    }

    /**
     * Can be overridden
     * */
    open fun <V : Any?> setValue(property: KProperty<*>, value: V) {
        TODO()
    }

    // Default attributes

    override val friendly_name: String by attrsDelegate()
    override val hidden: Boolean by attrsDelegate(false)
    override val entity_picture: String by attrsDelegate()
    override val icon: String by attrsDelegate()
    override val assumed_state: Boolean by attrsDelegate(true)
    override val device_class: String by attrsDelegate() // TODO maybe move to binary sensor, sensor, cover and media player only
    override val unit_of_measurement: String? by attrsDelegate(null)
    override val initial_state: String by attrsDelegate()
    override val id: String by attrsDelegate()


    suspend fun getLastChanged(): String = TODO("last_changed uit State")
    suspend fun getLastUpdated(): String = TODO("last_updated uit State")
    suspend fun getContext(): Context = TODO("context uit State")

    /** Request the update of an entity, rather than waiting for the next scheduled update, for example Google travel time sensor, a template sensor, or a light */
    suspend inline fun updateEntity() = callService("update_entity")

    /** Call a service with this entity using a different serviceDomain */
    suspend fun callService(
        serviceDomain: Domain<*>,
        serviceName: String,
        data: JsonObject = json { },
        doEntityCheck: Boolean = true
    ): ResultMessage {
        if (doEntityCheck) checkEntityExists()
        return kHassInstance.callService(
            entity = this,
            serviceDomain = serviceDomain,
            serviceName = serviceName,
            data = data
        )
    }

    suspend fun callService(
        serviceName: String,
        data: JsonObject = json { },
        doEntityCheck: Boolean = true
    ): ResultMessage {
        if (doEntityCheck) checkEntityExists()
        return kHassInstance.callService(
            entity = this,
            serviceName = serviceName,
            data = data
        )
    }

    /** Shortcut to name of domain for this entity. */
    val domainName: String
        get() = domain.domainName

    /** Entity ID as defined in Home Assistant.
     * Like "light.light_name". */
    val entityID: String
        get() = "$domainName.$name"

    open val additionalToStringAttributes: Array<Attribute<*>> = arrayOf(::state, ::rawAttributes, ::entityID)

    /** Get printable String for this entity.
     * Also prints [additionalToStringAttributes]. */
    override fun toString(): String = "${domain::class.simpleName} ($domainName) Entity($name) {${
    (hassAttributes + additionalToStringAttributes)
        .filter {
            try {
                it.get()
            } catch (e: Exception) {
                null
            } != null
        }
        .map { "\n    ${it.name} = ${it.get()}" }
        .toString()
        .run { subSequence(1, length - 1) }
    }\n}"

    /** An entity is defined by its domain and name. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Entity<*, *>

        if (name != other.name) return false
        if (domain != other.domain) return false

        return true
    }

    /** An entity is defined by its domain and name. */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + domain.hashCode()
        return result
    }

}

interface AttributesDelegate<V : Any?> {
    /** Makes delegated attributes possible for entities. */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): V

    /** Makes delegated attributes possible for entities. Uses Scene.apply to try and set the value of the attribute. */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V)
//        if (onState == null)
//            throw IllegalArgumentException("onState is needed to attempt to use Scene.apply to set the value of an attribute.")
//
//        val jsonValue = when (value) {
//            is Number, is Number? -> JsonPrimitive(value as Number?)
//            is Boolean, is Boolean? -> JsonPrimitive(value as Boolean?)
//            is String, is String? -> JsonPrimitive(value as String?)
//
//            else -> throw IllegalArgumentException("value must be a number, string or boolean, else it can't be turned into a JsonPrimitive")
//        }
//        runBlocking {
//            entity.Scene.apply(SceneEntityState(
//                entity = entity,
//                state = onState,
//                attributes = json { property.name to jsonValue }
//            ))
//        }

}

/**
 * Shorthand for apply, allows for DSL-like behavior on entities.
 *
 * For example:
 * ```
 * myLight {
 *     color = Colors.BLUE
 *     white_value = 125
 * }
 * ```
 * */
inline operator fun <A : HassAttributes, S : Any, E : Entity<S, A>> E.invoke(callback: E.() -> Unit): E =
    apply(callback)

/**
 * Shorthand for apply for each, allows for DSL-like behavior on collections of entities.
 *
 * For example:
 * ```
 * myListOfLights {
 *     color = Colors.BLUE
 *     white_value = 125
 * }
 * ```
 * */
inline operator fun <A : HassAttributes, S : Any, E : Entity<S, A>> Iterable<E>.invoke(callback: E.() -> Unit): Iterable<E> =
    apply { forEach(callback) }

/** */
fun <T : Any?> KProperty<T>.toKProperty0(instance: Any?): KProperty0<T> =
    object : KProperty0<T>, KProperty<T> by this {
        override fun get(): T = call(instance)
        override fun getDelegate(): Any? = error("")
        override fun invoke(): T = get()
        override val getter: KProperty0.Getter<T> =
            object : KProperty0.Getter<T>, KProperty.Getter<T> by this@toKProperty0.getter {
                override fun invoke(): T = get()
            }
    }

/**  */
inline fun <reified A : HassAttributes> A.getHassAttributes(): Array<Attribute<*>> =
    A::class.members
        .filter { it.isAbstract }
        .filterIsInstance<KProperty<*>>()
        .map { it.toKProperty0(this@getHassAttributes) }
        .toTypedArray()

inline fun <reified A : HassAttributes> A.getHassAttributesHelpers(): Array<Attribute<*>> =
    A::class.members
        .filter { !it.isAbstract }
        .filterIsInstance<KProperty<*>>()
        .map { it.toKProperty0(this@getHassAttributesHelpers) }
        .toTypedArray()


inline fun <reified A : HassAttributes> A.convertHassAttrsToJson(): JsonObject =
    JsonObject(
        getHassAttributes()
            .map { it.name to it.get().toJson() }
            .toMap()
    )