package nl.jolanrensen.kHomeAssistant.states

interface State<T : Any> {
    val state: T
}
