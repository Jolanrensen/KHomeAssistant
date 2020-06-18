package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import nl.jolanrensen.kHomeAssistant.HasContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilStateChangedTo
import nl.jolanrensen.kHomeAssistant.helper.cast
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

/**
 * Input Text domain
 * https://www.home-assistant.io/integrations/input_text/
 * */
class InputText(override var getKHomeAssistant: () -> KHomeAssistant?) : Domain<InputText.Entity> {
    override val domainName = "input_text"

    override fun checkContext() = require(getKHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputText.' from a KHomeAssistantContext instead of using InputText directly.""".trimMargin()
    }

    /** Making sure InputText acts as a singleton. */
    override fun equals(other: Any?) = other is InputText
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_text configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String) = Entity(getKHomeAssistant = getKHomeAssistant, name = name)

    enum class InputTextMode(val stateValue: String) {
        TEXT("text"), PASSWORD("password")
    }

    class Entity(
        override val getKHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<String>(
        getKHomeAssistant = getKHomeAssistant,
        name = name,
        domain = InputText(getKHomeAssistant)
    ) {
        /** Delegate so you can control an [InputText] like a local variable
         * Simply type "var yourString by [InputText].Entity("your_string")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String = state
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            state = value
        }

        init {
            attributes += listOf(
                ::min,
                ::max,
                ::initial,
                ::pattern,
                ::mode,
                ::editable
            )
        }

        override fun parseStateValue(stateValue: String) = stateValue

        override fun getStateValue(state: String) = state

        /** [state] can also be writable. */
        override var state: String
            get() = super.state
            set(value) {
                runBlocking { setValue(value) }
            }

        // Attributes
        // read only

        /** Minimum length for the text value. 0 is the minimum number of characters allowed in an entity state. */
        val min: Int by attrsDelegate(0)

        /** Maximum length for the text value. 255 is the maximum number of characters allowed in an entity state. */
        val max: Int by attrsDelegate(255)

        /** Initial value when Home Assistant starts. */
        val initial: String by attrsDelegate()

        /** Regex pattern for client-side validation. */
        val pattern: Regex
            get() = Regex(rawAttributes[::pattern.name]!!.cast<String>()!!)

        /** Can specify text or password. Elements of type “password” provide a way for the user to securely enter a value. */
        val mode: InputTextMode
            get() =
                try {
                    InputTextMode.values()
                        .find { it.stateValue == rawAttributes[::mode.name]!!.cast<String>()!! }!!
                } catch (e: Exception) {
                    InputTextMode.TEXT
                }

        val editable: Boolean by attrsDelegate()

        /** Set the state value. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun setValue(value: String, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_value",
                data = buildMap<String, JsonElement> {
                    value.let {
                        val pattern = try { pattern } catch (e: Exception) { null }
                        if (it.length !in min..max || pattern != null && pattern.matches(it))
                            throw IllegalArgumentException("incorrect value $it")
                        this["value"] = JsonPrimitive(it)
                    }
                }
            )
            if (!async) suspendUntilStateChangedTo(value)
            return result
        }
    }
}

/** Access the InputText Domain. */
val HasContext.InputText: InputText
    get() = InputText(getKHomeAssistant)