package nl.jolanrensen.kHomeAssistant.entities

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.JsonElement
import nl.jolanrensen.kHomeAssistant.Task
import nl.jolanrensen.kHomeAssistant.core.StateListener
import nl.jolanrensen.kHomeAssistant.runAt

/**
 * Creates a listener executed when any attribute of the entity changes.
 * ```
 * myEntity.onAttributesChanged {
 *     // do something
 * }
 * ```
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param callback the block of code to execute when any attribute has changed
 * @return the entity
 */
fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onAttributesChanged(
    callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    kHassInstance.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, newState ->
            if (oldState?.attributes != newState?.attributes)
                callback()

        }))
    return this
}

/**
 * Creates a listener executed when the specified attribute of the entity changes.
 * The attribute must have the same name as in Home Assistant (or in [Entity.rawAttributes]).
 *
 * Example:
 * ```
 * myEntity.onAttributeChanged("my_attribute") {
 *     // do something
 * }
 * ```
 * or
 * ```
 * myEntity.onAttributeChanged("my_attribute", { oldValue, newValue ->
 *     // do something with oldValue for instance
 * })
 * ```
 *
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the name of the attribute (as in Home Assistant)
 * @param callbackWith the block of code with values to execute when any attribute has changed. Don't provide [callbackWithout] in conjunction with this.
 * @param callbackWithout the block of code to execute when any attribute has changed. Don't provide [callbackWith] in conjunction with this.
 * @return the entity
 */
fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onAttributeChanged(
    attribute: String,
    callbackWith: (suspend E.(oldValue: JsonElement?, newValue: JsonElement?) -> Unit)? = null,
    callbackWithout: (suspend E.() -> Unit)? = null
): E {
    if (callbackWith != null && callbackWithout != null || callbackWith == null && callbackWithout == null)
        throw IllegalArgumentException("Exactly one of callbackWith and callbackWithout need to be provided")

    checkEntityExists()
    kHassInstance.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, newState ->
            val old = oldState?.attributes?.get(attribute)
            val new = newState?.attributes?.get(attribute)
            if (old != new) {
                callbackWithout?.invoke(this)
                callbackWith?.invoke(this, old, new)
            }
        }))
    return this
}


// --- Attribute ---


/**
 * Creates a listener executed when the specified attribute of the entity changes.
 * ```
 * myEntity.onAttributeChanged(myEntity::myAttribute) {
 *    // do something
 * }
 * myEntity.onAttributeChanged(myEntity::myAttribute, { oldValue, newValue ->
 *    // do something with oldValue for example
 * })
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChanged(::myAttribute) {
 *         // do something
 *     }
 *     onAttributeChanged(::myAttribute, { oldValue, newValue ->
 *         // do something with oldValue for example
 *     })
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (::myAttribute) to the attribute in the entity
 * @param callbackWith the block of code with values to execute when any attribute has changed. Don't provide [callbackWithout] in conjunction with this.
 * @param callbackWithout the block of code to execute when any attribute has changed. Don't provide [callbackWith] in conjunction with this.
 * @return the entity
 */
fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.onAttributeChanged(
    attribute: Attribute<A>,
    callbackWith: (suspend E.(oldValue: A?, newValue: A?) -> Unit)? = null,
    callbackWithout: (suspend E.() -> Unit)? = null
): E {
    if (callbackWith != null && callbackWithout != null || callbackWith == null && callbackWithout == null)
        throw IllegalArgumentException("Exactly one of callbackWith and callbackWithout need to be provided")

    checkEntityExists()
    kHassInstance.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, _ ->

            // get the old attribute value by temporarily setting the old attributes as alternative in the delegate
            alternativeAttributes = oldState?.attributes
            val oldAttributeValue: A? = try {
                attribute.get()
            } catch (e: Exception) {
                null
            }
            alternativeAttributes = null

            val newAttributeValue: A? = try {
                attribute.get()
            } catch (e: Exception) {
                null
            }

            if (oldAttributeValue != newAttributeValue) {
                callbackWithout?.invoke(this)
                callbackWith?.invoke(this, oldAttributeValue, newAttributeValue)
            }
        }))
    return this
}


