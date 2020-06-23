package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.CoroutineScope
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant

interface HasKHassContext : CoroutineScope {
    val getKHass: () -> KHomeAssistant?
}

