package nl.jolanrensen.kHomeAssistant.domains

import nl.jolanrensen.kHomeAssistant.KHomeAssistant
import nl.jolanrensen.kHomeAssistant.entities.Attribute
import nl.jolanrensen.kHomeAssistant.entities.BaseEntity
import nl.jolanrensen.kHomeAssistant.entities.BaseHassAttributes
import nl.jolanrensen.kHomeAssistant.entities.getHassAttributes

/**
 * https://developers.home-assistant.io/docs/core/entity/air-quality
 *
 * NOTE: Most air quality integrations produce results as a 'sensor' instead of an 'air_quality' entity.
 */
class AirQuality(override val kHassInstance: KHomeAssistant) : Domain<AirQuality.Entity> {
    override val domainName: String = "air_quality"

    /** Making sure AirQuality acts as a singleton. */
    override fun equals(other: Any?) = other is AirQuality
    override fun hashCode(): Int = domainName.hashCode()

    override fun Entity(name: String) = Entity(kHassInstance = kHassInstance, name = name)

    interface HassAttributes : BaseHassAttributes {
        // Read only

        /** The particulate matter 2.5 (<= 2.5 μm) level. */
        val particulate_matter_2_5: Float

        /** The particulate matter 10 (<= 10 μm) level. */
        val particulate_matter_10: Float

        /** The particulate matter 0.1 (<= 0.1 μm) level. */
        val particulate_matter_0_1: Float

        /** The Air Quality Index (AQI). */
        val air_quality_index: Float

        /** The O3 (ozone) level. */
        val ozone: Float

        /** The CO (carbon monoxide) level. */
        val carbon_monoxide: Float

        /** The CO2 (carbon dioxide) level. */
        val carbon_dioxide: Float

        /** The SO2 (sulphur dioxide) level. */
        val sulphur_dioxide: Float

        /** The N2O (nitrogen oxide) level. */
        val nitrogen_oxide: Float

        /** The NO (nitrogen monoxide) level. */
        val nitrogen_monoxide: Float

        /** The NO2 (nitrogen dioxide) level. */
        val nitrogen_dioxide: Float

        /** The volatile organic compounds (VOC) level. */
        val volatile_organic_compounds: Float
    }

    class Entity(
        override val kHassInstance: KHomeAssistant,
        override val name: String
    ) : BaseEntity<String, HassAttributes>(
        kHassInstance = kHassInstance,
        name = name,
        domain = AirQuality(kHassInstance)
    ), HassAttributes {

        override val hassAttributes: Array<Attribute<*>> = getHassAttributes<HassAttributes>()

        override fun stateToString(state: String) = state
        override fun stringToState(stateValue: String) = stateValue

        // Attributes
        override val particulate_matter_2_5: Float by attrsDelegate()
        override val particulate_matter_10: Float by attrsDelegate()
        override val particulate_matter_0_1: Float by attrsDelegate()
        override val air_quality_index: Float by attrsDelegate()
        override val ozone: Float by attrsDelegate()
        override val carbon_monoxide: Float by attrsDelegate()
        override val carbon_dioxide: Float by attrsDelegate()
        override val sulphur_dioxide: Float by attrsDelegate()
        override val nitrogen_oxide: Float by attrsDelegate()
        override val nitrogen_monoxide: Float by attrsDelegate()
        override val nitrogen_dioxide: Float by attrsDelegate()
        override val volatile_organic_compounds: Float by attrsDelegate()
    }
}

/** Access the AirQuality Domain */
val KHomeAssistant.AirQuality: AirQuality
    get() = AirQuality(this)