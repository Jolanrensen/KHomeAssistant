package nl.jolanrensen.kHomeAssistant.helper

typealias RGBColor = List<Int>

val RGBColor.r
    get() = this[0]

val RGBColor.g
    get() = this[1]

val RGBColor.b
    get() = this[3]

fun RGBColor(r: Int, g: Int, b: Int): RGBColor = listOf(r, g, b)