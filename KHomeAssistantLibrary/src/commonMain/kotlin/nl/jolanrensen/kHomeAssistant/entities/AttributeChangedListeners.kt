package nl.jolanrensen.kHomeAssistant.entities

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.coroutines.channels.Channel
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
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param callback the block of code to execute when any attribute has changed
 * @return the entity
 */
fun <S : Any, E : BaseEntity<S>> E.onAttributesChanged(
        callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    getKHomeAssistant()!!.stateListeners
            .getOrPut(entityID) { hashSetOf() }
            .add(StateListener({ oldState, newState ->
                if (oldState.attributes != newState.attributes)
                    callback()

            }))
    return this
}

/**
 * Creates a listener executed when the specified attribute of the entity changes.
 * The attribute must have the same name as in Home Assistant (or in [BaseEntity.rawAttributes]).
 * ```
 * myEntity.onAttributeChanged("my_attribute") {
 *     // do something
 * }
 * ```
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the name of the attribute (as in Home Assistant)
 * @param callback the block of code to execute when any attribute has changed
 * @return the entity
 */
fun <S : Any, E : BaseEntity<S>> E.onAttributeChanged(
        attribute: String,
        callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    getKHomeAssistant()!!.stateListeners
            .getOrPut(entityID) { hashSetOf() }
            .add(StateListener({ oldState, newState ->
                if (oldState.attributes[attribute] != newState.attributes[attribute])
                    callback()
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
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChanged(::myAttribute) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (::myAttribute) to the attribute in the entity
 * @param callback the block of code to execute when any attribute has changed
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> E.onAttributeChanged(
        attribute: Attribute<A>,
        callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    getKHomeAssistant()!!.stateListeners
            .getOrPut(entityID) { hashSetOf() }
            .add(StateListener({ oldState, _ ->

                // get the old attribute value by temporarily setting the old attributes as alternative in the delegate
                alternativeAttributes = oldState.attributes
                val oldAttributeValue = attribute.get()
                alternativeAttributes = null

                val newAttributeValue = attribute.get()

                if (oldAttributeValue != newAttributeValue)
                    callback()
            }))
    return this
}

/**
 * Creates a listener executed when the attribute of the specified entity changes.
 * ```
 * myEntity::myAttribute.onChanged(myEntity) {
 *     // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     ::myAttribute.onChanged(this) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the attribute reference (::myAttribute) for which to create the listener
 * @param entity the entity for which to create the listener
 * @param callback the block of code to execute when any attribute has changed
 * @return the attribute
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> Attribute<A>.onChanged(
        entity: E,
        callback: suspend E.() -> Unit
): Attribute<A> {
    entity.onAttributeChanged(this, callback)
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
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> E.onAttributeChangedTo(
        attribute: Attribute<A>,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute) {
    if (attribute.get() == newAttributeValue)
        callback()
}

/**
 * Creates a listener executed when the attribute of the specified entity changes to the specified value.
 * ```
 * myEntity::myAttribute.onChangedTo(myEntity, someValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     ::myAttribute.onChangedTo(this, someValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @param entity the entity for which to create the listener
 * @receiver the reference (::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> Attribute<A>.onChangedTo(
        entity: E,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): Attribute<A> {
    entity.onAttributeChangedTo(this, newAttributeValue, callback)
    return this
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
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must NOT match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> E.onAttributeChangedNotTo(
        attribute: Attribute<A>,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute) {
    if (attribute.get() != newAttributeValue)
        callback()
}

/**
 * Creates a listener executed when the attribute of the specified entity changes anything but the specified value.
 * ```
 * myEntity::myAttribute.onChangedNotTo(myEntity, someOtherValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     ::myAttribute.onChangedNotTo(this, someOtherValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @param entity the entity for which to create the listener
 * @receiver attribute the reference (::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must NOT match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> Attribute<A>.onChangedNotTo(
        entity: E,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): Attribute<A> {
    entity.onAttributeChangedNotTo(this, newAttributeValue, callback)
    return this
}


// --- NonSpecificAttribute ---


/**
 * Creates a listener executed when the specified attribute of the entity changes.
 * ```
 * myEntity.onAttributeChanged(MyDomain.MyEntity::myAttribute) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChanged(MyDomain.MyEntity::myAttribute) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param callback the block of code to execute when any attribute has changed
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> E.onAttributeChanged(
        attribute: NonSpecificAttribute<E, A>,
        callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    getKHomeAssistant()!!.stateListeners
            .getOrPut(entityID) { hashSetOf() }
            .add(StateListener({ oldState, _ ->

                // get the old attribute value by temporarily setting the old attributes as alternative in the delegate
                alternativeAttributes = oldState.attributes
                val oldAttributeValue = attribute.get(this)
                alternativeAttributes = null

                val newAttributeValue = attribute.get(this)

                if (oldAttributeValue != newAttributeValue)
                    callback()
            }))
    return this
}

/**
 * Creates a listener executed when the attribute of the specified entity changes.
 * ```
 * MyDomain.MyEntity::myAttribute.onChanged(myEntity) {
 *     // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     MyDomain.MyEntity::myAttribute.onChanged(this) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the attribute reference (::myAttribute) for which to create the listener
 * @param entity the entity for which to create the listener
 * @param callback the block of code to execute when any attribute has changed
 * @return the attribute
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> NonSpecificAttribute<E, A>.onChanged(
        entity: E,
        callback: suspend E.() -> Unit
): NonSpecificAttribute<E, A> {
    entity.onAttributeChanged(this, callback)
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
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> E.onAttributeChangedTo(
        attribute: NonSpecificAttribute<E, A>,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute = attribute) {
    if (attribute.get(this) == newAttributeValue)
        callback()
}

/**
 * Creates a listener executed when the attribute of the specified entity changes to the specified value.
 * ```
 * MyDomain.MyEntity::myAttribute.onChangedTo(myEntity, someValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     MyDomain.MyEntity::myAttribute.onChangedTo(this, someValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @param entity the entity for which to create the listener
 * @receiver the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> NonSpecificAttribute<E, A>.onChangedTo(
        entity: E,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): NonSpecificAttribute<E, A> {
    entity.onAttributeChangedTo(this, newAttributeValue, callback)
    return this
}

/**
 * Creates a listener executed when the specified attribute of the entity changes anything but the specified value.
 * ```
 * myEntity.onAttributeChangedNotTo(MyDomain.MyEntity::myAttribute, someOtherValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     onAttributeChangedNotTo(MyDomain.MyEntity::myAttribute, someOtherValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @receiver the entity for which to create the listener
 * @param attribute the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must NOT match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> E.onAttributeChangedNotTo(
        attribute: NonSpecificAttribute<E, A>,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): E = onAttributeChanged(attribute) {
    if (attribute.get(this) != newAttributeValue)
        callback()
}

/**
 * Creates a listener executed when the attribute of the specified entity changes anything but the specified value.
 * ```
 * MyDomain.MyEntity::myAttribute.onChangedNotTo(myEntity, someOtherValue) {
 *    // do something
 * }
 * ```
 * or
 * ```
 * myEntity {
 *     MyDomain.MyEntity::myAttribute.onChangedNotTo(this, someOtherValue) {
 *         // do something
 *     }
 * }
 * ```
 * @param A the type of the attribute
 * @param S the state type of the entity [E]
 * @param E the type of the receiver, a [BaseEntity] inheriting entity
 * @param entity the entity for which to create the listener
 * @receiver attribute the reference (MyDomain.MyEntity::myAttribute) to the attribute in the entity
 * @param newAttributeValue the value the attribute must NOT match to execute the [callback]
 * @param callback the block of code to execute when any attribute has changed to [newAttributeValue]
 * @return the entity
 */
fun <A : Any?, S : Any, E : BaseEntity<S>> NonSpecificAttribute<E, A>.onChangedNotTo(
        entity: E,
        newAttributeValue: A,
        callback: suspend E.() -> Unit
): NonSpecificAttribute<E, A> {
    entity.onAttributeChangedNotTo(this, newAttributeValue, callback)
    return this
}

suspend fun <A : Any?, S : Any, E : BaseEntity<S>> E.suspendUntilAttributeChangedTo(
        attribute: Attribute<A>,
        newAttributeValue: A,
        timeout: TimeSpan = 2.seconds
) = suspendUntilAttributeChanged(attribute, { it == newAttributeValue }, timeout)

suspend fun <A : Any?, S : Any, E : BaseEntity<S>> E.suspendUntilAttributeChanged(
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
            getKHomeAssistant()!!.stateListeners[entityID]?.remove(stateListener)
            task?.cancel()
            continueChannel.send(Unit)
        }
    }, true)

    task = runAt(DateTimeTz.nowLocal() + timeout) {
        getKHomeAssistant()!!.stateListeners[entityID]?.remove(stateListener)
        continueChannel.send(Unit)
    }

    getKHomeAssistant()!!.stateListeners
            .getOrPut(entityID) { hashSetOf() }
            .add(stateListener)

    continueChannel.receive()
}