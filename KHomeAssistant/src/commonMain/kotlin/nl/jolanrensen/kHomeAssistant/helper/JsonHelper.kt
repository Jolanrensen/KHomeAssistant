package nl.jolanrensen.kHomeAssistant.helper

import kotlinx.serialization.json.*
import kotlin.reflect.KClass


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
} catch (e: Exception) { null }
