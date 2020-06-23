package nl.jolanrensen.kHomeAssistant

fun <T> Collection<T>.contentEquals(other: Collection<T>): Boolean =
    containsAll(other) && other.containsAll(this)