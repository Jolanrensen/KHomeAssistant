package nl.jolanrensen.kHomeAssistant.entities

import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.coroutines.channels.Channel
import nl.jolanrensen.kHomeAssistant.Task
import nl.jolanrensen.kHomeAssistant.core.StateListener
import nl.jolanrensen.kHomeAssistant.runAt

fun <S : Any, E : BaseEntity<S>> E.onStateChangedToNot(
    newState: S,
    callback: suspend E.() -> Unit
): E {
    onStateChanged {
        if (state != newState)
            callback()
    }
    return this
}

fun <S : Any, E : BaseEntity<S>> E.onStateChangedTo(
    newState: S,
    callback: suspend E.() -> Unit
): E {
    onStateChanged {
        if (state == newState)
            callback()
    }
    return this
}

fun <S : Any, E : BaseEntity<S>> E.onStateChanged(
    callback: suspend E.() -> Unit
): E {
    checkEntityExists()
    stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(StateListener({ oldState, newState ->
            if (oldState.state != newState.state)
                callback()
        }))
    return this
}

suspend fun <S : Any, E : BaseEntity<S>> E.suspendUntilStateChangedTo(
    newState: S,
    timeout: TimeSpan = 1.seconds
) = suspendUntilStateChanged({ it == newState }, timeout)

suspend fun <S : Any, E : BaseEntity<S>> E.suspendUntilStateChanged(
    condition: (S) -> Boolean,
    timeout: TimeSpan = 1.seconds
) {
    checkEntityExists()
    if (condition(state)) return

    val continueChannel = Channel<Unit>()

    var stateListener: StateListener? = null
    var task: Task? = null

    stateListener = StateListener({ _, _ ->
        if (condition(state)) {
            stateListeners[entityID]?.remove(stateListener)
            task?.cancel()
            continueChannel.send(Unit)
        }
    }, true)

    task = runAt(DateTimeTz.nowLocal() + timeout) {
        stateListeners[entityID]?.remove(stateListener)
        continueChannel.send(Unit)
    }

    stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add(stateListener)

    continueChannel.receive()
}