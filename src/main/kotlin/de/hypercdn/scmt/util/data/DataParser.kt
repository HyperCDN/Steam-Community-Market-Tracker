package de.hypercdn.scmt.util.data

import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun parseCurrencyToNumber(string: String): Pair<Number?, String?> {
    val detectedCurrencies = Locale.getAvailableLocales()
        .map { getCurrencyForLocale(it) }
        .filter { it != null && string.contains(it.symbol) }
        .distinctBy { it!!.currencyCode }
        .toList()
    val cleaned = string.replace(Regex("[^0-9,\\._]"), "")
    return parseNumberWithDecorations(cleaned) to detectedCurrencies.get(0)?.currencyCode

}
private fun getCurrencyForLocale(locale: Locale): Currency? {
    return try {
        Currency.getInstance(locale)
    }catch (e: Exception){
        return null
    }
}

fun sleepWithoutException(unit: TimeUnit, value: Long) {
    try {
        unit.sleep(value)
    }catch (_: Exception) {}
}

fun parseNumberWithDecorations(string: String): Number {
    val a = NumberFormat.getNumberInstance(Locale.US).parse(string).toDouble()
    val b = NumberFormat.getNumberInstance(Locale.GERMAN).parse(string).toDouble()
    return if (string.matches(Regex("^[+-]?[0-9]{1,3}(?:[\\,_]?[0-9]{3})*(?:\\.[0-9]{1,2})?\$"))) {
        a
    } else if (string.matches(Regex("^[+-]?[0-9]{1,3}(?:[\\._]?[0-9]{3})*(?:\\,[0-9]{1,2})?\$"))) {
        b
    } else {
        string.toDouble()
    }
}