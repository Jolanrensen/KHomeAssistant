package nl.jolanrensen.kHomeAssistant.core

/**
 * Mode of operation of [KHomeAssistantInstance].
 */
enum class Mode {
    /** After the initializations of the supplied automations are finished, the method terminates. Listeners etc. are ignored. */
    JUST_INITIALIZE,

    /** After the initializations of the supplied automations are finished, the method keeps running. Events can keep coming in. */
    KEEP_RUNNING,

    /** Depending on whether there exist listeners in the supplied automations, the method keeps running or not. (Default) */
    AUTOMATIC
}