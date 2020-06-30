package nl.jolanrensen.kHomeAssistant

import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

fun <T> Collection<T>.contentEquals(other: Collection<T>): Boolean =
    containsAll(other) && other.containsAll(this)

/** */
fun <T : Any?> KProperty<T>.toKProperty0(instance: Any?): KProperty0<T> =
    object : KProperty0<T>, KProperty<T> by this {
        override fun get(): T = call(instance)
        override fun getDelegate(): Any? = error("")
        override fun invoke(): T = get()
        override val getter: KProperty0.Getter<T> =
            object : KProperty0.Getter<T>, KProperty.Getter<T> by this@toKProperty0.getter {
                override fun invoke(): T = get()
            }
    }