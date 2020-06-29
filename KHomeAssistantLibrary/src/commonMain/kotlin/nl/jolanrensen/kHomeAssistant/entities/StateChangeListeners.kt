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

fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onStateChangedToNot(
    newState: S?,
    callback: suspend E.() -> Unit
): E {
    onStateChanged {
        if (try {
                state
            } catch (e: Exception) {
                null
            } != newState
        )
            callback()
    }
    return this
}

fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onStateChangedTo(
    newState: S?,
    callback: suspend E.() -> Unit
): E {
    onStateChanged {
        if (try {
                state
            } catch (e: Exception) {
                null
            } == newState
        )
            callback()
    }
    return this
}

fun <H : HassAttributes, S : Any, E : Entity<S, H>> E.onStateChanged(
    callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    kHassInstance
        .stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, newState ->
            if (oldState?.state != newState?.state)
                callback()
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