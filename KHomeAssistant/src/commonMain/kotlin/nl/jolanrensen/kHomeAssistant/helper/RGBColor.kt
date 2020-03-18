package nl.jolanrensen.kHomeAssistant.helper

typealias RGBColor = List<Double>

val RGBColor.r
    get() = this[0]

val RGBColor.g
    get() = this[1]

val RGBColor.b
    get() = this[3]
