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
    var numString = string.replace("-", "0")
    if(numString.endsWith(".") || numString.endsWith(",")) {
        numString = numString.dropLast(1)
    }
    return if (numString.matches(Regex("^[+-]?[0-9]{1,3}(?:[\\,_]?[0-9]{3})*(?:\\.[0-9]{1,2})?\$"))) {
        NumberFormat.getNumberInstance(Locale.US).parse(numString).toDouble()
    } else if (numString.matches(Regex("^[+-]?[0-9]{1,3}(?:[\\._]?[0-9]{3})*(?:\\,[0-9]{1,2})?\$"))) {
        NumberFormat.getNumberInstance(Locale.GERMAN).parse(numString).toDouble()
    } else {
        numString.toDouble()
    }
}