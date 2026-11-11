package com.istudio.noteskmp.core.presentation.time

import android.text.format.DateFormat
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

actual fun formatShortDate(date: LocalDate, includeYear: Boolean): String {
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, if (includeYear) "yMMMd" else "MMMd")
    val calendar = Calendar.getInstance().apply {
        clear()
        set(date.year, date.month.ordinal, date.day)
    }
    return SimpleDateFormat(pattern, locale).format(calendar.time)
}
