package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface KHomeAssistantContext: CoroutineScope {
    val kHomeAssistant: () -> KHomeAssistant?
}

