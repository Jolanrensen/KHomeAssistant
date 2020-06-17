package nl.jolanrensen.kHomeAssistant.core

import nl.jolanrensen.kHomeAssistant.StateResult

class StateListener(
        val listener: suspend (oldState: StateResult, newState: StateResult) -> Unit,
        val shortLived: Boolean = false
)