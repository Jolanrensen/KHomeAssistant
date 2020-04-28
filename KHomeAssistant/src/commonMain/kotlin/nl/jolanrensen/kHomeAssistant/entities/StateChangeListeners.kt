package nl.jolanrensen.kHomeAssistant.entities

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
    kHomeAssistant()!!.stateListeners
        .getOrPut(entityID) { hashSetOf() }
        .add { oldState, newState ->
            if (oldState.state != newState.state)
                callback()
        }
    return this
}