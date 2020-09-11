package nl.jolanrensen.kHomeAssistant.domains

import kotlinx.serialization.json.buildJsonObject
import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.domains.AlarmControlPanel.State.*
import nl.jolanrensen.kHomeAssistant.domains.AlarmControlPanel.SupportedFeatures.*
import nl.jolanrensen.kHomeAssistant.entities.*
import nl.jolanrensen.kHomeAssistant.helper.UnsupportedFeatureException
import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
import nl.jolanrensen.kHomeAssistant.toJson
import kotlin.reflect.KClass

class AlarmControlPanel<CodeFormat : Any>(
    override val kHassInstance: KHomeAssistant,
    private val codeFormatClass: KClass<CodeFormat>
) : Domain<AlarmControlPanel<CodeFormat>.Entity> {
    override val domainName: String = "alarm_control_panel"

    enum class SupportedFeatures(val value: Int) {
        SUPPORT_ALARM_ARM_HOME(1),
        SUPPORT_ALARM_ARM_AWAY(2),
        SUPPORT_ALARM_ARM_NIGHT(4),
        SUPPORT_ALARM_TRIGGER(8),
        SUPPORT_ALARM_ARM_CUSTOM_BYPASS(16)
    }

    enum class State(val value: String) {
        DISARMED("disarmed"),
        ARMED_HOME("armed_home"),
        ARMED_AWAY("armed_away"),
        ARMED_NIGHT("armed_night"),
        ARMED_CUSTOM_BYPASS("armed_custom_bypass"),
        PENDING("pending"),
        ARMING("arming"),
        DISARMING("disarming"),
        TRIGGERED("triggered")
    }

    interface HassAttributes : BaseHassAttributes {

        /** Either None, Number or Any. */
        val code_format: String

        /** Last change triggered by. */
        val changed_by: String?

        /** True if code arm is required. */
        val code_arm_required: Boolean

        /** Set of supported features. @see [supportedFeatures]. */
        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        val supported_features: Int

        // Helper

        /** Set of supported features. */
        @OptIn(ExperimentalStdlibApi::class)
        val supportedFeatures: Set<SupportedFeatures>
            get() = buildSet {
                val value = supported_features
                SupportedFeatures.values().forEach {
                    if (it.value and value == it.value)
                        add(it)
                }
            }
    }

    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)

    inner class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<State, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = AlarmControlPanel(kHassInstance, codeFormatClass)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()
        override val additionalToStringAttributes: Array<Attribute<*>> =
            super.additionalToStringAttributes + getHassAttributesHelpers<HassAttributes>()

        override fun stateToString(state: State): String? = state.value
        override fun stringToState(stateValue: String): State? = State.values().find { it.value == stateValue }

        override var state: State
            get() = super.state
            set(value) = TODO("")

        // Attributes
        override val code_format: String by attrsDelegate()
        override val changed_by: String? by attrsDelegate(null)
        override val code_arm_required: Boolean by attrsDelegate(true)

        @Deprecated("You can use the typed version", replaceWith = ReplaceWith("supportedFeatures"))
        override val supported_features: Int by attrsDelegate()

        init {
            when (code_format) {
                "number" -> if (codeFormatClass != Int::class)
                    throw IllegalArgumentException("$name's code_format is $code_format, you are using the wrong variant.")
                "any" -> if (codeFormatClass != Any::class)
                    throw IllegalArgumentException("$name's code_format is $code_format, you are using the wrong variant.")
                "none" -> if (codeFormatClass != Nothing::class)
                    throw IllegalArgumentException("$name's code_format is $code_format, you are using the wrong variant.")
            }
        }

        private fun checkIfSupported(vararg supportedFeatures: SupportedFeatures) {
            supportedFeatures.forEach {
                if (it !in supportedFeatures)
                    throw UnsupportedFeatureException("Unfortunately the alarm control panel $name does not support ${it.name}.")
            }
        }

        fun onTriggered(callback: suspend Entity.() -> Unit) = onStateChangedTo(TRIGGERED, callback)
        fun onPending(callback: suspend Entity.() -> Unit) = onStateChangedTo(PENDING, callback)
        fun onArmedHome(callback: suspend Entity.() -> Unit) = onStateChangedTo(ARMED_HOME, callback)
        fun onArmedAway(callback: suspend Entity.() -> Unit) = onStateChangedTo(ARMED_AWAY, callback)
        fun onArmedNight(callback: suspend Entity.() -> Unit) = onStateChangedTo(ARMED_NIGHT, callback)
        fun onArmedCustomBypass(callback: suspend Entity.() -> Unit) = onStateChangedTo(ARMED_CUSTOM_BYPASS, callback)


        /** Send disarm command. */
        suspend fun disarm(code: CodeFormat? = null, async: Boolean = false): ResultMessage {
            if (code == null && codeFormatClass != Nothing::class)
                throw IllegalArgumentException("code_format is not 'none' but no code was supplied.")
            //val prevState = state
            val result = callService(
                serviceName = "alarm_disarm",
                data = buildJsonObject { if (code != null) put("code", code.toJson()) }
            )
            // TODO check how to wait for result
            if (!async) suspendUntilStateChangedTo(DISARMED)
            return result
        }

        /** Arm alarm as away. */
        suspend fun armAway(code: CodeFormat? = null, async: Boolean = false): ResultMessage {
            if (code == null && code_arm_required && codeFormatClass != Nothing::class)
                throw IllegalArgumentException("code_format is not 'none' but no code was supplied.")
            checkIfSupported(SUPPORT_ALARM_ARM_AWAY)
            val result = callService(
                serviceName = "alarm_arm_away",
                data = buildJsonObject { if (code != null) put("code", code.toJson()) }
            )
            // TODO check how to wait for result
            if (!async) suspendUntilStateChangedTo(ARMED_AWAY)
            return result
        }

        /** Arm alarm as home. */
        suspend fun armHome(code: CodeFormat? = null, async: Boolean = false): ResultMessage {
            if (code == null && code_arm_required && codeFormatClass != Nothing::class)
                throw IllegalArgumentException("code_format is not 'none' but no code was supplied.")
            checkIfSupported(SUPPORT_ALARM_ARM_HOME)
            val result = callService(
                serviceName = "alarm_arm_home",
                data = buildJsonObject { if (code != null) put("code", code.toJson()) }
            )
            // TODO check how to wait for result
            if (!async) suspendUntilStateChangedTo(ARMED_HOME)
            return result
        }

        /** Arm alarm as night. */
        suspend fun armNight(code: CodeFormat? = null, async: Boolean = false): ResultMessage {
            if (code == null && code_arm_required && codeFormatClass != Nothing::class)
                throw IllegalArgumentException("code_format is not 'none' but no code was supplied.")
            checkIfSupported(SUPPORT_ALARM_ARM_NIGHT)
            val result = callService(
                serviceName = "alarm_arm_night",
                data = buildJsonObject { if (code != null) put("code", code.toJson()) }
            )
            // TODO check how to wait for result
            if (!async) suspendUntilStateChangedTo(ARMED_NIGHT)
            return result
        }

        /** Arm alarm as custom bypass. */
        suspend fun armCustomBypass(code: CodeFormat? = null, async: Boolean = false): ResultMessage {
            if (code == null && code_arm_required && codeFormatClass != Nothing::class)
                throw IllegalArgumentException("code_format is not 'none' but no code was supplied.")
            checkIfSupported(SUPPORT_ALARM_ARM_CUSTOM_BYPASS)
            val result = callService(
                serviceName = "alarm_arm_custom_bypass",
                data = buildJsonObject { if (code != null) put("code", code.toJson()) }
            )
            // TODO check how to wait for result
            if (!async) suspendUntilStateChangedTo(ARMED_CUSTOM_BYPASS)
            return result
        }

        /** Trigger the alarm */
        suspend fun trigger(code: CodeFormat? = null, async: Boolean = false): ResultMessage {
            if (code == null && codeFormatClass != Nothing::class)
                throw IllegalArgumentException("code_format is not 'none' but no code was supplied.")
            checkIfSupported(SUPPORT_ALARM_TRIGGER)
            val result = callService(
                serviceName = "alarm_trigger",
                data = buildJsonObject { if (code != null) put("code", code.toJson()) }
            )
            // TODO check how to wait for result
            if (!async) suspendUntilStateChangedTo(TRIGGERED)
            return result
        }
    }
}


val KHomeAssistant.AlarmControlPanelNone: AlarmControlPanel<Nothing>
    get() = AlarmControlPanel(this, Nothing::class)

val KHomeAssistant.AlarmControlPanelNumber: AlarmControlPanel<Int>
    get() = AlarmControlPanel(this, Int::class)

val KHomeAssistant.AlarmControlPanelAny: AlarmControlPanel<Any>
    get() = AlarmControlPanel(this, Any::class)
