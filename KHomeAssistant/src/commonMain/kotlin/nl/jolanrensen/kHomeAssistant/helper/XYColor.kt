package nl.jolanrensen.kHomeAssistant.helper

typealias XYColor = List<Double>

val XYColor.x
    get() = this[0]

val XYColor.y
    get() = this[1]