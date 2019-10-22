package com.climapp.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

object ClimappUtils {
    val imagesUrl = "https://darksky.net/images/weather-icons/"

    fun getDayOfWeek(date: String): String {
        val locale = Locale("es", "ES")
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).getDayOfWeek().getDisplayName(
            TextStyle.FULL,locale)
    }

    fun getRounded(number: Double): Long {
        return Math.round(number)
    }

}