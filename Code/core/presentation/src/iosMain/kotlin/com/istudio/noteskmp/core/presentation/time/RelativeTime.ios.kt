package com.istudio.noteskmp.core.presentation.time

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun formatShortDate(date: LocalDate, includeYear: Boolean): String {
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = (date.month.ordinal + 1).toLong()
        day = date.day.toLong()
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(components) ?: return date.toString()
    val formatter = NSDateFormatter().apply {
        locale = NSLocale.currentLocale
        setLocalizedDateFormatFromTemplate(if (includeYear) "yMMMd" else "MMMd")
    }
    return formatter.stringFromDate(nsDate)
}
