package nl.jolanrensen.kHomeAssistant.helper

typealias HSColor = List<Float>

val HSColor.h
    get() = this[0]

val HSColor.s
    get() = this[1]

fun HSColor(h: Float, s: Float): HSColor = listOf(h, s)

//fun HSColor.toString() = "HSColor(h = $h, s = $s)"