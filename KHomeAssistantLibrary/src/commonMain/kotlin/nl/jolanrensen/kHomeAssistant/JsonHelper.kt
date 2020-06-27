package nl.jolanrensen.kHomeAssistant

import kotlinx.serialization.json.*
import kotlinx.serialization.json.JsonNull.jsonNull
import nl.jolanrensen.kHomeAssistant.entities.HassAttributes
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
fun <T : Any?> JsonElement.cast(type: KType): T = try {
    when (type) {
        // NULLABLE

        // JsonElements
        typeOf<JsonPrimitive?>() -> primitive as T
        typeOf<JsonObject?>() -> jsonObject as T
        typeOf<Map<String, JsonElement>?>() -> jsonObject.toMap() as T
        // TODO maybe add all for map

        typeOf<JsonNull?>() -> jsonNull as T
        typeOf<JsonArray?>() -> jsonArray as T

        typeOf<List<JsonPrimitive>?>() -> jsonArray.map { it.primitive } as T
        typeOf<List<JsonObject>?>() -> jsonArray.map { it.jsonObject } as T
        typeOf<List<JsonArray>?>() -> jsonArray.map { it.jsonArray } as T
        typeOf<List<JsonNull>?>() -> jsonArray.map { it.jsonNull } as T
        typeOf<List<String>?>() -> jsonArray.map { it.content } as T
        typeOf<List<Int>?>() -> jsonArray.map { it.int } as T
        typeOf<List<Long>?>() -> jsonArray.map { it.long } as T
        typeOf<List<Double>?>() -> jsonArray.map { it.double } as T
        typeOf<List<Float>?>() -> jsonArray.map { it.float } as T
        typeOf<List<Boolean>?>() -> jsonArray.map { it.boolean } as T

        // JsonPrimitives
        typeOf<String?>() -> content as T
        typeOf<Int?>() -> int as T
        typeOf<Long?>() -> long as T
        typeOf<Double?>() -> double as T
        typeOf<Float?>() -> float as T
        typeOf<Boolean?>() -> boolean as T


        // NON-NULLABLE

        // JsonElements
        typeOf<JsonPrimitive>() -> primitive as T
        typeOf<JsonObject>() -> jsonObject as T
        typeOf<Map<String, JsonElement>>() -> jsonObject.toMap() as T
        // TODO maybe add all for map

        typeOf<JsonNull>() -> jsonNull as T
        typeOf<JsonArray>() -> jsonArray as T

        typeOf<List<JsonPrimitive>>() -> jsonArray.map { it.primitive } as T
        typeOf<List<JsonObject>>() -> jsonArray.map { it.jsonObject } as T
        typeOf<List<JsonArray>>() -> jsonArray.map { it.jsonArray } as T
        typeOf<List<JsonNull>>() -> jsonArray.map { it.jsonNull } as T
        typeOf<List<String>>() -> jsonArray.map { it.content } as T
        typeOf<List<Int>>() -> jsonArray.map { it.int } as T
        typeOf<List<Long>>() -> jsonArray.map { it.long } as T
        typeOf<List<Double>>() -> jsonArray.map { it.double } as T
        typeOf<List<Float>>() -> jsonArray.map { it.float } as T
        typeOf<List<Boolean>>() -> jsonArray.map { it.boolean } as T

        // JsonPrimitives
        typeOf<String>() -> content as T
        typeOf<Int>() -> int as T
        typeOf<Long>() -> long as T
        typeOf<Double>() -> double as T
        typeOf<Float>() -> float as T
        typeOf<Boolean>() -> boolean as T
        else -> throw IllegalArgumentException("Couldn't cast $this to type $type")
    }

} catch (e: Exception) {
    throw IllegalArgumentException("Couldn't cast $this to type $type", e)
}

@Suppress("UNCHECKED_CAST")
fun Any?.toJson(): JsonElement = this?.let {
    when (it) {
        is Number -> JsonPrimitive(it)
        is String -> JsonPrimitive(it)
        is Boolean -> JsonPrimitive(it)

        is Array<*> -> JsonArray(it.map { it.toJson() })
        is List<*> -> JsonArray(it.map { it.toJson() })

        is Map<*, *> -> JsonObject(
            it
                .mapKeys { it.toString() }
                .mapValues { it.toJson() }
        )

        else -> throw IllegalArgumentException("Couldn't cast $this to json")
    }
} ?: jsonNull

operator fun JsonArray.plus(other: JsonArray) = JsonArray(content + other.content)
operator fun JsonObject.plus(other: JsonObject) = JsonObject(content + other.content)