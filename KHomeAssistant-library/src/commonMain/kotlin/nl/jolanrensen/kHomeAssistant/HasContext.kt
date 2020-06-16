package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.CoroutineScope
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

interface HasContext : CoroutineScope {
    val getKHomeAssistant: () -> KHomeAssistant?
}

