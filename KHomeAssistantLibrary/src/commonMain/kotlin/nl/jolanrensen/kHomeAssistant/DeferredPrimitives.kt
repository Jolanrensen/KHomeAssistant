//package nl.jolanrensen.kHomeAssistant
//
//import com.soywiz.korio.async.async
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Deferred
//import kotlin.coroutines.CoroutineContext
//import kotlin.reflect.typeOf
//
//interface HasCoroutinesScope {
//    val scope: CoroutineScope
//}
//
///** Enables easy creation of deferred primitives using + or -.
// * The scope must implement [HasCoroutinesScope]. */
//interface DeferredPrimitivesScope : HasCoroutinesScope {
//
////    val Int.percent: Percent
////        get() = deferred(this)
//
//    val Int.deferred
//        get() = deferred(this)
//
//    fun deferred(value: Int): DeferredInt = DeferredInt(scope.async { value })
//
//}
//
////typealias Percent = DeferredInt
//
//sealed class DeferredPrimitive<T>(deferred: Deferred<T>) : Deferred<T> by deferred, CoroutineScope {
//    override val coroutineContext: CoroutineContext = deferred
//
//    companion object {
////        operator fun invoke() TODO?
//    }
//}
//
//sealed class DeferredNumber<T : Number>(deferred: Deferred<T>) : DeferredPrimitive<T>(deferred) {
//
//    abstract operator fun plus(other: DeferredByte): DeferredNumber<*>
//    abstract operator fun plus(other: DeferredShort): DeferredNumber<*>
//    abstract operator fun plus(other: DeferredInt): DeferredNumber<*>
//    abstract operator fun plus(other: DeferredLong): DeferredNumber<*>
//    abstract operator fun plus(other: DeferredFloat): DeferredNumber<*>
//    abstract operator fun plus(other: DeferredDouble): DeferredNumber<*>
//
//    abstract operator fun plus(other: Byte): DeferredNumber<*>
//    abstract operator fun plus(other: Short): DeferredNumber<*>
//    abstract operator fun plus(other: Int): DeferredNumber<*>
//    abstract operator fun plus(other: Long): DeferredNumber<*>
//    abstract operator fun plus(other: Float): DeferredNumber<*>
//    abstract operator fun plus(other: Double): DeferredNumber<*>
//
//
//    abstract operator fun <S : Number> minus(other: DeferredNumber<S>): DeferredNumber<T>
//    abstract operator fun <S : Number> minus(other: S): DeferredNumber<T>
//
//    abstract operator fun <S : Number> div(other: DeferredNumber<S>): DeferredNumber<T>
//    abstract operator fun <S : Number> div(other: S): DeferredNumber<T>
//
//    abstract operator fun <S : Number> times(other: DeferredNumber<S>): DeferredNumber<T>
//    abstract operator fun <S : Number> times(other: S): DeferredNumber<T>
//
//    abstract operator fun <S : Number> rem(other: DeferredNumber<S>): DeferredNumber<T>
//    abstract operator fun <S : Number> rem(other: S): DeferredNumber<T>
//
//    abstract operator fun inc(): DeferredNumber<T>
//    abstract operator fun dec(): DeferredNumber<T>
//
//    abstract suspend operator fun compareTo(other: Number): Int
//
//    abstract suspend operator fun <T : DeferredNumber<*>> compareTo(other: T): Int
//
//
//}
//
//class DeferredInt(deferred: Deferred<Int>) : DeferredNumber<Int>(deferred) {
//
//    override operator fun plus(other: DeferredByte): DeferredInt =
//        async { await() + other.await() }.asDeferredPrimitive()
//
//    override operator fun plus(other: DeferredShort): DeferredInt = DeferredInt(async { await() + other.await() })
//    override operator fun plus(other: DeferredInt): DeferredInt = DeferredInt(async { await() + other.await() })
//    override operator fun plus(other: DeferredLong): DeferredLong = DeferredLong(async { await() + other.await() })
//    override operator fun plus(other: DeferredFloat): DeferredFloat = DeferredFloat(async { await() + other.await() })
//    override operator fun plus(other: DeferredDouble): DeferredDouble =
//        DeferredDouble(async { await() + other.await() })
//
//
//    override operator fun plus(other: Byte): DeferredInt = DeferredInt(async { await() + other })
//    override operator fun plus(other: Short): DeferredInt = DeferredInt(async { await() + other })
//    override operator fun plus(other: Int): DeferredInt = DeferredInt(async { await() + other })
//    override operator fun plus(other: Long): DeferredLong = DeferredLong(async { await() + other })
//    override operator fun plus(other: Float): DeferredFloat = DeferredFloat(async { await() + other })
//    override operator fun plus(other: Double): DeferredDouble = DeferredDouble(async { await() + other })
//
//    override fun <S : Number> minus(other: DeferredNumber<S>): DeferredInt =
//        async { await() - other.await().toInt() } as DeferredInt
//
//    override fun <S : Number> minus(other: S): DeferredInt =
//        async { await() - other.toInt() } as DeferredInt
//
//    override fun <S : Number> times(other: DeferredNumber<S>): DeferredInt =
//        async { await() * other.await().toInt() } as DeferredInt
//
//    override fun <S : Number> times(other: S): DeferredInt =
//        async { await() * other.toInt() } as DeferredInt
//
//    override fun <S : Number> div(other: DeferredNumber<S>): DeferredInt =
//        async { await() / other.await().toInt() } as DeferredInt
//
//    override fun <S : Number> div(other: S): DeferredInt =
//        async { await() / other.toInt() } as DeferredInt
//
//    override fun <S : Number> rem(other: DeferredNumber<S>): DeferredInt =
//        async { await() % other.await().toInt() } as DeferredInt
//
//    override fun <S : Number> rem(other: S): DeferredInt =
//        async { await() % other.toInt() } as DeferredInt
//
//
//
//    override suspend fun <T : DeferredNumber<*>> compareTo(other: T): Int {
//        return when (other) {
//            is DeferredNumber
//        }
//
//
//    }
//
//    override suspend fun compareTo(other: Number): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun inc(): DeferredInt = this + 1
//
//    override fun dec(): DeferredInt = this - 1
//}
//
//suspend fun DeferredInt.equals(other: Any?): Boolean = when (other) {
//    is Int -> await() == other
//    is DeferredInt -> await() == other.await()
//    else -> false
//
//}
//
//fun Deferred<Int>.asDeferredPrimitive() = DeferredInt(this)
//fun Deferred<Int>.asDeferredInt() = DeferredInt(this)
//
//fun CoroutineScope.deferred(value: Int): DeferredInt = DeferredInt(async { value })
//
//
//class DeferredLong(deferred: Deferred<Long>) : DeferredNumber<Long>(deferred) {
//    override fun <S : Number> plus(other: DeferredNumber<S>): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> plus(other: S): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: DeferredNumber<S>): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: S): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: DeferredNumber<S>): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: S): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: DeferredNumber<S>): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: S): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: DeferredNumber<S>): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: S): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun inc(): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override fun dec(): DeferredNumber<Long> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun compareTo(other: Number): Int {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun <T : DeferredNumber<*>> compareTo(other: T): Int {
//        TODO("Not yet implemented")
//    }
//
//}
//
//fun Deferred<Long>.asDeferredPrimitive() = DeferredLong(this)
//fun Deferred<Long>.asDeferredLong() = DeferredLong(this)
//
//fun CoroutineScope.deferred(value: Long): DeferredLong = async { value }.toDeferredPrimitive()
//
//class DeferredDouble(deferred: Deferred<Double>) : DeferredNumber<Double>(deferred) {
//    override fun <S : Number> plus(other: DeferredNumber<S>): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> plus(other: S): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: DeferredNumber<S>): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: S): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: DeferredNumber<S>): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: S): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: DeferredNumber<S>): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: S): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: DeferredNumber<S>): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: S): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun inc(): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override fun dec(): DeferredNumber<Double> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun compareTo(other: Number): Int {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun <T : DeferredNumber<*>> compareTo(other: T): Int {
//        TODO("Not yet implemented")
//    }
//}
//
//fun CoroutineScope.deferred(value: Double): DeferredDouble = async { value }.toDeferredPrimitive()
//
//class DeferredShort(deferred: Deferred<Short>) : DeferredNumber<Short>(deferred) {
//    override fun <S : Number> plus(other: DeferredNumber<S>): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> plus(other: S): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: DeferredNumber<S>): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: S): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: DeferredNumber<S>): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: S): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: DeferredNumber<S>): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: S): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: DeferredNumber<S>): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: S): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun inc(): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override fun dec(): DeferredNumber<Short> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun compareTo(other: Number): Int {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun <T : DeferredNumber<*>> compareTo(other: T): Int {
//        TODO("Not yet implemented")
//    }
//}
//
//fun CoroutineScope.deferred(value: Short): DeferredShort = async { value }.toDeferredPrimitive()
//
//class DeferredFloat(deferred: Deferred<Float>) : DeferredNumber<Float>(deferred) {
//    override fun <S : Number> plus(other: DeferredNumber<S>): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> plus(other: S): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: DeferredNumber<S>): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> minus(other: S): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: DeferredNumber<S>): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> div(other: S): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: DeferredNumber<S>): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> times(other: S): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: DeferredNumber<S>): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun <S : Number> rem(other: S): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun inc(): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override fun dec(): DeferredNumber<Float> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun compareTo(other: Number): Int {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun <T : DeferredNumber<*>> compareTo(other: T): Int {
//        TODO("Not yet implemented")
//    }
//}
//
//fun CoroutineScope.deferred(value: Float): DeferredFloat = async { value }.toDeferredPrimitive()
//
//
//class DeferredByte(deferred: Deferred<Byte>) : DeferredNumber<Byte>(deferred) {
//
//}
//
//
//class DeferredChar(deferred: Deferred<Char>) : DeferredPrimitive<Char>(deferred)
//
//fun CoroutineScope.deferred(value: Char): DeferredChar = async { value }.toDeferredPrimitive()
//
//class DeferredString(deferred: Deferred<String>) : DeferredPrimitive<String>(deferred)
//
//fun CoroutineScope.deferred(value: String): DeferredString = async { value }.toDeferredPrimitive()
//
//@OptIn(ExperimentalStdlibApi::class)
//inline fun <reified S : Deferred<*>> DeferredNumber<*>.cast(): S = when (typeOf<S>()) {
//    typeOf<DeferredInt>(), typeOf<Deferred<Int>>() -> async { await().toInt() }
//    typeOf<DeferredLong>(), typeOf<Deferred<Long>>() -> async { await().toLong() }
//    typeOf<DeferredDouble>(), typeOf<Deferred<Double>>() -> async { await().toDouble() }
//    typeOf<DeferredShort>(), typeOf<Deferred<Short>>() -> async { await().toShort() }
//    typeOf<DeferredFloat>(), typeOf<Deferred<Float>>() -> async { await().toFloat() }
//    typeOf<DeferredChar>(), typeOf<Deferred<Char>>() -> async { await().toChar() }
//    typeOf<DeferredString>(), typeOf<Deferred<String>>() -> async { await().toString() }
//    else -> throw ClassCastException("Can't cast $this to ${typeOf<S>()}")
//} as S
//
//
//inline fun <reified S : DeferredPrimitive<*>> Deferred<*>.asDeferredPrimitive(): S =
//    when (S) {
//        is DeferredString ->
//
//        else ->
//    }
//
//
