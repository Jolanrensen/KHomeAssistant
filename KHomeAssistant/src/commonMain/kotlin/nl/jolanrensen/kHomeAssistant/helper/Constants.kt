package nl.jolanrensen.kHomeAssistant.helper

import com.soywiz.klock.DateFormat
import com.soywiz.klock.PatternDateFormat

val HASS_DATE_FORMAT: PatternDateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSXX:XX")