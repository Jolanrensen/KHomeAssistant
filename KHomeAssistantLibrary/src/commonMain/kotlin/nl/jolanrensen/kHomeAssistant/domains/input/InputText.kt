package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilStateChangedTo
import nl.jolanrensen.kHomeAssistant.cast
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

/**
 * Input Text domain
 * https://www.home-assistant.io/integrations/input_text/
 * */
class InputText(kHassInstance: KHomeAssistant) : Domain<InputText.Entity>, KHomeAssistant by kHassInstance {
    override val domainName = "input_text"

    /** Making sure InputText acts as a singleton. */
    override fun equals(other: Any?) = other is InputText
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_text configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String) = Entity(kHassInstance = this, name = name)

    enum class InputTextMode(val stateValue: String) {
        TEXT("text"), PASSWORD("password")
    }

    class Entity(
        kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<String>(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputText(kHassInstance)
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

        override fun stringToState(stateValue: String) = stateValue

        override fun stateToString(state: String) = state

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
                data = json {
                    value.let {
                        val pattern = try {
                            pattern
                        } catch (e: Exception) {
                            null
                        }
                        if (it.length !in min..max || pattern != null && pattern.matches(it))
                            throw IllegalArgumentException("incorrect value $it")
                        "value" to it
                    }
                }
            )
            if (!async) suspendUntilStateChangedTo(value)
            return result
        }
    }
}

/** Access the InputText Domain. */
val KHomeAssistant.InputText: InputText
    get() = InputText(this)