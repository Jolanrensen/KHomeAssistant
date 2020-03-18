package nl.jolanrensen.kHomeAssistant.helper

typealias HSColor = List<Double>

val HSColor.h
    get() = this[0]

val HSColor.s
    get() = this[1]