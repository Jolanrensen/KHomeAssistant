package nl.jolanrensen.kHomeAssistant.entities

class EntityNotInHassException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}