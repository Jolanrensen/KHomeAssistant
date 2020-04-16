package nl.jolanrensen.kHomeAssistant.helper

import kotlinx.serialization.json.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf


inline fun <reified T : Any?> JsonElement.cast(): T? = try {
    when (T::class) {

        // JsonElements
        JsonPrimitive::class -> primitive as T
        JsonObject::class, Map::class -> jsonObject as T
        JsonArray::class, List::class -> jsonArray as T
        JsonNull::class -> jsonNull as T

        // JsonPrimitives
        String::class -> content as T
        Int::class -> int as T
        Long::class -> long as T
        Double::class -> double as T
        Float::class -> float as T
        Boolean::class -> boolean as T
        else -> null
    }
} catch (e: Exception) {
    null
}

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalStdlibApi::class)
fun <T : Any?> JsonElement.cast(type: KType): T? = try {
    when (type) {
        // JsonElements
        typeOf<JsonPrimitive?>() -> primitive as T
        typeOf<JsonObject?>(), typeOf<Map<String, JsonElement>?>() -> jsonObject as T
        typeOf<JsonNull?>() -> jsonNull as T

        typeOf<JsonArray?>(),
        typeOf<List<JsonPrimitive>?>(),
        typeOf<List<JsonObject>?>(),
        typeOf<List<JsonArray>?>(),
        typeOf<List<JsonNull>?>(),
        typeOf<List<String>?>(),
        typeOf<List<Int>?>(),
        typeOf<List<Long>?>(),
        typeOf<List<Double>?>(),
        typeOf<List<Float>?>(),
        typeOf<List<Boolean>?>() -> jsonArray as T

        // JsonPrimitives
        typeOf<String?>() -> content as T
        typeOf<Int?>() -> int as T
        typeOf<Long?>() -> long as T
        typeOf<Double?>() -> double as T
        typeOf<Float?>() -> float as T
        typeOf<Boolean?>() -> boolean as T
        else -> null
    }

} catch (e: Exception) {
    null
}