package de.hypercdn.scmt.util.data

import java.text.NumberFormat
import java.util.*

fun parseCurrencyToNumber(string: String): Pair<Number?, String?> {
    val detectedCurrencies = Locale.getAvailableLocales()
        .map { getCurrencyForLocale(it) }
        .filter { it != null && string.contains(it.symbol) }
        .distinctBy { it!!.currencyCode }
        .toList()
    var cleaned = string
        .replace("-", "0")
        .replace(Regex("[^0-9,\\._]"), "");
    if(cleaned.endsWith(".") || cleaned.endsWith(",")) cleaned = cleaned.dropLast(1)
    return parseNumberWithDecorations(cleaned) to detectedCurrencies.get(0)?.currencyCode

}
private fun getCurrencyForLocale(locale: Locale): Currency? {
    return try {
        Currency.getInstance(locale)
    }catch (e: Exception){
        return null
    }
}

fun parseNumberWithDecorations(string: String): Number {
    return if (string.matches(Regex("^[+-]?[0-9]{1,3}(?:[\\,_]?[0-9]{3})*(?:\\.[0-9]{1,2})?\$"))) {
        NumberFormat.getNumberInstance(Locale.US).parse(string).toDouble()
    } else if (string.matches(Regex("^[+-]?[0-9]{1,3}(?:[\\._]?[0-9]{3})*(?:\\,[0-9]{1,2})?\$"))) {
        NumberFormat.getNumberInstance(Locale.GERMAN).parse(string).toDouble()
    } else {
        string.toDouble()
    }
}