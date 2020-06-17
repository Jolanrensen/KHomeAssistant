package nl.jolanrensen.kHomeAssistant.helper

typealias XYColor = List<Float>

val XYColor.x
    get() = this[0]

val XYColor.y
    get() = this[1]

fun XYColor(x: Float, y: Float): XYColor = listOf(x, y)