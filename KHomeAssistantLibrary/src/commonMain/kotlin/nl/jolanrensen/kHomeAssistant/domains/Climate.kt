//TODO
// package nl.jolanrensen.kHomeAssistant.domains
//
//import kotlinx.serialization.json.json
//import nl.jolanrensen.kHomeAssistant.KHomeAssistant
//import nl.jolanrensen.kHomeAssistant.RunBlocking
//import nl.jolanrensen.kHomeAssistant.RunBlocking.runBlocking
//import nl.jolanrensen.kHomeAssistant.domains.Climate.HvacMode.*
//import nl.jolanrensen.kHomeAssistant.entities.*
//import nl.jolanrensen.kHomeAssistant.messages.ResultMessage
//
//class Climate(override val kHassInstance: KHomeAssistant) : Domain<Climate.Entity> {
//    override val domainName = "climate"
//
//    /** Making sure Climate acts as a singleton. */
//    override fun equals(other: Any?) = other is Climate
//    override fun hashCode(): Int = domainName.hashCode()
//
//    enum class SupportedFeatures(val value: Int) {
//        SUPPORT_TARGET_TEMPERATURE(1),
//        SUPPORT_TARGET_TEMPERATURE_RANGE(2),
//        SUPPORT_TARGET_HUMIDITY(4),
//        SUPPORT_FAN_MODE(8),
//        SUPPORT_PRESET_MODE(16),
//        SUPPORT_SWING_MODE(32),
//        SUPPORT_AUX_HEAT(64)
//    }
//
//    enum class Presets(val value: String) {
//        PRESET_NONE("none"),
//        PRESET_ECO("eco"),
//        PRESET_AWAY("away"),
//        PRESET_BOOST("boost"),
//        PRESET_COMFORT("comfort"),
//        PRESET_HOME("home"),
//        PRESET_SLEEP("sleep"),
//        PRESET_ACTIVITY("activity")
//    }
//
//    enum class FanMode(val value: String) {
//        FAN_ON("on"),
//        FAN_OFF("off"),
//        FAN_AUTO("auto"),
//        FAN_LOW("low"),
//        FAN_MEDIUM("medium"),
//        FAN_HIGH("high"),
//        FAN_MIDDLE("middle"),
//        FAN_FOCUS("focus"),
//        FAN_DIFFUSE("diffuse")
//    }
//
//    enum class SwingMode(val value: String) {
//        SWING_OFF("off"),
//        SWING_BOTH("both"),
//        SWING_VERTICAL("vertical"),
//        SWING_HORIZONTAL("horizontal")
//    }
//
//    enum class HvacMode(val value: String) {
//        HVAC_MODE_OFF("off"),
//        HVAC_MODE_HEAT("heat"),
//        HVAC_MODE_COOL("cool"),
//        HVAC_MODE_HEAT_COOL("heat_cool"),
//        HVAC_MODE_AUTO("auto"),
//        HVAC_MODE_DRY("dry"),
//        HVAC_MODE_FAN_ONLY("fan_only"),
//    }
//
//    enum class HvacAction(val value: String) {
//        CURRENT_HVAC_OFF("off"),
//        CURRENT_HVAC_HEAT("heating"),
//        CURRENT_HVAC_COOL("cooling"),
//        CURRENT_HVAC_DRY("drying"),
//        CURRENT_HVAC_IDLE("idle"),
//        CURRENT_HVAC_FAN("fan")
//    }
//
//    override fun Entity(name: String): Entity = Entity(kHassInstance = kHassInstance, name = name)
//
//    interface HassAttributes : BaseHassAttributes {
//
//    }
//
//    class Entity(
//        override val kHassInstance: KHomeAssistant,
//        override val name: String
//    ) : BaseEntity<HvacMode, HassAttributes>(
//        kHassInstance = kHassInstance,
//        name = name,
//        domain = Climate(kHassInstance)
//    ), HassAttributes {
//
//        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()
//        override val additionalToStringAttributes: Array<Attribute<*>> = super.additionalToStringAttributes +
//                getHassAttributesHelpers<HassAttributes>()
//
//
//        override fun stateToString(state: HvacMode): String = state.value
//        override fun stringToState(stateValue: String): HvacMode? = HvacMode.values().find { it.value == stateValue }
//
//
//        /** state can also be writable. */
//        override var state: HvacMode
//            get() = super.state
//            set(value) {
//                runBlocking { TODO() }
//            }
//
//        /** Turn on the climate device. */
//        suspend fun turnOn(async: Boolean = false): ResultMessage {
//            if (HVAC_MODE_OFF !in hvacModes)
//                throw IllegalArgumentException("Climate does not support being turned on/off.")
//            val result = callService("turn_on")
//            if (!async) suspendUntilStateChanged({ it != HVAC_MODE_OFF })
//            return result
//        }
//
//        /** Turn off the climate device. */
//        suspend fun turnOff(async: Boolean = false): ResultMessage {
//            if (HVAC_MODE_OFF !in hvacModes)
//                throw IllegalArgumentException("Climate does not support being turned on/off.")
//            val result = callService("turn_off")
//            if (!async) suspendUntilStateChangedTo(HVAC_MODE_OFF)
//            return result
//        }
//
//        /** Set climate device’s HVAC mode. */
//        suspend fun setHvacMode(hvacMode: HvacMode, async: Boolean = false) = setHvacMode(hvacMode.value, async)
//
//        /** Set climate device’s HVAC mode. */
//        suspend fun setHvacMode(hvacModeName: String, async: Boolean = false): ResultMessage {
//            val result = callService(
//                serviceName = "set_hvac_mode",
//                data = buildJsonObject {
//                    hvacModeName.let {
//                        if (it !in hvac_modes)
//                            throw IllegalArgumentException("Climate does not support HVAC mode $it")
//                        "hvac_mode" to it
//                    }
//                }
//            )
//            if (!async) suspendUntilStateChanged({ it?.value == hvacModeName })
//            return result
//        }
//
//
//
//    }
//}
//
///** Access the Climate Domain */
//val KHomeAssistant.Climate: Climate
//    get() = Climate(this)