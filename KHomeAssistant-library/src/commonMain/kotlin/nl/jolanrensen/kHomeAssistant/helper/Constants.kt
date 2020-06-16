package nl.jolanrensen.kHomeAssistant.helper

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateFormat.Companion.FORMAT1
import com.soywiz.klock.PatternDateFormat

val HASS_DATE_FORMAT: PatternDateFormat = try {
    DateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSSXX:XX")
} catch (e: Exception) {
    FORMAT1
}

// 2020-04-22T03:52:42+00:00
val HASS_DATE_FORMAT_SUN: PatternDateFormat = FORMAT1
