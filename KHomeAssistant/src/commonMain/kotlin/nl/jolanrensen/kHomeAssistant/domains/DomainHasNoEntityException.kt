package nl.jolanrensen.kHomeAssistant.domains

/**
 * Exception that is usually thrown when .Entity() is called
 * on a Domain that has no entity available to it.
 * */
class DomainHasNoEntityException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}