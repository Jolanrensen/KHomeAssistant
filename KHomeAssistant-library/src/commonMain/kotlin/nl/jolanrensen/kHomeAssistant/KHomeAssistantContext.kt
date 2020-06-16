package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.CoroutineScope
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

interface KHomeAssistantContext : CoroutineScope {
    val kHomeAssistant: () -> KHomeAssistant?
}

