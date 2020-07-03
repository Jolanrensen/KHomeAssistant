package nl.jolanrensen.kHomeAssistant.entities

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.coroutines.channels.Channel
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.Task
import nl.jolanrensen.kHomeAssistant.core.StateListener
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.runAt

fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onStateChangedNotTo(
    newState: S?,
    callback: suspend E.() -> Unit
): E {
    onStateChanged({ _, new ->
        if (new != newState)
            callback()
    })
    return this
}

fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onStateChangedTo(
    newState: S?,
    callback: suspend E.() -> Unit
): E {
    onStateChanged({ _, new ->
        if (new == newState)
            callback()
    })
    return this
}

/**
 * TODO
 * @param callbackWith the block of code with values to execute when the state has changed. Don't provide [callbackWithout] in conjunction with this.
 * @param callbackWithout the block of code to execute when the state has changed. Don't provide [callbackWith] in conjunction with this.
 * example:
 * ```
 * myEntity.onStateChanged {
 *     // do something
 * }
 * ```
 * or
 * ```
 * myEntity.onStateChanged({ oldState, newState ->
 *     // do something with oldState for example
 * })
 *
 */
fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onStateChanged(
    callbackWith: (suspend E.(oldState: S?, newState: S?) -> Unit)? = null,
    callbackWithout: (suspend E.() -> Unit)? = null
): E {
    if (callbackWith != null && callbackWithout != null || callbackWith == null && callbackWithout == null)
        throw IllegalArgumentException("Exactly one of callbackWith and callbackWithout need to be provided")

    checkEntityExists()
    kHassInstance
        .stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, newState ->
            if (oldState?.state != newState?.state) {
                callbackWithout?.invoke(this)
                callbackWith?.invoke(
                    this,
                    oldState?.state?.let { stringToState(it) },
                    newState?.state?.let { stringToState(it) }
                )
            }
        }))
    return this
}




fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onEntityRemoved(
    callback: suspend E.() -> Unit
): E = onStateChangedTo(null, callback)

suspend fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.suspendUntilStateChangedTo(
    newState: S?,
    timeout: TimeSpan = 1.seconds
) = suspendUntilStateChanged({ it == newState }, timeout)

suspend fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.suspendUntilStateChanged(
    condition: (S?) -> Boolean,
    timeout: TimeSpan = 1.seconds
) {
    checkEntityExists()
    if (condition(
            try {
                state
            } catch (e: Exception) {
                null
            }
        )
    ) return

    val continueChannel = Channel<Unit>()

    var stateListener: StateListener? = null
    var task: Task? = null

    stateListener = StateListener({ _, _ ->
        if (condition(
                try {
                    state
                } catch (e: Exception) {
                    null
                }
            )
        ) {
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

suspend fun KHomeAssistant.suspendUntilEntityExists(
    domain: Domain<*>,
    entityName: String,
    timeout: TimeSpan = 1.seconds
) {
    val entityID = "${domain.domainName}.$entityName"
    if (entityID in instance.entityIds) return

    val continueChannel = Channel<Unit>()

    var stateListener: StateListener? = null
    var task: Task? = null
    stateListener = StateListener({ _, _ ->
        stateListeners[entityID]?.remove(stateListener)
        task?.cancel()
        continueChannel.send(Unit)
    }, true)

    task = runAt(DateTimeTz.nowLocal() + timeout) {
        stateListeners[entityID]?.remove(stateListener)
        continueChannel.send(Unit)
    }

    if (entityID in instance.entityIds) return

    stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(stateListener)

    continueChannel.receive()
}