/**
 * Creates a listener executed when the specified attribute of the entity changes to the specified value.
 * ```
 * myEntity.onAttributeChangedTo(myEntity::myAttribute, someValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChangedTo(::myAttribute, someValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.onAttributeChangedTo(
    attribute: Attribute<A>,
    newAttributeValue: A,
    callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute) {
    if (attribute.get() == newAttributeValue)
        callback()
}


/**
 * Creates a listener executed when the specified attribute of the entity changes anything but the specified value.
 * ```
 * myEntity.onAttributeChangedNotTo(myEntity::myAttribute, someOtherValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChangedNotTo(::myAttribute, someOtherValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must NOT match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.onAttributeChangedNotTo(
    attribute: Attribute<A>,
    newAttributeValue: A,
    callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute) {
    if (attribute.get() != newAttributeValue)
        callback()
}


// --- NonSpecificAttribute ---


/**
 * Creates a listener executed when the specified attribute of the entity changes.
 * ```
 * myEntity.onAttributeChanged(MyDomain.MyEntity::myAttribute) {
 *    // do something
 * }
 * myEntity.onAttributeChanged(MyDomain.MyEntity::myAttribute, { oldValue, newValue ->
 *    // do something with oldValue for example
 * })
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChanged(MyDomain.MyEntity::myAttribute) {
 *         // do something
 *     }
 *     onAttributeChanged(MyDomain.MyEntity::myAttribute, { oldValue, newValue ->
 *         // do something with oldValue for example
 *     })
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param callbackWith the block of code with values to execute when any attribute has changed. Don't provide [callbackWithout] in conjunction with this.
 * @param callbackWithout the block of code to execute when any attribute has changed. Don't provide [callbackWith] in conjunction with this.
 * @return the entity
 */
fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.onAttributeChanged(
    attribute: NonSpecificAttribute<E, A>,
    callbackWith: (suspend E.(oldValue: A?, newValue: A?) -> Unit)? = null,
    callbackWithout: (suspend E.() -> Unit)? = null
): E {
    if (callbackWith != null && callbackWithout != null || callbackWith == null && callbackWithout == null)
        throw IllegalArgumentException("Exactly one of callbackWith and callbackWithout need to be provided")

    checkEntityExists()
    kHassInstance.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, _ ->

            // get the old attribute value by temporarily setting the old attributes as alternative in the delegate
            alternativeAttributes = oldState?.attributes
            val oldAttributeValue: A? = try {
                attribute.get(this)
            } catch (e: Exception) {
                null
            }
            alternativeAttributes = null

            val newAttributeValue: A? = try {
                attribute.get(this)
            } catch (e: Exception) {
                null
            }

            if (oldAttributeValue != newAttributeValue) {
                callbackWithout?.invoke(this)
                callbackWith?.invoke(this, oldAttributeValue, newAttributeValue)
            }
        }))
    return this
}


/**
 * Creates a listener executed when the specified attribute of the entity changes to the specified value.
 * ```
 * myEntity.onAttributeChangedTo(MyDomain.MyEntity::myAttribute, someValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChangedTo(MyDomain.MyEntity::myAttribute, someValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [Entity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.onAttributeChangedTo(
    attribute: NonSpecificAttribute<E, A>,
    newAttributeValue: A,
    callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute = attribute) {
    if (attribute.get(this) == newAttributeValue)
        callback()
}


suspend fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.suspendUntilAttributeChangedTo(
    attribute: Attribute<A>,
    newAttributeValue: A,
    timeout: TimeSpan = 2.seconds
) = suspendUntilAttributeChanged(attribute, { it == newAttributeValue }, timeout)

suspend fun <H : HassAttributes, A : Any?, S : Any, E : Entity<S, H>> E.suspendUntilAttributeChanged(
    attribute: Attribute<A>,
    condition: (A) -> Boolean,
    timeout: TimeSpan = 2.seconds
) {
    checkEntityExists()
    if (condition(attribute.get())) return

    val continueChannel = Channel<Unit>()

    var stateListener: StateListener? = null
    var task: Task? = null

    stateListener = StateListener({ _, _ ->
        if (condition(attribute.get())) {
            kHassInstance.stateListeners[entityID]?.remove(stateListener)
            task?.cancel()
            continueChannel.send(Unit)
        }
    }, true)

    task = kHassInstance.runAt(DateTimeTz.nowLocal() + timeout) {
        kHassInstance.stateListeners[entityID]?.remove(stateListener)
        continueChannel.send(Unit)
    }

    kHassInstance.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(stateListener)

    continueChannel.receive()
}