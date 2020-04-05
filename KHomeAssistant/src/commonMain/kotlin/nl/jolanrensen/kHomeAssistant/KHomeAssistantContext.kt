package nl.jolanrensen.kHomeAssistant

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async as realAsync
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.launch as realLaunch

interface KHomeAssistantContext : CoroutineScope {
    val kHomeAssistant: () -> KHomeAssistant?
}

