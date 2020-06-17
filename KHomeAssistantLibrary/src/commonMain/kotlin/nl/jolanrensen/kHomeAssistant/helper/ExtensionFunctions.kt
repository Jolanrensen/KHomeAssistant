package nl.jolanrensen.kHomeAssistant.helper

fun <T> Collection<T>.contentEquals(other: Collection<T>): Boolean =
    containsAll(other) && other.containsAll(this)