package com.example.pr_52.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formats {

    private val locale = Locale.forLanguageTag("ru-RU")
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", locale)

    /** 79.4213 -> "79,4213" */
    fun rate(value: Double): String = String.format(locale, "%.4f", value)

    /** 1762600000000 -> "08.11.2026 14:35" */
    fun date(timestamp: Long): String = dateFormat.format(Date(timestamp))
}
