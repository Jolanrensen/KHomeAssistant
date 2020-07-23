package nl.jolanrensen.kHomeAssistant.helper

enum class TemperatureUnit(val value: String) {
    TEMP_CELSIUS("°C"),
    TEMP_FAHRENHEIT("°F"),
    TEMP_KELVIN("°K") // seriously Hass... degree Kelvin?!
}