package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import nl.jolanrensen.kHomeAssistant.KHomeAssistantContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.AttributesDelegate
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilAttributeChanged
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilStateChangedTo
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

// TODO

/**
 * https://www.home-assistant.io/integrations/input_select/
 * */
class InputSelect(override var kHomeAssistant: () -> KHomeAssistant?) : Domain<InputSelect.Entity> {
    override val domainName = "input_select"

    override fun checkContext() = require(kHomeAssistant() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputSelect.' from a KHomeAssistantContext instead of using InputSelect directly.""".trimMargin()
    }

    /** Making sure InputSelect acts as a singleton. */
    override fun equals(other: Any?) = other is InputSelect
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_select configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String): Entity = Entity(kHomeAssistant = kHomeAssistant, name = name)

    class Entity(
        override val kHomeAssistant: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<String>(
        kHomeAssistant = kHomeAssistant,
        name = name,
        domain = InputSelect(kHomeAssistant)
    ) {
        init {
            attributes += arrayOf(
                ::options,
                ::editable
            )
        }

        /** Some attributes are writable. */
        @Suppress("UNCHECKED_CAST")
        operator fun <V : Any?> AttributesDelegate.setValue(
            thisRef: BaseEntity<*>?,
            property: KProperty<*>,
            value: V
        ) {
            runBlocking {
                when (property.name) {
                    ::options.name -> {
                        setOptions(value as List<String>)
                    }
                }
                Unit
            }
        }

        // Read only attributes

        val editable: Boolean? by attrsDelegate

        // Read-write attributes

        /** List of options to choose from. */
        var options: List<String>? by attrsDelegate

        override fun parseStateValue(stateValue: String) = stateValue

        override fun getStateValue(state: String) = state

        /** [state] can also be writable. */
        override var state: String
            get() = super.state
            set(value) {
                runBlocking { selectOption(value) }
            }

        /** Change the available options. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun setOptions(options: List<String>, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_options",
                data = buildMap<String, JsonElement> {
                    options.let {
                        val entityOptions = this@Entity.options
                        if (entityOptions == null
                            || !options.containsAll(entityOptions)
                            || !entityOptions.containsAll(options)
                        ) {
                            this["options"] = jsonArray {
                                options.forEach { +it }
                            }
                        }
                    }
                } // TODO test
            )
            if (!async) suspendUntilAttributeChanged(
                attribute = ::options,
                condition = {
                    if (it == null) {
                        it == options
                    } else {
                        options.containsAll(it) && it.containsAll(options)
                    }
                })
            return result
        }

        /** Set the state value. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun selectOption(option: String, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "select_option",
                data = buildMap<String, JsonElement> {
                    option.let {
                        if (it !in options!!)
                            throw IllegalArgumentException("incorrect value $it")
                        this["option"] = JsonPrimitive(option)
                    }
                }
            )
            if (!async) suspendUntilStateChangedTo(option)
            return result
        }
    }
}

/** Access the InputSelect Domain. */
val KHomeAssistantContext.InputSelect: InputSelect
    get() = InputSelect(kHomeAssistant)