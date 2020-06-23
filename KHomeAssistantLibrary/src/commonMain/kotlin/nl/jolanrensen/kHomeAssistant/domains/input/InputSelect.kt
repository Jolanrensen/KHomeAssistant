package nl.jolanrensen.kHomeAssistant.domains.input

import kotlinx.serialization.json.json
import kotlinx.serialization.json.jsonArray
import nl.jolanrensen.kHomeAssistant.HasKHassContext
import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
import nl.jolanrensen.kHomeAssistant.core.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.Domain
import nl.jolanrensen.kHomeAssistant.entities.AttributesDelegate
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilAttributeChanged
import nl.jolanrensen.kHomeAssistant.entities.suspendUntilStateChangedTo
import nl.jolanrensen.kHomeAssistant.contentEquals
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import kotlin.reflect.KProperty

// TODO

/**
 * https://www.home-assistant.io/integrations/input_select/
 * */
class InputSelect(override var getKHass: () -> KHomeAssistant?) : Domain<InputSelect.Entity> {
    override val domainName = "input_select"

    override fun checkContext() = require(getKHass() != null) {
        """ Please initialize kHomeAssistant before calling this.
            Make sure to use the helper function 'InputSelect.' from a KHomeAssistantContext instead of using InputSelect directly.""".trimMargin()
    }

    /** Making sure InputSelect acts as a singleton. */
    override fun equals(other: Any?) = other is InputSelect
    override fun hashCode(): Int = domainName.hashCode()

    /** Reload input_select configuration. */
    suspend fun reload() = callService("reload")

    override fun Entity(name: String): Entity = Entity(getKHass = getKHass, name = name)

    class Entity(
        override val getKHass: () -> KHomeAssistant?,
        override val name: String
    ) : BaseEntity<String>(
        getKHass = getKHass,
        name = name,
        domain = InputSelect(getKHass)
    ) {
        init {
            attributes += arrayOf(
                ::options,
                ::editable
            )
        }

        /** Some attributes are writable. */
        @Suppress("UNCHECKED_CAST")
        operator fun <V : Any?> AttributesDelegate<V>.setValue(
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

        val editable: Boolean by attrsDelegate()

        // Read-write attributes

        /** List of options to choose from. */
        var options: List<String> by attrsDelegate()

        override fun stringToState(stateValue: String) = stateValue

        override fun stateToString(state: String) = state

        /** [state] can also be writable. */
        override var state: String
            get() = super.state
            set(value) {
                runBlocking { selectOption(value) }
            }

        /** Select the previous option. */
        suspend fun selectPrevious(async: Boolean = false): ResultMessage {
            var previous: String? = null
            if (!async) {
                val entityOptions = options
                previous = entityOptions[(entityOptions.indexOf(state) - 1) % entityOptions.size]
            }
            val result = callService(serviceName = "select_previous")
            if (!async) suspendUntilStateChangedTo(previous!!)
            return result
        }

        /** Select the next option. */
        suspend fun selectNext(async: Boolean = false): ResultMessage {
            var next: String? = null
            if (!async) {
                val entityOptions = options
                next = entityOptions[(entityOptions.indexOf(state) + 1) % entityOptions.size]
            }
            val result = callService(serviceName = "select_next")
            if (!async) suspendUntilStateChangedTo(next!!)
            return result
        }

        /** Change the available options. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun setOptions(options: List<String>, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "set_options",
                data = json {
                    options.let {
                        val entityOptions = this@Entity.options
                        if (!options.contentEquals(entityOptions)) {
                            "options" to jsonArray {
                                options.forEach { +it }
                            }
                        }
                    }
                } // TODO test
            )
            if (!async) suspendUntilAttributeChanged(
                attribute = ::options,
                condition = {
                    options.contentEquals(it)
                })
            return result
        }

        /** Set the state value. */
        @OptIn(ExperimentalStdlibApi::class)
        suspend fun selectOption(option: String, async: Boolean = false): ResultMessage {
            val result = callService(
                serviceName = "select_option",
                data = json {
                    option.let {
                        if (it !in options)
                            throw IllegalArgumentException("incorrect value $it")
                        "option" to option
                    }
                }
            )
            if (!async) suspendUntilStateChangedTo(option)
            return result
        }
    }
}

/** Access the InputSelect Domain. */
val HasKHassContext.InputSelect: InputSelect
    get() = InputSelect(getKHass)