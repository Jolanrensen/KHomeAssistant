package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.json
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.cast
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.domains.Light
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

/**
 * Input Text domain
 * https://www.home-assistant.io/integrations/input_text/
 * */
class InputText(override val kHassInstance: KHomeAssistant) : Domain<InputText.Entity> {
    override val domainName = "input_text"

    /** Making sure InputText acts as a singleton. */
    override fun equals(other: Any?) = other is InputText
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_text configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String) = Entity(kHassInstance = kHassInstance, name = name)

    enum class InputTextMode(val stateValue: String) {
        TEXT("text"), PASSWORD("password")
    }

    interface HassAttributes : BaseHassAttributes {
        // read only

        /** Minimum length for the text value. 0 is the minimum number of characters allowed in an entity state. */
        val min: Int

        /** Maximum length for the text value. 255 is the maximum number of characters allowed in an entity state. */
        val max: Int

        /** Initial value when Home Assistant starts. */
        val initial: String

        /** Regex pattern for client-side validation. @see [pattern_]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("pattern_"))
        val pattern: String

        /** Can specify text or password. Elements of type “password” provide a way for the user to securely enter a value. @see [mode_]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("mode_"))
        val mode: String

        /** Is true if this input text is editable. */
        val editable: Boolean

        // Helper functions

        /** Regex pattern for client-side validation. */
        val pattern_: Regex
            get() = Regex(pattern)

        /** Can specify text or password. Elements of type “password” provide a way for the user to securely enter a value. */
        val mode_: InputTextMode
            get() = try {
                InputTextMode.values().find { it.stateValue == mode }!!
            } catch (e: Exception) {
                InputTextMode.TEXT
            }
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<String, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = InputText(kHassInstance)
    ), HassAttributes {
        /** Delegate so you can control an [InputText] like a local variable
         * Simply type "var yourString by [InputText].Entity("your_string")
         **/
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String = state
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            state = value
        }

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        override fun stringToState(stateValue: String) = stateValue

        override fun stateToString(state: String) = state

        /** [state] can also be writable. */
        override var state: String
            get() = super.state
            set(value) {
                runBlocking { setValue(value) }
            }

        // Attributes
        override val min: Int by attrsDelegate(0)
        override val max: Int by attrsDelegate(255)
        override val initial: String by attrsDelegate("")
        override val pattern: String by attrsDelegate()
        override val mode: String by attrsDelegate()
        override val editable: Boolean by attrsDelegate()

        /** Set the state value. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun setValue(value: String, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_value",
                data = json {
                    value.let {
                        val pattern = try {
                            pattern_
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